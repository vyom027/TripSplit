package com.ljku.tripsplit.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ljku.tripsplit.data.entities.ExpenseEntity
import com.ljku.tripsplit.data.entities.ExpenseRequestEntity
import com.ljku.tripsplit.data.entities.MemberEntity
import com.ljku.tripsplit.data.entities.SettlementEntity
import com.ljku.tripsplit.data.entities.TripEntity
import kotlinx.coroutines.flow.Flow

import androidx.room.Upsert

@Dao
interface TripDao {
    @Upsert
    suspend fun insertTrip(trip: TripEntity)

    @Upsert
    suspend fun insertMembers(members: List<MemberEntity>)

    @Upsert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Upsert
    suspend fun insertSettlement(settlement: SettlementEntity)

    @Upsert
    suspend fun insertRequest(request: ExpenseRequestEntity)

    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripById(tripId: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripByIdOnce(tripId: String): TripEntity?

    @Query("SELECT * FROM members WHERE tripId = :tripId ORDER BY name ASC")
    fun getMembersForTrip(tripId: String): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members")
    suspend fun getAllMembersOnce(): List<MemberEntity>

    @Query("SELECT * FROM members WHERE tripId = :tripId ORDER BY name ASC")
    suspend fun getMembersForTripOnce(tripId: String): List<MemberEntity>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    suspend fun getMemberById(memberId: String): MemberEntity?

    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY date DESC")
    fun getExpensesForTrip(tripId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY date DESC")
    suspend fun getExpensesForTripOnce(tripId: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM settlements WHERE tripId = :tripId ORDER BY settledAt DESC")
    fun getSettlementsForTrip(tripId: String): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements WHERE tripId = :tripId ORDER BY settledAt DESC")
    suspend fun getSettlementsForTripOnce(tripId: String): List<SettlementEntity>

    @Query("SELECT * FROM expense_requests WHERE tripId = :tripId ORDER BY createdAt DESC")
    fun getRequestsForTrip(tripId: String): Flow<List<ExpenseRequestEntity>>

    @Query("SELECT * FROM expense_requests WHERE tripId = :tripId ORDER BY createdAt DESC")
    suspend fun getRequestsForTripOnce(tripId: String): List<ExpenseRequestEntity>

    @Query("SELECT * FROM expense_requests WHERE id = :requestId")
    fun getRequestById(requestId: String): Flow<ExpenseRequestEntity?>

    @Query("SELECT * FROM members WHERE inviteToken = :token LIMIT 1")
    suspend fun getMemberByInviteToken(token: String): MemberEntity?

    @Query("SELECT * FROM trips WHERE managerRecoveryToken = :token LIMIT 1")
    suspend fun getTripByRecoveryToken(token: String): TripEntity?

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteMember(memberId: String)

    @Query("DELETE FROM settlements WHERE id = :settlementId")
    suspend fun deleteSettlement(settlementId: String)

    @Query("DELETE FROM expense_requests WHERE id = :requestId")
    suspend fun deleteRequest(requestId: String)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)
}
