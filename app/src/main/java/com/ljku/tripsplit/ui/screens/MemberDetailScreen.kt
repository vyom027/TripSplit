package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.Settlement
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.ui.components.ExpenseDetailModalBottomSheet
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.theme.AccentTeal
import com.ljku.tripsplit.ui.theme.BackgroundDimWhite
import com.ljku.tripsplit.ui.theme.MutedSageGreen
import com.ljku.tripsplit.ui.theme.MutedTerracotta
import com.ljku.tripsplit.ui.theme.TextCharcoal
import com.ljku.tripsplit.ui.theme.TextGray
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    member: Member,
    members: List<Member>,
    expenses: List<Expense>,
    settlements: List<Settlement>,
    canManage: Boolean = true,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onEditExpense: ((String) -> Unit)? = null,
    onDeleteExpense: ((String) -> Unit)? = null,
    onExpenseClick: ((String) -> Unit)? = null,
    onBack: () -> Unit
) {
    var selectedExpenseDetail by remember { mutableStateOf<Expense?>(null) }
    val balances = remember(expenses, settlements, members) {
        SettlementEngine.calculateBalances(members, expenses, settlements)
    }
    val balance = balances[member.id] ?: 0L

    val relatedExpenses = expenses.filter { expense ->
        expense.paidBy.any { it.memberId == member.id } ||
            expense.splitAmong.any { it.memberId == member.id }
    }.sortedByDescending { it.date }

    val totalPaid = expenses.flatMap { it.paidBy }
        .filter { it.memberId == member.id }
        .sumOf { it.amountPaid }

    val totalShare = expenses.flatMap { it.splitAmong }
        .filter { it.memberId == member.id }
        .sumOf { it.amountOwed }

    Scaffold(
        containerColor = BackgroundDimWhite,
        topBar = {
            TopAppBar(
                title = { Text(member.displayName(currentMemberId, viewerName), style = Typography.titleLarge, color = TextCharcoal) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextCharcoal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDimWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GlassyCard(modifier = Modifier.fillMaxWidth(), elevation = 8.dp) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Net balance", style = Typography.labelLarge, color = TextGray)
                        val color = when {
                            balance > 0 -> MutedSageGreen
                            balance < 0 -> MutedTerracotta
                            else -> TextGray
                        }
                        val prefix = if (balance > 0) "+" else if (balance < 0) "-" else ""
                        Text(
                            "$prefix${Formatters.money(abs(balance))}",
                            style = Typography.displayMedium,
                            color = color
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Paid ${Formatters.money(totalPaid)} · Share ${Formatters.money(totalShare)}", style = Typography.labelLarge, color = TextGray)
                    }
                }
            }

            item {
                Text("EXPENSES", style = Typography.labelMedium, color = TextGray)
            }

            if (relatedExpenses.isEmpty()) {
                item { Text("No expenses yet.", color = TextGray) }
            } else {
                items(relatedExpenses, key = { it.id }) { expense ->
                    GlassyCard(modifier = Modifier.fillMaxWidth(), onClick = { selectedExpenseDetail = expense }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(expense.description, style = Typography.bodyLarge, color = TextCharcoal)
                                Text(Formatters.date(expense.date), style = Typography.labelLarge, color = TextGray)
                            }
                            Text(Formatters.money(expense.totalAmount), style = Typography.titleLarge, color = AccentTeal)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    selectedExpenseDetail?.let { expense ->
        ExpenseDetailModalBottomSheet(
            expense = expense,
            members = members,
            canManage = canManage && onEditExpense != null,
            onEdit = {
                val expId = expense.id
                selectedExpenseDetail = null
                onEditExpense?.invoke(expId)
            },
            onDelete = {
                val expId = expense.id
                selectedExpenseDetail = null
                onDeleteExpense?.invoke(expId)
            },
            onDismiss = { selectedExpenseDetail = null }
        )
    }
}
