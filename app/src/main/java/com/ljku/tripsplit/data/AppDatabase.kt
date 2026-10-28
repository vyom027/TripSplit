package com.ljku.tripsplit.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ljku.tripsplit.data.daos.TripDao
import com.ljku.tripsplit.data.entities.ExpenseEntity
import com.ljku.tripsplit.data.entities.ExpenseRequestEntity
import com.ljku.tripsplit.data.entities.MemberEntity
import com.ljku.tripsplit.data.entities.SettlementEntity
import com.ljku.tripsplit.data.entities.TripEntity

@Database(
    entities = [
        TripEntity::class,
        MemberEntity::class,
        ExpenseEntity::class,
        SettlementEntity::class,
        ExpenseRequestEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trips ADD COLUMN budget INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE trips ADD COLUMN accentColor INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE expenses ADD COLUMN notes TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE expenses ADD COLUMN isPersonal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN personalMemberId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trips ADD COLUMN managerMemberId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE trips ADD COLUMN managerRecoveryToken TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE trips ADD COLUMN locked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE trips ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE trips ADD COLUMN unlocked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE trips ADD COLUMN categoryBudgets TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("ALTER TABLE members ADD COLUMN upiId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE members ADD COLUMN inviteToken TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE members ADD COLUMN role TEXT NOT NULL DEFAULT 'MEMBER'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS expense_requests (
                        id TEXT NOT NULL PRIMARY KEY,
                        tripId TEXT NOT NULL,
                        fromMemberId TEXT NOT NULL,
                        description TEXT NOT NULL,
                        totalAmount INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        paidBy TEXT NOT NULL,
                        splitAmong TEXT NOT NULL,
                        splitMode TEXT NOT NULL DEFAULT 'EQUAL',
                        category TEXT NOT NULL DEFAULT 'Other',
                        notes TEXT,
                        isPersonal INTEGER NOT NULL DEFAULT 0,
                        personalMemberId TEXT,
                        receiptUri TEXT,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(tripId) REFERENCES trips(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_requests_tripId ON expense_requests(tripId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_requests_status ON expense_requests(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_members_inviteToken ON members(inviteToken)")
            }
        }
    }
}
