package com.ljku.tripsplit.sync

import android.content.Context
import com.ljku.tripsplit.data.TripSnapshot
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.ExpenseRequest
import com.ljku.tripsplit.domain.ExpenseRequestStatus
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.MemberRole
import com.ljku.tripsplit.domain.PaidShare
import com.ljku.tripsplit.domain.Settlement
import com.ljku.tripsplit.domain.SplitMode
import com.ljku.tripsplit.domain.SplitShare
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.domain.TripIdentity
import kotlinx.coroutines.tasks.await

data class JoinResult(
    val identity: TripIdentity,
    val snapshot: TripSnapshot,
    val alreadyClaimed: Boolean = false,
    val claimedMemberName: String? = null
)

interface CloudSync {
    val available: Boolean
    suspend fun join(token: String): JoinResult?
    suspend fun push(snapshot: TripSnapshot)
    suspend fun pull(tripId: String): TripSnapshot?
    suspend fun pullAllTrips(): List<TripSnapshot>
    suspend fun resetClaim(tripId: String, memberId: String)
}

class NoOpCloudSync : CloudSync {
    override val available: Boolean = false
    override suspend fun join(token: String): JoinResult? = null
    override suspend fun push(snapshot: TripSnapshot) = Unit
    override suspend fun pull(tripId: String): TripSnapshot? = null
    override suspend fun pullAllTrips(): List<TripSnapshot> = emptyList()
    override suspend fun resetClaim(tripId: String, memberId: String) = Unit
}

fun createCloudSync(context: Context): CloudSync {
    return try {
        FirebaseCloudSync(context)
    } catch (_: Throwable) {
        NoOpCloudSync()
    }
}

private class FirebaseCloudSync(context: Context) : CloudSync {
    private val appAvailable: Boolean = runCatching {
        val firebaseAppClass = Class.forName("com.google.firebase.FirebaseApp")
        val initialize = firebaseAppClass.getMethod("initializeApp", Context::class.java)
        initialize.invoke(null, context.applicationContext)
        true
    }.getOrDefault(false)

    override val available: Boolean = appAvailable && runCatching {
        Class.forName("com.google.firebase.firestore.FirebaseFirestore")
        true
    }.getOrDefault(false)

    override suspend fun join(token: String): JoinResult? {
        if (!available) return null
        ensureAnonymous()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // 1. Direct Firestore join query (Runs on free Spark plan without Cloud Functions)
        val directResult = runCatching {
            val memberDocs = db.collectionGroup("members")
                .whereEqualTo("inviteToken", token)
                .limit(1)
                .get()
                .await()

            if (!memberDocs.isEmpty) {
                val doc = memberDocs.documents[0]
                val tripRef = doc.reference.parent.parent
                val tripId = doc.getString("tripId") ?: tripRef?.id ?: ""
                val memberId = doc.id
                val memberName = doc.getString("name")?.replace(Regex("\\s*\\([Yy]ou\\)$"), "")?.trim() ?: "Member"
                val isClaimed = doc.getBoolean("claimed") ?: false
                val roleStr = doc.getString("role") ?: "MEMBER"
                val snapshot = pull(tripId)
                val isManager = roleStr == "MANAGER" || snapshot?.trip?.managerMemberId == memberId
                val effectiveRole = if (isManager) MemberRole.MANAGER else MemberRole.MEMBER
                val effectiveToken = if (isManager && !snapshot?.trip?.managerRecoveryToken.isNullOrBlank()) snapshot.trip.managerRecoveryToken!! else token

                // Option B: One-time link burning
                // If this member invite was already claimed:
                if (!isManager && isClaimed) {
                    if (snapshot != null) {
                        return@runCatching JoinResult(
                            identity = TripIdentity(tripId, memberId, effectiveRole, effectiveToken),
                            snapshot = snapshot,
                            alreadyClaimed = true,
                            claimedMemberName = memberName
                        )
                    }
                }

                // First-time claim: Burn the link by setting claimed = true in Cloud Sync
                if (!isManager && !isClaimed) {
                    runCatching { doc.reference.update("claimed", true).await() }
                }

                if (snapshot != null) {
                    JoinResult(
                        identity = TripIdentity(tripId, memberId, effectiveRole, effectiveToken),
                        snapshot = snapshot,
                        alreadyClaimed = false,
                        claimedMemberName = memberName
                    )
                } else null
            } else {
                val tripDocs = db.collection("trips")
                    .whereEqualTo("managerRecoveryToken", token)
                    .limit(1)
                    .get()
                    .await()

                if (!tripDocs.isEmpty) {
                    val doc = tripDocs.documents[0]
                    val tripId = doc.id
                    val managerMemberId = doc.getString("managerMemberId") ?: ""
                    val snapshot = pull(tripId)
                    if (snapshot != null) {
                        JoinResult(
                            identity = TripIdentity(tripId, managerMemberId, MemberRole.MANAGER, token),
                            snapshot = snapshot
                        )
                    } else null
                } else null
            }
        }.getOrNull()

        if (directResult != null) return directResult

        // 2. Cloud Function fallback (if functions deployed)
        return runCatching {
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            val result = functions.getHttpsCallable("joinWithToken").call(hashMapOf("token" to token)).await()
            @Suppress("UNCHECKED_CAST")
            val data = result.getData() as? Map<String, Any?> ?: return null
            val tripId = data["tripId"] as? String ?: return null
            val memberId = data["memberId"] as? String ?: return null
            val role = runCatching { MemberRole.valueOf(data["role"] as? String ?: "MEMBER") }
                .getOrDefault(MemberRole.MEMBER)
            val snapshot = pull(tripId) ?: return null
            JoinResult(
                identity = TripIdentity(tripId, memberId, role, token),
                snapshot = snapshot
            )
        }.getOrNull()
    }

    override suspend fun push(snapshot: TripSnapshot) {
        if (!available) return
        ensureAnonymous()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val tripRef = db.collection("trips").document(snapshot.trip.id)

        val remoteExpenses = runCatching { tripRef.collection("expenses").get().await().documents }.getOrDefault(emptyList())
        val localExpenseIds = snapshot.expenses.map { it.id }.toSet()
        val expensesToDelete = remoteExpenses.filter { it.id !in localExpenseIds }

        val remoteSettlements = runCatching { tripRef.collection("settlements").get().await().documents }.getOrDefault(emptyList())
        val localSettlementIds = snapshot.settlements.map { it.id }.toSet()
        val settlementsToDelete = remoteSettlements.filter { it.id !in localSettlementIds }

        val remoteRequests = runCatching { tripRef.collection("requests").get().await().documents }.getOrDefault(emptyList())
        val localRequestIds = snapshot.requests.map { it.id }.toSet()
        val requestsToDelete = remoteRequests.filter { it.id !in localRequestIds }

        db.runBatch { batch ->
            batch.set(tripRef, snapshot.trip.toMap())
            snapshot.members.forEach { member ->
                batch.set(tripRef.collection("members").document(member.id), member.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }
            snapshot.expenses.forEach { expense ->
                batch.set(tripRef.collection("expenses").document(expense.id), expense.toMap())
            }
            snapshot.settlements.forEach { settlement ->
                batch.set(tripRef.collection("settlements").document(settlement.id), settlement.toMap())
            }
            snapshot.requests.forEach { request ->
                batch.set(tripRef.collection("requests").document(request.id), request.toMap())
            }
            expensesToDelete.forEach { batch.delete(it.reference) }
            settlementsToDelete.forEach { batch.delete(it.reference) }
            requestsToDelete.forEach { batch.delete(it.reference) }
        }.await()
    }

    override suspend fun pull(tripId: String): TripSnapshot? {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val tripRef = db.collection("trips").document(tripId)
        val tripSnap = tripRef.get().await()
        if (!tripSnap.exists()) return null
        val trip = tripSnap.data.toTrip(tripId)
        val members = tripRef.collection("members").get().await().documents.map { it.data.toMember(it.id, tripId) }
        val expenses = tripRef.collection("expenses").get().await().documents.map { it.data.toExpense(it.id, tripId) }
        val settlements = tripRef.collection("settlements").get().await().documents.map { it.data.toSettlement(it.id, tripId) }
        val requests = tripRef.collection("requests").get().await().documents.map { it.data.toRequest(it.id, tripId) }
        return TripSnapshot(trip, members, expenses, settlements, requests)
    }

    override suspend fun pullAllTrips(): List<TripSnapshot> {
        if (!available) return emptyList()
        ensureAnonymous()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        return runCatching {
            val tripsSnap = db.collection("trips").get().await()
            tripsSnap.documents.mapNotNull { doc ->
                pull(doc.id)
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun resetClaim(tripId: String, memberId: String) {
        if (!available) return
        ensureAnonymous()
        runCatching {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("trips").document(tripId)
                .collection("members").document(memberId)
                .update("claimed", false)
                .await()
        }
    }

    private suspend fun ensureAnonymous() {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }
}

private fun Trip.toMap() = hashMapOf(
    "name" to name,
    "createdAt" to createdAt,
    "startDate" to startDate,
    "endDate" to endDate,
    "budget" to budget,
    "accentColor" to accentColor,
    "managerMemberId" to managerMemberId,
    "managerRecoveryToken" to managerRecoveryToken,
    "locked" to locked,
    "archived" to archived,
    "unlocked" to unlocked,
    "categoryBudgets" to categoryBudgets
)

private fun Member.toMap() = hashMapOf(
    "tripId" to tripId,
    "name" to name,
    "upiId" to upiId,
    "inviteToken" to inviteToken,
    "role" to role.name
)

private fun Expense.toMap() = hashMapOf(
    "description" to description,
    "totalAmount" to totalAmount,
    "date" to date,
    "paidBy" to paidBy.map { mapOf("memberId" to it.memberId, "amountPaid" to it.amountPaid) },
    "splitAmong" to splitAmong.map { mapOf("memberId" to it.memberId, "amountOwed" to it.amountOwed) },
    "splitMode" to splitMode.name,
    "category" to category,
    "notes" to notes,
    "isPersonal" to isPersonal,
    "personalMemberId" to personalMemberId
)

private fun Settlement.toMap() = hashMapOf(
    "fromMemberId" to fromMemberId,
    "toMemberId" to toMemberId,
    "amount" to amount,
    "settledAt" to settledAt
)

private fun ExpenseRequest.toMap() = hashMapOf(
    "fromMemberId" to fromMemberId,
    "description" to description,
    "totalAmount" to totalAmount,
    "date" to date,
    "paidBy" to paidBy.map { mapOf("memberId" to it.memberId, "amountPaid" to it.amountPaid) },
    "splitAmong" to splitAmong.map { mapOf("memberId" to it.memberId, "amountOwed" to it.amountOwed) },
    "splitMode" to splitMode.name,
    "category" to category,
    "notes" to notes,
    "isPersonal" to isPersonal,
    "personalMemberId" to personalMemberId,
    "receiptUri" to receiptUri,
    "status" to status.name,
    "createdAt" to createdAt
)

private fun Map<String, Any?>?.toTrip(id: String): Trip {
    val data = this ?: emptyMap()
    @Suppress("UNCHECKED_CAST")
    val budgets = (data["categoryBudgets"] as? Map<String, Any?>)?.mapNotNull { (k, v) ->
        val n = (v as? Number)?.toLong() ?: return@mapNotNull null
        k to n
    }?.toMap() ?: emptyMap()
    return Trip(
        id = id,
        name = data["name"] as? String ?: "",
        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
        memberIds = emptyList(),
        startDate = (data["startDate"] as? Number)?.toLong(),
        endDate = (data["endDate"] as? Number)?.toLong(),
        budget = (data["budget"] as? Number)?.toLong(),
        accentColor = (data["accentColor"] as? Number)?.toInt(),
        managerMemberId = data["managerMemberId"] as? String,
        managerRecoveryToken = data["managerRecoveryToken"] as? String,
        locked = data["locked"] as? Boolean ?: false,
        archived = data["archived"] as? Boolean ?: false,
        unlocked = data["unlocked"] as? Boolean ?: false,
        categoryBudgets = budgets
    )
}

private fun Map<String, Any?>?.toMember(id: String, tripId: String): Member {
    val data = this ?: emptyMap()
    return Member(
        id = id,
        tripId = data["tripId"] as? String ?: tripId,
        name = data["name"] as? String ?: "",
        upiId = data["upiId"] as? String,
        inviteToken = data["inviteToken"] as? String ?: "",
        role = runCatching { MemberRole.valueOf(data["role"] as? String ?: "MEMBER") }.getOrDefault(MemberRole.MEMBER)
    )
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>?.toExpense(id: String, tripId: String): Expense {
    val data = this ?: emptyMap()
    return Expense(
        id = id,
        tripId = tripId,
        description = data["description"] as? String ?: "",
        totalAmount = (data["totalAmount"] as? Number)?.toLong() ?: 0L,
        date = (data["date"] as? Number)?.toLong() ?: 0L,
        paidBy = (data["paidBy"] as? List<Map<String, Any?>>).orEmpty().map {
            PaidShare(it["memberId"] as? String ?: "", (it["amountPaid"] as? Number)?.toLong() ?: 0L)
        },
        splitAmong = (data["splitAmong"] as? List<Map<String, Any?>>).orEmpty().map {
            SplitShare(it["memberId"] as? String ?: "", (it["amountOwed"] as? Number)?.toLong() ?: 0L)
        },
        splitMode = runCatching { SplitMode.valueOf(data["splitMode"] as? String ?: "EQUAL") }.getOrDefault(SplitMode.EQUAL),
        category = data["category"] as? String ?: "Other",
        notes = data["notes"] as? String,
        isPersonal = data["isPersonal"] as? Boolean ?: false,
        personalMemberId = data["personalMemberId"] as? String
    )
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>?.toSettlement(id: String, tripId: String): Settlement {
    val data = this ?: emptyMap()
    return Settlement(
        id = id,
        tripId = tripId,
        fromMemberId = data["fromMemberId"] as? String ?: "",
        toMemberId = data["toMemberId"] as? String ?: "",
        amount = (data["amount"] as? Number)?.toLong() ?: 0L,
        settledAt = (data["settledAt"] as? Number)?.toLong() ?: 0L
    )
}

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>?.toRequest(id: String, tripId: String): ExpenseRequest {
    val data = this ?: emptyMap()
    return ExpenseRequest(
        id = id,
        tripId = tripId,
        fromMemberId = data["fromMemberId"] as? String ?: "",
        description = data["description"] as? String ?: "",
        totalAmount = (data["totalAmount"] as? Number)?.toLong() ?: 0L,
        date = (data["date"] as? Number)?.toLong() ?: 0L,
        paidBy = (data["paidBy"] as? List<Map<String, Any?>>).orEmpty().map {
            PaidShare(it["memberId"] as? String ?: "", (it["amountPaid"] as? Number)?.toLong() ?: 0L)
        },
        splitAmong = (data["splitAmong"] as? List<Map<String, Any?>>).orEmpty().map {
            SplitShare(it["memberId"] as? String ?: "", (it["amountOwed"] as? Number)?.toLong() ?: 0L)
        },
        splitMode = runCatching { SplitMode.valueOf(data["splitMode"] as? String ?: "EQUAL") }.getOrDefault(SplitMode.EQUAL),
        category = data["category"] as? String ?: "Other",
        notes = data["notes"] as? String,
        isPersonal = data["isPersonal"] as? Boolean ?: false,
        personalMemberId = data["personalMemberId"] as? String,
        receiptUri = data["receiptUri"] as? String,
        status = runCatching { ExpenseRequestStatus.valueOf(data["status"] as? String ?: "PENDING") }
            .getOrDefault(ExpenseRequestStatus.PENDING),
        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
    )
}
