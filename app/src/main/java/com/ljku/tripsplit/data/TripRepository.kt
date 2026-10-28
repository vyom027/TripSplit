package com.ljku.tripsplit.data

import com.ljku.tripsplit.data.daos.TripDao
import com.ljku.tripsplit.data.entities.ExpenseEntity
import com.ljku.tripsplit.data.entities.ExpenseRequestEntity
import com.ljku.tripsplit.data.entities.MemberEntity
import com.ljku.tripsplit.data.entities.SettlementEntity
import com.ljku.tripsplit.data.entities.TripEntity
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.ExpenseRequest
import com.ljku.tripsplit.domain.ExpenseRequestStatus
import com.ljku.tripsplit.domain.InviteLinks
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.MemberRole
import com.ljku.tripsplit.domain.Settlement
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.domain.SplitMode
import com.ljku.tripsplit.domain.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class TripRepository(private val tripDao: TripDao) {

    suspend fun createTrip(trip: Trip, members: List<Member>) {
        val preparedMembers = members.map { member ->
            member.copy(
                inviteToken = member.inviteToken.ifBlank { InviteLinks.newToken() }
            )
        }
        val manager = preparedMembers.firstOrNull { it.role == MemberRole.MANAGER }
            ?: preparedMembers.firstOrNull()
        val preparedTrip = trip.copy(
            managerMemberId = trip.managerMemberId ?: manager?.id,
            managerRecoveryToken = trip.managerRecoveryToken?.ifBlank { null } ?: InviteLinks.newToken(),
            memberIds = preparedMembers.map { it.id }
        )
        val withManagerRole = preparedMembers.map { member ->
            if (member.id == preparedTrip.managerMemberId) member.copy(role = MemberRole.MANAGER)
            else member.copy(role = MemberRole.MEMBER)
        }
        tripDao.insertTrip(preparedTrip.toEntity())
        tripDao.insertMembers(withManagerRole.map { it.toEntity() })
    }

    suspend fun updateTripWithMembers(trip: Trip, members: List<Member>, removedMemberIds: List<String>) {
        val remaining = members.map { member ->
            member.copy(inviteToken = member.inviteToken.ifBlank { InviteLinks.newToken() })
        }
        val expenses = tripDao.getExpensesForTripOnce(trip.id).map { it.toDomain() }
        val sanitized = SettlementEngine.sanitizeExpensesAfterMemberRemoval(
            expenses = expenses,
            remainingMemberIds = remaining.map { it.id },
            removedIds = removedMemberIds.toSet()
        )
        tripDao.insertTrip(trip.copy(memberIds = remaining.map { it.id }).toEntity())
        removedMemberIds.forEach { tripDao.deleteMember(it) }
        tripDao.insertMembers(remaining.map { it.toEntity() })
        val sanitizedIds = sanitized.map { it.id }.toSet()
        expenses.filter { it.id !in sanitizedIds }.forEach { tripDao.deleteExpense(it.id) }
        sanitized.forEach { tripDao.insertExpense(it.toEntity()) }
    }

    suspend fun saveTrip(trip: Trip) {
        tripDao.insertTrip(trip.toEntity())
    }

    suspend fun saveExpense(expense: Expense) {
        tripDao.insertExpense(expense.toEntity())
    }

    suspend fun addExpense(expense: Expense) = saveExpense(expense)

    suspend fun deleteExpense(expenseId: String) {
        tripDao.deleteExpense(expenseId)
    }

    suspend fun recordSettlement(settlement: Settlement) {
        tripDao.insertSettlement(settlement.toEntity())
    }

    suspend fun deleteSettlement(settlementId: String) {
        tripDao.deleteSettlement(settlementId)
    }

    suspend fun addMember(member: Member) {
        val withToken = member.copy(inviteToken = member.inviteToken.ifBlank { InviteLinks.newToken() })
        tripDao.insertMembers(listOf(withToken.toEntity()))
    }

    suspend fun saveMember(member: Member) {
        tripDao.insertMembers(listOf(member.toEntity()))
    }

    suspend fun regenerateMemberToken(member: Member): Member {
        val updated = member.copy(inviteToken = InviteLinks.newToken())
        saveMember(updated)
        return updated
    }

    suspend fun regenerateManagerRecovery(trip: Trip): Trip {
        val updated = trip.copy(managerRecoveryToken = InviteLinks.newToken())
        saveTrip(updated)
        return updated
    }

    suspend fun ensureInviteTokens(tripId: String) {
        val trip = tripDao.getTripByIdOnce(tripId) ?: return
        val members = tripDao.getMembersForTripOnce(tripId).map { it.toDomain() }
        val patched = members.map { if (it.inviteToken.isBlank()) it.copy(inviteToken = InviteLinks.newToken()) else it }
        if (patched != members) tripDao.insertMembers(patched.map { it.toEntity() })
        if (trip.managerRecoveryToken.isNullOrBlank()) {
            tripDao.insertTrip(trip.copy(managerRecoveryToken = InviteLinks.newToken()))
        }

        // For the specific legacy "Dwarka" trip, Vivek is the manager. For all other trips, preserve the trip's managerMemberId.
        val legacyDwarkaId = "9710345b-ae95-49e9-a21c-b30af7389aaa"
        val targetManagerId = if (tripId == legacyDwarkaId) {
            patched.find { it.name.trim().equals("Vivek", ignoreCase = true) }?.id ?: trip.managerMemberId
        } else {
            trip.managerMemberId ?: patched.firstOrNull { it.role == MemberRole.MANAGER }?.id ?: patched.firstOrNull()?.id
        }
        if (targetManagerId != null) {
            val updatedTrip = tripDao.getTripByIdOnce(tripId)!!
            if (updatedTrip.managerMemberId != targetManagerId) {
                tripDao.insertTrip(updatedTrip.copy(managerMemberId = targetManagerId))
            }
            val correctedMembers = patched.map { m ->
                if (m.id == targetManagerId) {
                    if (m.role != MemberRole.MANAGER) m.copy(role = MemberRole.MANAGER) else m
                } else {
                    if (m.role != MemberRole.MEMBER) m.copy(role = MemberRole.MEMBER) else m
                }
            }
            if (correctedMembers != patched) {
                tripDao.insertMembers(correctedMembers.map { it.toEntity() })
            }
        }
    }

    suspend fun deleteTrip(tripId: String) {
        tripDao.deleteTrip(tripId)
    }

    suspend fun saveRequest(request: ExpenseRequest) {
        tripDao.insertRequest(request.toEntity())
    }

    suspend fun deleteRequest(requestId: String) {
        tripDao.deleteRequest(requestId)
    }

    suspend fun approveRequest(request: ExpenseRequest, expense: Expense) {
        if (request.category == "Settlement") {
            val fromId = request.paidBy.firstOrNull()?.memberId ?: request.fromMemberId
            val toId = request.splitAmong.firstOrNull()?.memberId ?: ""
            if (fromId.isNotBlank() && toId.isNotBlank()) {
                recordSettlement(
                    newSettlement(
                        tripId = request.tripId,
                        fromMemberId = fromId,
                        toMemberId = toId,
                        amount = request.totalAmount
                    )
                )
            }
        } else {
            saveExpense(expense)
        }
        tripDao.deleteRequest(request.id)
    }

    suspend fun approveRequestDirectly(request: ExpenseRequest) {
        approveRequest(request, request.toExpense())
    }

    suspend fun rejectRequest(requestId: String) {
        tripDao.deleteRequest(requestId)
    }

    suspend fun findByInviteToken(token: String): Pair<Trip, Member>? {
        tripDao.getMemberByInviteToken(token)?.let { member ->
            val trip = tripDao.getTripByIdOnce(member.tripId) ?: return null
            val members = tripDao.getMembersForTripOnce(member.tripId)
            return trip.toDomain().copy(memberIds = members.map { it.id }) to member.toDomain()
        }
        tripDao.getTripByRecoveryToken(token)?.let { tripEntity ->
            val members = tripDao.getMembersForTripOnce(tripEntity.id)
            val manager = members.firstOrNull { it.id == tripEntity.managerMemberId }
                ?: members.firstOrNull { it.role == MemberRole.MANAGER.name }
                ?: members.firstOrNull()
                ?: return null
            return tripEntity.toDomain().copy(memberIds = members.map { it.id }) to manager.toDomain().copy(role = MemberRole.MANAGER)
        }
        return null
    }

    fun getAllTrips(): Flow<List<Trip>> =
        combine(tripDao.getAllTrips(), tripDao.getAllMembers()) { trips, members ->
            val membersByTrip = members.groupBy { it.tripId }
            trips.map { entity ->
                entity.toDomain().copy(
                    memberIds = membersByTrip[entity.id]?.map { it.id } ?: emptyList()
                )
            }
        }

    fun getAllExpenses(): Flow<List<Expense>> =
        tripDao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    fun getAllMembers(): Flow<List<Member>> =
        tripDao.getAllMembers().map { list -> list.map { it.toDomain() } }

    fun getTripDetails(tripId: String): Flow<Trip?> =
        combine(tripDao.getTripById(tripId), tripDao.getMembersForTrip(tripId)) { trip, members ->
            trip?.toDomain()?.copy(
                memberIds = members.map { it.id }
            )
        }

    fun getMembersForTrip(tripId: String): Flow<List<Member>> =
        tripDao.getMembersForTrip(tripId).map { list -> list.map { it.toDomain() } }

    suspend fun getMembersForTripOnce(tripId: String): List<Member> =
        tripDao.getMembersForTripOnce(tripId).map { it.toDomain() }

    fun getExpensesForTrip(tripId: String): Flow<List<Expense>> =
        tripDao.getExpensesForTrip(tripId).map { list -> list.map { it.toDomain() } }

    fun getExpenseById(expenseId: String): Flow<Expense?> =
        tripDao.getExpenseById(expenseId).map { it?.toDomain() }

    fun getSettlementsForTrip(tripId: String): Flow<List<Settlement>> =
        tripDao.getSettlementsForTrip(tripId).map { list -> list.map { it.toDomain() } }

    fun getRequestsForTrip(tripId: String): Flow<List<ExpenseRequest>> =
        tripDao.getRequestsForTrip(tripId).map { list -> list.map { it.toDomain() } }

    fun getRequestById(requestId: String): Flow<ExpenseRequest?> =
        tripDao.getRequestById(requestId).map { it?.toDomain() }

    suspend fun getMemberById(memberId: String): Member? =
        tripDao.getMemberById(memberId)?.toDomain()

    suspend fun snapshot(tripId: String): TripSnapshot? {
        val trip = tripDao.getTripByIdOnce(tripId) ?: return null
        val rawMembers = tripDao.getMembersForTripOnce(tripId).map { it.toDomain() }
        val members = rawMembers.map {
            if (it.inviteToken.isBlank()) it.copy(inviteToken = InviteLinks.newToken()) else it
        }
        if (members != rawMembers) {
            tripDao.insertMembers(members.map { it.toEntity() })
        }
        val tripDomain = trip.toDomain()
        val preparedTrip = if (tripDomain.managerRecoveryToken.isNullOrBlank()) {
            val withToken = tripDomain.copy(managerRecoveryToken = InviteLinks.newToken())
            tripDao.insertTrip(withToken.toEntity())
            withToken
        } else tripDomain

        return TripSnapshot(
            trip = preparedTrip.copy(memberIds = members.map { it.id }),
            members = members,
            expenses = tripDao.getExpensesForTripOnce(tripId).map { it.toDomain() },
            settlements = tripDao.getSettlementsForTripOnce(tripId).map { it.toDomain() },
            requests = tripDao.getRequestsForTripOnce(tripId).map { it.toDomain() }
        )
    }

    suspend fun applySnapshot(snapshot: TripSnapshot) {
        tripDao.insertTrip(snapshot.trip.toEntity())
        tripDao.insertMembers(snapshot.members.map { it.toEntity() })

        val localRequests = tripDao.getRequestsForTripOnce(snapshot.trip.id)
        val remoteRequestIds = snapshot.requests.map { it.id }.toSet()
        localRequests.forEach { if (it.id !in remoteRequestIds) tripDao.deleteRequest(it.id) }

        val localExpenses = tripDao.getExpensesForTripOnce(snapshot.trip.id)
        val remoteExpenseIds = snapshot.expenses.map { it.id }.toSet()
        localExpenses.forEach { if (it.id !in remoteExpenseIds) tripDao.deleteExpense(it.id) }

        val localSettlements = tripDao.getSettlementsForTripOnce(snapshot.trip.id)
        val remoteSettlementIds = snapshot.settlements.map { it.id }.toSet()
        localSettlements.forEach { if (it.id !in remoteSettlementIds) tripDao.deleteSettlement(it.id) }

        snapshot.expenses.forEach { tripDao.insertExpense(it.toEntity()) }
        snapshot.settlements.forEach { tripDao.insertSettlement(it.toEntity()) }
        snapshot.requests.forEach { tripDao.insertRequest(it.toEntity()) }
    }

    fun newSettlement(
        tripId: String,
        fromMemberId: String,
        toMemberId: String,
        amount: Long
    ) = Settlement(
        id = UUID.randomUUID().toString(),
        tripId = tripId,
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        amount = amount,
        settledAt = System.currentTimeMillis()
    )

    private fun TripEntity.toDomain() = Trip(
        id = id,
        name = name,
        createdAt = createdAt,
        memberIds = emptyList(),
        startDate = startDate,
        endDate = endDate,
        budget = budget,
        accentColor = accentColor,
        managerMemberId = managerMemberId,
        managerRecoveryToken = managerRecoveryToken,
        locked = locked,
        archived = archived,
        unlocked = unlocked,
        categoryBudgets = categoryBudgets
    )

    private fun Trip.toEntity() = TripEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        startDate = startDate,
        endDate = endDate,
        budget = budget,
        accentColor = accentColor,
        managerMemberId = managerMemberId,
        managerRecoveryToken = managerRecoveryToken,
        locked = locked,
        archived = archived,
        unlocked = unlocked,
        categoryBudgets = categoryBudgets
    )

    private fun MemberEntity.toDomain() = Member(
        id = id,
        tripId = tripId,
        name = name,
        upiId = upiId,
        inviteToken = inviteToken,
        role = runCatching { MemberRole.valueOf(role) }.getOrDefault(MemberRole.MEMBER)
    )

    private fun Member.toEntity() = MemberEntity(
        id = id,
        tripId = tripId,
        name = name,
        upiId = upiId,
        inviteToken = inviteToken,
        role = role.name
    )

    private fun ExpenseEntity.toDomain() = Expense(
        id = id,
        tripId = tripId,
        description = description,
        totalAmount = totalAmount,
        date = date,
        paidBy = paidBy,
        splitAmong = splitAmong,
        splitMode = runCatching { SplitMode.valueOf(splitMode) }.getOrDefault(SplitMode.EQUAL),
        category = category,
        notes = notes,
        isPersonal = isPersonal,
        personalMemberId = personalMemberId
    )

    private fun Expense.toEntity() = ExpenseEntity(
        id = id,
        tripId = tripId,
        description = description,
        totalAmount = totalAmount,
        date = date,
        paidBy = paidBy,
        splitAmong = splitAmong,
        splitMode = splitMode.name,
        category = category,
        notes = notes,
        isPersonal = isPersonal,
        personalMemberId = personalMemberId
    )

    private fun SettlementEntity.toDomain() = Settlement(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        amount = amount,
        settledAt = settledAt
    )

    private fun Settlement.toEntity() = SettlementEntity(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        toMemberId = toMemberId,
        amount = amount,
        settledAt = settledAt
    )

    private fun ExpenseRequestEntity.toDomain() = ExpenseRequest(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        description = description,
        totalAmount = totalAmount,
        date = date,
        paidBy = paidBy,
        splitAmong = splitAmong,
        splitMode = runCatching { SplitMode.valueOf(splitMode) }.getOrDefault(SplitMode.EQUAL),
        category = category,
        notes = notes,
        isPersonal = isPersonal,
        personalMemberId = personalMemberId,
        receiptUri = receiptUri,
        status = runCatching { ExpenseRequestStatus.valueOf(status) }.getOrDefault(ExpenseRequestStatus.PENDING),
        createdAt = createdAt
    )

    private fun ExpenseRequest.toEntity() = ExpenseRequestEntity(
        id = id,
        tripId = tripId,
        fromMemberId = fromMemberId,
        description = description,
        totalAmount = totalAmount,
        date = date,
        paidBy = paidBy,
        splitAmong = splitAmong,
        splitMode = splitMode.name,
        category = category,
        notes = notes,
        isPersonal = isPersonal,
        personalMemberId = personalMemberId,
        receiptUri = receiptUri,
        status = status.name,
        createdAt = createdAt
    )
}

data class TripSnapshot(
    val trip: Trip,
    val members: List<Member>,
    val expenses: List<Expense>,
    val settlements: List<Settlement>,
    val requests: List<ExpenseRequest>
)
