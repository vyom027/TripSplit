package com.ljku.tripsplit.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ljku.tripsplit.domain.PaidShare
import com.ljku.tripsplit.domain.SplitShare

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val budget: Long? = null,
    val accentColor: Int? = null,
    val managerMemberId: String? = null,
    val managerRecoveryToken: String? = null,
    val locked: Boolean = false,
    val archived: Boolean = false,
    val unlocked: Boolean = false,
    val categoryBudgets: Map<String, Long> = emptyMap()
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("inviteToken")]
)
data class MemberEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val name: String,
    val upiId: String? = null,
    val inviteToken: String = "",
    val role: String = "MEMBER"
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val description: String,
    val totalAmount: Long,
    val date: Long,
    val paidBy: List<PaidShare>,
    val splitAmong: List<SplitShare>,
    val splitMode: String = "EQUAL",
    val category: String = "Other",
    val notes: String? = null,
    val isPersonal: Boolean = false,
    val personalMemberId: String? = null
)

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class SettlementEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Long,
    val settledAt: Long
)

@Entity(
    tableName = "expense_requests",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("status")]
)
data class ExpenseRequestEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val fromMemberId: String,
    val description: String,
    val totalAmount: Long,
    val date: Long,
    val paidBy: List<PaidShare>,
    val splitAmong: List<SplitShare>,
    val splitMode: String = "EQUAL",
    val category: String = "Other",
    val notes: String? = null,
    val isPersonal: Boolean = false,
    val personalMemberId: String? = null,
    val receiptUri: String? = null,
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)
