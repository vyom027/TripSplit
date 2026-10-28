package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.SplitMode
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expense: Expense,
    members: List<Member>,
    canManage: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Expense Details", style = Typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    if (canManage) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete Expense", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (canManage) {
                FloatingActionButton(
                    onClick = onEdit,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit Expense")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val icon = when (expense.category) {
                        "Food" -> Icons.Outlined.Restaurant
                        "Transport" -> Icons.Outlined.DirectionsCar
                        "Stay" -> Icons.Outlined.Hotel
                        "Activities" -> Icons.Outlined.SportsEsports
                        else -> Icons.Outlined.Category
                    }
                    val categoryColor = when (expense.category) {
                        "Food" -> Color(0xFFE07A5F)
                        "Transport" -> Color(0xFF5B8DD9)
                        "Stay" -> Color(0xFF8A5BB8)
                        "Activities" -> Color(0xFF4ECDC4)
                        else -> Color(0xFF9E9E9E)
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(categoryColor.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = expense.category, tint = categoryColor, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Formatters.money(expense.totalAmount),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = expense.description,
                        style = Typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.date(expense.date),
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.isPersonal) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Personal Expense") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.tertiary
                            ),
                            border = null
                        )
                    }
                }
            }

            if (!expense.notes.isNullOrBlank()) {
                item {
                    GlassyCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(expense.notes, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }

            item {
                Text("PAID BY", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, elevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        expense.paidBy.forEach { paidShare ->
                            val memberName = members.find { it.id == paidShare.memberId }?.name ?: "Unknown"
                            val pct = if (expense.totalAmount > 0) ((paidShare.amountPaid.toFloat() / expense.totalAmount.toFloat()) * 100).toInt() else 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            memberName.take(1).uppercase(),
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(memberName, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                        Text("$pct% of total", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    Formatters.money(paidShare.amountPaid),
                                    style = Typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                val splitModeText = when (expense.splitMode) {
                    SplitMode.EQUAL -> "SPLIT EQUALLY"
                    SplitMode.EXACT -> "SPLIT EXACTLY"
                    SplitMode.PERCENTAGE -> "SPLIT BY PERCENTAGE"
                    SplitMode.SHARES -> "SPLIT BY SHARES"
                    SplitMode.PERSONAL -> "PERSONAL EXPENSE"
                }
                Text(splitModeText, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                GlassyCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, elevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        expense.splitAmong.forEach { splitShare ->
                            val memberName = members.find { it.id == splitShare.memberId }?.name ?: "Unknown"
                            val pct = if (expense.totalAmount > 0) ((splitShare.amountOwed.toFloat() / expense.totalAmount.toFloat()) * 100).toInt() else 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            memberName.take(1).uppercase(),
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(memberName, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                        Text("$pct% share", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    Formatters.money(splitShare.amountOwed),
                                    style = Typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // FAB padding
        }
    }
}
