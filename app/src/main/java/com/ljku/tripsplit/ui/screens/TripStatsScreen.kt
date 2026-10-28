package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.Settlement
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.ui.components.AnimatedCountUp
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.components.LuxeMemberAvatar
import com.ljku.tripsplit.ui.components.StatBox
import com.ljku.tripsplit.ui.theme.AuroraCoralLight
import com.ljku.tripsplit.ui.theme.AuroraEmeraldLight
import com.ljku.tripsplit.ui.theme.AuroraGoldLight
import com.ljku.tripsplit.ui.theme.AuroraIndigo
import com.ljku.tripsplit.ui.theme.AuroraRose
import com.ljku.tripsplit.ui.theme.MoneyMedium
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import kotlinx.coroutines.launch

// Rotating accent colors for member rows
private val memberAccentColors = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFF43F5E), // Rose
    Color(0xFF3B82F6), // Blue
    Color(0xFF8B5CF6), // Violet
    Color(0xFF06B6D4), // Cyan
    Color(0xFFEC4899), // Pink
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripStatsScreen(
    trip: Trip,
    members: List<Member>,
    expenses: List<Expense>,
    settlements: List<Settlement>,
    onBack: () -> Unit
) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    val totalGroupSpent = remember(expenses) { expenses.filter { !it.isPersonal }.sumOf { it.totalAmount } }
    val totalPersonalSpent = remember(expenses) { expenses.filter { it.isPersonal }.sumOf { it.totalAmount } }
    val totalSettled = remember(settlements) { settlements.sumOf { it.amount } }
    val averageCostPerPerson = if (members.isNotEmpty()) totalGroupSpent / members.size else 0L

    // Pre-compute per-member shares once, not inside every recompose
    val memberShares = remember(members, expenses) {
        members.map { member ->
            val groupShare = expenses.filter { !it.isPersonal }.sumOf { expense ->
                expense.splitAmong.find { it.memberId == member.id }?.amountOwed ?: 0L
            }
            val personalShare = expenses.filter { it.isPersonal && it.personalMemberId == member.id }.sumOf { it.totalAmount }
            Triple(member, groupShare, personalShare)
        }
    }
    val maxShare = memberShares.maxOfOrNull { it.second + it.third } ?: 1L

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trip Analytics",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Hero Gradient Card ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E3A5F))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = trip.name.uppercase(),
                        style = Typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Group Spend",
                        style = Typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedCountUp(
                        amount = totalGroupSpent.toDouble() / 100.0,
                        currencySymbol = "₹",
                        isLarge = true,
                        color = Color.White
                    )
                    if (totalPersonalSpent > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ ${Formatters.money(totalPersonalSpent)} personal",
                                style = Typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // ── Stat Boxes Row ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    title = "Avg / Traveler",
                    value = Formatters.money(averageCostPerPerson),
                    icon = Icons.Outlined.Group,
                    color = AuroraIndigo,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    title = "Total Settled",
                    value = Formatters.money(totalSettled),
                    icon = Icons.Outlined.Payments,
                    color = AuroraEmeraldLight,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Member Breakdown ────────────────────────────────────────────
            Text(
                text = "TRAVELER COST BREAKDOWN",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GlassyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    memberShares.forEachIndexed { index, (member, groupShare, personalShare) ->
                        val totalShare = groupShare + personalShare
                        val fraction = if (maxShare > 0) totalShare.toFloat() / maxShare.toFloat() else 0f
                        val accentColor = memberAccentColors[index % memberAccentColors.size]

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMember = member
                                    scope.launch { sheetState.show() }
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    LuxeMemberAvatar(name = member.name, size = 38.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = member.name,
                                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (personalShare > 0)
                                                "${Formatters.money(groupShare)} shared · ${Formatters.money(personalShare)} personal"
                                            else "Group share",
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.money(totalShare),
                                        style = MoneyMedium,
                                        color = accentColor
                                    )
                                    Text(
                                        text = "tap for details",
                                        style = Typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Progress bar showing share relative to highest spender
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = accentColor,
                                trackColor = accentColor.copy(alpha = 0.15f),
                                strokeCap = StrokeCap.Round
                            )
                        }

                        if (index < memberShares.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // ── Member Expense Bottom Sheet (compact, not full-screen, no lag) ────
    selectedMember?.let { member ->
        val accentColor = memberAccentColors[
            members.indexOfFirst { it.id == member.id }.coerceAtLeast(0) % memberAccentColors.size
        ]

        val memberGroupExpenses = remember(member, expenses) {
            expenses.filter { !it.isPersonal && it.splitAmong.any { s -> s.memberId == member.id } }
        }
        val memberPersonalExpenses = remember(member, expenses) {
            expenses.filter { it.isPersonal && it.personalMemberId == member.id }
        }
        val grandTotal = remember(member, memberGroupExpenses, memberPersonalExpenses) {
            memberGroupExpenses.sumOf { e ->
                e.splitAmong.find { it.memberId == member.id }?.amountOwed ?: 0L
            } + memberPersonalExpenses.sumOf { it.totalAmount }
        }

        ModalBottomSheet(
            onDismissRequest = { selectedMember = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Sticky header inside sheet
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 4.dp, bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            LuxeMemberAvatar(name = member.name, size = 36.dp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Individual expense breakdown",
                                style = Typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Total chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = Formatters.money(grandTotal),
                                style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // Scrollable expense list — Column avoids LazyColumn lag in sheets
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (memberGroupExpenses.isEmpty() && memberPersonalExpenses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses yet",
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        if (memberGroupExpenses.isNotEmpty()) {
                            Text(
                                text = "GROUP EXPENSES",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            memberGroupExpenses.forEach { expense ->
                                val myShare = expense.splitAmong
                                    .find { it.memberId == member.id }?.amountOwed ?: 0L
                                ExpenseSheetRow(
                                    label = expense.description,
                                    amount = Formatters.money(myShare),
                                    amountColor = MaterialTheme.colorScheme.onSurface,
                                    iconTint = accentColor
                                )
                            }
                        }

                        if (memberPersonalExpenses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "PERSONAL EXPENSES",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            memberPersonalExpenses.forEach { expense ->
                                ExpenseSheetRow(
                                    label = expense.description,
                                    amount = Formatters.money(expense.totalAmount),
                                    amountColor = AuroraCoralLight,
                                    iconTint = AuroraCoralLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = Formatters.money(grandTotal),
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseSheetRow(
    label: String,
    amount: String,
    amountColor: Color,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Text(
            text = amount,
            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = amountColor
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        thickness = 0.5.dp
    )
}
