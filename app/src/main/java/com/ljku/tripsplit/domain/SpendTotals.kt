package com.ljku.tripsplit.domain

object SpendTotals {
    fun group(expenses: List<Expense>): Long =
        expenses.filter { !it.isPersonal }.sumOf { it.totalAmount }

    fun personal(expenses: List<Expense>): Long =
        expenses.filter { it.isPersonal }.sumOf { it.totalAmount }

    fun amountPaid(expenses: List<Expense>, memberId: String, groupOnly: Boolean = true): Long =
        expenses
            .filter { if (groupOnly) !it.isPersonal else true }
            .sumOf { expense -> expense.paidBy.filter { it.memberId == memberId }.sumOf { it.amountPaid } }
}
