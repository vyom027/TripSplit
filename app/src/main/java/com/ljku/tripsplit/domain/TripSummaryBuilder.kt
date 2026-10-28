package com.ljku.tripsplit.domain

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

object TripSummaryBuilder {

  private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    currency = Currency.getInstance("INR")
    maximumFractionDigits = 0
  }

  private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

  fun buildShareText(
    trip: Trip,
    members: List<Member>,
    expenses: List<Expense>,
    settlements: List<Settlement>
  ): String {
    val balances = SettlementEngine.calculateBalances(members, expenses, settlements)
    val pending = SettlementEngine.simplifyDebts(balances)
    val totalSpent = SpendTotals.group(expenses)
    val personalSpent = SpendTotals.personal(expenses)

    val builder = StringBuilder()
    builder.appendLine("TripSplit Summary")
    builder.appendLine("Trip: ${trip.name}")
    if (trip.startDate != null || trip.endDate != null) {
      val start = trip.startDate?.let { dateFormat.format(Date(it)) } ?: "—"
      val end = trip.endDate?.let { dateFormat.format(Date(it)) } ?: "—"
      builder.appendLine("Dates: $start → $end")
    }
    builder.appendLine("Group spent: ${format(totalSpent)}")
    if (personalSpent > 0) builder.appendLine("Personal spent: ${format(personalSpent)}")
    builder.appendLine()

    if (expenses.isEmpty()) {
      builder.appendLine("No expenses recorded yet.")
    } else {
      builder.appendLine("Expenses:")
      expenses.sortedByDescending { it.date }.forEach { expense ->
        val payers = expense.paidBy.joinToString(", ") { paid ->
          val name = members.nameFor(paid.memberId)
          "$name ${format(paid.amountPaid)}"
        }
        builder.appendLine("• ${expense.description} — ${format(expense.totalAmount)} ($payers)")
      }
      builder.appendLine()
    }

    builder.appendLine("Balances:")
    members.forEach { member ->
      val balance = balances[member.id] ?: 0L
      val label = when {
        balance > 0 -> "gets back ${format(balance)}"
        balance < 0 -> "owes ${format(-balance)}"
        else -> "settled"
      }
      builder.appendLine("• ${member.name}: $label")
    }
    builder.appendLine()

    if (pending.isEmpty()) {
      builder.appendLine("All settled up!")
    } else {
      builder.appendLine("Who owes who:")
      pending.forEach { tx ->
        builder.appendLine(
          "• ${members.nameFor(tx.fromMemberId)} pays ${members.nameFor(tx.toMemberId)} ${format(tx.amount)}"
        )
      }
    }

    if (settlements.isNotEmpty()) {
      builder.appendLine()
      builder.appendLine("Settlement history:")
      settlements.sortedByDescending { it.settledAt }.forEach { settlement ->
        builder.appendLine(
          "• ${dateFormat.format(Date(settlement.settledAt))}: " +
            "${members.nameFor(settlement.fromMemberId)} → ${members.nameFor(settlement.toMemberId)} " +
            format(settlement.amount)
        )
      }
    }

    return builder.toString().trim()
  }

  private fun format(cents: Long): String = currencyFormat.format(cents / 100.0)

  private fun List<Member>.nameFor(memberId: String): String =
    find { it.id == memberId }?.name ?: "Someone"
}
