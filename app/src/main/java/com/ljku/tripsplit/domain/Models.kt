package com.ljku.tripsplit.domain

import kotlinx.serialization.Serializable

enum class SplitMode {
    EQUAL,
    EXACT,
    PERCENTAGE,
    SHARES,
    PERSONAL
}

enum class MemberRole {
    MANAGER,
    MEMBER
}

enum class ExpenseRequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class Trip(
    val id: String,
    val name: String,
    val createdAt: Long,
    val memberIds: List<String>,
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
) {
    fun isEffectivelyLocked(now: Long = System.currentTimeMillis()): Boolean {
        if (archived || locked) return true
        val ended = endDate != null && endDate < now
        return ended && !unlocked
    }
}

data class Member(
    val id: String,
    val tripId: String,
    val name: String,
    val upiId: String? = null,
    val inviteToken: String = "",
    val role: MemberRole = MemberRole.MEMBER
) {
    fun displayName(currentMemberId: String? = null, currentUserName: String? = null): String {
        val cleanName = name.replace(Regex("\\s*\\([Yy]ou\\)$"), "").trim()
        val isYou = (currentMemberId != null && this.id == currentMemberId) ||
                (!currentUserName.isNullOrBlank() && (
                    this.name.trim().equals(currentUserName.trim(), ignoreCase = true) ||
                    cleanName.equals(currentUserName.trim(), ignoreCase = true)
                ))
        return if (isYou) "$cleanName (You)" else cleanName
    }

    fun isUser(currentMemberId: String? = null, currentUserName: String? = null): Boolean {
        val cleanName = name.replace(Regex("\\s*\\([Yy]ou\\)$"), "").trim()
        return (currentMemberId != null && this.id == currentMemberId) ||
                (!currentUserName.isNullOrBlank() && (
                    this.name.trim().equals(currentUserName.trim(), ignoreCase = true) ||
                    cleanName.equals(currentUserName.trim(), ignoreCase = true)
                ))
    }
}

fun List<Member>.displayNameFor(
    memberId: String?,
    currentMemberId: String? = null,
    currentUserName: String? = null,
    fallback: String = "Someone"
): String {
    if (memberId == null) return fallback
    val m = this.find { it.id == memberId } ?: return fallback
    return m.displayName(currentMemberId, currentUserName)
}

data class Expense(
    val id: String,
    val tripId: String,
    val description: String,
    val totalAmount: Long,
    val date: Long,
    val paidBy: List<PaidShare>,
    val splitAmong: List<SplitShare>,
    val splitMode: SplitMode = SplitMode.EQUAL,
    val category: String = "Other",
    val notes: String? = null,
    val isPersonal: Boolean = false,
    val personalMemberId: String? = null
)

@Serializable
data class PaidShare(
    val memberId: String,
    val amountPaid: Long
)

@Serializable
data class SplitShare(
    val memberId: String,
    val amountOwed: Long
)

data class Settlement(
    val id: String,
    val tripId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Long,
    val settledAt: Long
)

data class Transaction(
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Long
)

data class ExpenseRequest(
    val id: String,
    val tripId: String,
    val fromMemberId: String,
    val description: String,
    val totalAmount: Long,
    val date: Long,
    val paidBy: List<PaidShare>,
    val splitAmong: List<SplitShare>,
    val splitMode: SplitMode = SplitMode.EQUAL,
    val category: String = "Other",
    val notes: String? = null,
    val isPersonal: Boolean = false,
    val personalMemberId: String? = null,
    val receiptUri: String? = null,
    val status: ExpenseRequestStatus = ExpenseRequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toExpense(): Expense = Expense(
        id = id,
        tripId = tripId,
        description = description,
        totalAmount = totalAmount,
        date = date,
        paidBy = paidBy,
        splitAmong = splitAmong,
        splitMode = splitMode,
        category = category,
        notes = notes,
        isPersonal = isPersonal,
        personalMemberId = personalMemberId
    )
}

fun Expense.toRequest(fromMemberId: String) = ExpenseRequest(
    id = id,
    tripId = tripId,
    fromMemberId = fromMemberId,
    description = description,
    totalAmount = totalAmount,
    date = date,
    paidBy = paidBy,
    splitAmong = splitAmong,
    splitMode = splitMode,
    category = category,
    notes = notes,
    isPersonal = isPersonal,
    personalMemberId = personalMemberId
)

data class TripIdentity(
    val tripId: String,
    val memberId: String,
    val role: MemberRole,
    val token: String
)
