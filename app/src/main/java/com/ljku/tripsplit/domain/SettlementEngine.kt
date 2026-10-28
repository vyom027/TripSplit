package com.ljku.tripsplit.domain

import kotlin.math.min
import kotlin.math.roundToLong

object SettlementEngine {

    fun calculateBalances(
        members: List<Member>,
        expenses: List<Expense>,
        settlements: List<Settlement> = emptyList()
    ): Map<String, Long> {
        val balances = mutableMapOf<String, Long>()
        members.forEach { balances[it.id] = 0L }

        for (expense in expenses) {
            if (expense.isPersonal) continue
            for (paid in expense.paidBy) {
                balances[paid.memberId] = (balances[paid.memberId] ?: 0L) + paid.amountPaid
            }
            for (split in expense.splitAmong) {
                balances[split.memberId] = (balances[split.memberId] ?: 0L) - split.amountOwed
            }
        }

        for (settlement in settlements) {
            balances[settlement.fromMemberId] =
                (balances[settlement.fromMemberId] ?: 0L) + settlement.amount
            balances[settlement.toMemberId] =
                (balances[settlement.toMemberId] ?: 0L) - settlement.amount
        }

        return balances
    }

    fun simplifyDebts(balances: Map<String, Long>): List<Transaction> {
        val creditors = mutableListOf<Pair<String, Long>>()
        val debtors = mutableListOf<Pair<String, Long>>()

        for ((memberId, balance) in balances) {
            if (balance > 0) {
                creditors.add(memberId to balance)
            } else if (balance < 0) {
                debtors.add(memberId to balance)
            }
        }

        creditors.sortByDescending { it.second }
        debtors.sortBy { it.second }

        val transactions = mutableListOf<Transaction>()
        var c = 0
        var d = 0

        while (c < creditors.size && d < debtors.size) {
            val creditor = creditors[c]
            val debtor = debtors[d]

            val amount = min(creditor.second, -debtor.second)
            if (amount > 0) {
                transactions.add(
                    Transaction(
                        fromMemberId = debtor.first,
                        toMemberId = creditor.first,
                        amount = amount
                    )
                )
            }

            val newCreditorBalance = creditor.second - amount
            val newDebtorBalance = debtor.second + amount

            creditors[c] = creditor.copy(second = newCreditorBalance)
            debtors[d] = debtor.copy(second = newDebtorBalance)

            if (newCreditorBalance == 0L) c++
            if (newDebtorBalance == 0L) d++
        }

        return transactions
    }

    fun buildEqualSplits(totalCents: Long, memberIds: List<String>): List<SplitShare> {
        if (memberIds.isEmpty() || totalCents <= 0) return emptyList()
        val totalRupees = Math.round(totalCents / 100.0)
        val baseRupees = totalRupees / memberIds.size
        val remainder = (totalRupees % memberIds.size).toInt()
        return memberIds.mapIndexed { index, memberId ->
            SplitShare(memberId, (baseRupees + if (index < remainder) 1 else 0) * 100L)
        }
    }

    fun buildPercentageSplits(totalCents: Long, percentages: Map<String, Double>): List<SplitShare> {
        if (totalCents <= 0 || percentages.isEmpty()) return emptyList()
        val entries = percentages.filter { it.value > 0 }
        if (entries.isEmpty()) return emptyList()

        val totalRupees = Math.round(totalCents / 100.0)
        val rawRupees = entries.map { (id, pct) ->
            id to (totalRupees * pct / 100.0).roundToLong()
        }.toMutableList()

        val assigned = rawRupees.sumOf { it.second }
        var diff = totalRupees - assigned
        var index = 0
        while (diff != 0L && rawRupees.isNotEmpty()) {
            val entry = rawRupees[index % rawRupees.size]
            val delta = if (diff > 0) 1L else -1L
            rawRupees[index % rawRupees.size] = entry.first to (entry.second + delta)
            diff -= delta
            index++
        }

        return rawRupees.map { SplitShare(it.first, it.second * 100L) }
    }

    fun buildShareSplits(totalCents: Long, shares: Map<String, Int>): List<SplitShare> {
        if (totalCents <= 0) return emptyList()
        val entries = shares.filter { it.value > 0 }
        if (entries.isEmpty()) return emptyList()
        val totalShares = entries.values.sum()
        if (totalShares <= 0) return emptyList()

        val totalRupees = Math.round(totalCents / 100.0)
        val rawRupees = entries.map { (id, share) ->
            id to (totalRupees * share / totalShares)
        }.toMutableList()

        var diff = totalRupees - rawRupees.sumOf { it.second }
        var index = 0
        while (diff != 0L && rawRupees.isNotEmpty()) {
            val entry = rawRupees[index % rawRupees.size]
            val delta = if (diff > 0) 1L else -1L
            rawRupees[index % rawRupees.size] = entry.first to (entry.second + delta)
            diff -= delta
            index++
        }
        return rawRupees.map { SplitShare(it.first, it.second * 100L) }
    }

    fun sanitizeExpensesAfterMemberRemoval(
        expenses: List<Expense>,
        remainingMemberIds: List<String>,
        removedIds: Set<String>
    ): List<Expense> {
        if (removedIds.isEmpty()) return expenses
        return expenses.mapNotNull { expense ->
            val paid = expense.paidBy.filter { it.memberId !in removedIds }
            val split = expense.splitAmong.filter { it.memberId !in removedIds }
            if (expense.isPersonal && expense.personalMemberId in removedIds) return@mapNotNull null
            if (remainingMemberIds.isEmpty()) return@mapNotNull null
            val nextPaid = paid.ifEmpty {
                listOf(PaidShare(remainingMemberIds.first(), expense.totalAmount))
            }
            val nextSplit = if (split.isNotEmpty()) {
                val splitTotal = split.sumOf { it.amountOwed }
                if (splitTotal == expense.totalAmount) split
                else buildEqualSplits(expense.totalAmount, split.map { it.memberId })
            } else {
                buildEqualSplits(expense.totalAmount, remainingMemberIds)
            }
            expense.copy(paidBy = nextPaid, splitAmong = nextSplit)
        }
    }
}
