package com.ljku.tripsplit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.ExpenseRequest
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.Settlement
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.domain.SpendTotals
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.motion.StaggeredEnterAnimation
import com.ljku.tripsplit.ui.components.AnimatedCountUp
import com.ljku.tripsplit.ui.components.AuroraGradientCard
import com.ljku.tripsplit.ui.components.CategoryIconBox
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.components.LedgerGroupedContainer
import com.ljku.tripsplit.ui.components.LedgerHairlineDivider
import com.ljku.tripsplit.ui.components.LuxeMemberAvatar
import com.ljku.tripsplit.ui.components.LuxeSegmentedTabs
import com.ljku.tripsplit.ui.components.SettlementAvatarCard
import com.ljku.tripsplit.domain.Transaction
import com.ljku.tripsplit.ui.components.DebtTransferSliderCard
import com.ljku.tripsplit.ui.components.ExpenseDetailModalBottomSheet
import com.ljku.tripsplit.ui.components.RequestExpenseModalBottomSheet
import com.ljku.tripsplit.ui.components.SettleUpModalBottomSheet
import com.ljku.tripsplit.ui.components.SettlementCard
import com.ljku.tripsplit.ui.components.pressAnimation
import androidx.compose.material.icons.automirrored.rounded.Send
import com.ljku.tripsplit.ui.theme.BalanceNegative
import com.ljku.tripsplit.ui.theme.BalancePositive
import com.ljku.tripsplit.ui.theme.LedgerAmount
import com.ljku.tripsplit.ui.theme.MicroShare
import com.ljku.tripsplit.ui.theme.MoneyLarge
import com.ljku.tripsplit.ui.theme.MoneyMedium
import com.ljku.tripsplit.ui.theme.SectionTag
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private data class CategoryStyle(val icon: ImageVector, val color: Color)

private fun categoryStyle(category: String): CategoryStyle {
    return when (category.lowercase()) {
        "food" -> CategoryStyle(Icons.Outlined.Restaurant, Color(0xFF10B981))
        "transport" -> CategoryStyle(Icons.Outlined.DirectionsCar, Color(0xFF06B6D4))
        "stay" -> CategoryStyle(Icons.Outlined.Hotel, Color(0xFF8B5CF6))
        "activities" -> CategoryStyle(Icons.Outlined.SportsEsports, Color(0xFFF59E0B))
        else -> CategoryStyle(Icons.Outlined.Category, Color(0xFF94A3B8))
    }
}

private fun formatDateGroup(millis: Long): String {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    val todayCal = java.util.Calendar.getInstance()
    val yesterdayCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
    return when {
        cal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
        cal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR) -> "Today"
        cal.get(java.util.Calendar.YEAR) == yesterdayCal.get(java.util.Calendar.YEAR) &&
        cal.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCal.get(java.util.Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDashboardScreen(
    trip: Trip,
    members: List<Member>,
    expenses: List<Expense>,
    settlements: List<Settlement>,
    pendingRequests: List<ExpenseRequest> = emptyList(),
    isManager: Boolean = true,
    viewerName: String? = null,
    currentMemberId: String? = null,
    onAddExpense: () -> Unit,
    onRequestExpense: () -> Unit = {},
    onSubmitExpenseRequest: ((ExpenseRequest) -> Unit)? = null,
    onApproveRequest: ((ExpenseRequest) -> Unit)? = null,
    onRejectRequest: ((String) -> Unit)? = null,
    onRequestSettlement: ((Transaction) -> Unit)? = null,
    onOpenRequests: () -> Unit = {},
    onSettleUp: () -> Unit,
    onQuickSettle: (String) -> Unit,
    onEditTrip: () -> Unit,
    onShare: () -> Unit,
    onMemberClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit,
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onUndoDeleteExpense: (Expense) -> Unit,
    onStatsClick: () -> Unit,
    onMembersClick: () -> Unit,
    onBack: () -> Unit,
    onMarkSettled: ((Transaction) -> Unit)? = null
) {
    var settleModalTx by remember { mutableStateOf<Transaction?>(null) }
    var showSettleModal by remember { mutableStateOf(false) }
    var showRequestExpenseModal by remember { mutableStateOf(false) }
    var selectedExpenseDetail by remember { mutableStateOf<Expense?>(null) }

    val balances = remember(members, expenses, settlements) {
        SettlementEngine.calculateBalances(members, expenses, settlements)
    }
    val pendingDebts = remember(balances) { SettlementEngine.simplifyDebts(balances) }
    val groupSpent = remember(expenses) { SpendTotals.group(expenses) }

    val viewerMember = remember(viewerName, currentMemberId, members) {
        if (currentMemberId != null) members.find { it.id == currentMemberId }
        else members.find { it.name.equals(viewerName, ignoreCase = true) }
    }
    val effectiveCurrentMemberId = currentMemberId ?: viewerMember?.id
    val effectiveViewerName = viewerMember?.name ?: viewerName
    val viewerBalance = viewerMember?.let { balances[it.id] } ?: 0L

    val tripDates = when {
        trip.startDate != null && trip.endDate != null ->
            "${Formatters.date(trip.startDate)} → ${Formatters.date(trip.endDate)}"
        trip.startDate != null -> "From ${Formatters.date(trip.startDate)}"
        trip.endDate != null -> "Until ${Formatters.date(trip.endDate)}"
        else -> null
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember(expenses.size, pendingDebts.size, pendingRequests.size) {
        if (pendingRequests.isNotEmpty()) {
            listOf("Expenses (${expenses.size})", "Balances (${pendingDebts.size})", "Requests (${pendingRequests.size})")
        } else {
            listOf("Expenses (${expenses.size})", "Balances (${pendingDebts.size})")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showOverflowMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = trip.name,
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        tripDates?.let {
                            Text(
                                text = it,
                                style = Typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Outlined.Analytics, contentDescription = "Stats", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onMembersClick) {
                        Icon(Icons.Outlined.Group, contentDescription = "Members", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share Trip") },
                                onClick = { onShare(); showOverflowMenu = false },
                                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) }
                            )
                            if (isManager) {
                                DropdownMenuItem(
                                    text = { Text("Edit Trip Settings") },
                                    onClick = { onEditTrip(); showOverflowMenu = false },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val locked = trip.isEffectivelyLocked()
            if (!locked && selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        if (isManager) onAddExpense() else showRequestExpenseModal = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                    shape = CircleShape,
                    modifier = Modifier.pressAnimation()
                ) {
                    Icon(
                        imageVector = if (isManager) Icons.Rounded.Add else Icons.AutoMirrored.Rounded.Send,
                        contentDescription = if (isManager) "Add Expense" else "Request Expense",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- Swiss Ledger Open Masthead & Position Strip ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPEDITION TOTAL",
                        style = com.ljku.tripsplit.ui.theme.SectionTag,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "${members.size} travelers",
                            style = com.ljku.tripsplit.ui.theme.SectionTag,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Large Tabular Animated Count
                AnimatedCountUp(
                    amount = groupSpent.toDouble() / 100.0,
                    currencySymbol = "₹",
                    isLarge = true,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // The Tactile 2-Box Metric Split Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Box 1: YOU ARE OWED
                    val owedAmount = if (viewerBalance > 0) viewerBalance else 0L
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "YOU ARE OWED",
                                style = com.ljku.tripsplit.ui.theme.SectionTag,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (owedAmount > 0) "+${Formatters.money(owedAmount)}" else "₹0",
                                style = com.ljku.tripsplit.ui.theme.LedgerAmount,
                                color = if (owedAmount > 0) com.ljku.tripsplit.ui.theme.BalancePositive else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Box 2: YOU OWE
                    val debtAmount = if (viewerBalance < 0) abs(viewerBalance) else 0L
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "YOU OWE",
                                style = com.ljku.tripsplit.ui.theme.SectionTag,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (debtAmount > 0) "-${Formatters.money(debtAmount)}" else "₹0",
                                style = com.ljku.tripsplit.ui.theme.LedgerAmount,
                                color = if (debtAmount > 0) com.ljku.tripsplit.ui.theme.BalanceNegative else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row: High-Contrast Primary Button + 1px Hairline Secondary Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isManager) {
                        Button(
                            onClick = onAddExpense,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .pressAnimation(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Log Expense",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                settleModalTx = null
                                showSettleModal = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .pressAnimation(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Outlined.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Settle Debts",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Button(
                            onClick = { showRequestExpenseModal = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .pressAnimation(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Request Expense",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                settleModalTx = null
                                showSettleModal = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .pressAnimation(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Outlined.Payment, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Request Settle",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Outstanding Debts ("Devang owes Vivek") - Left-to-Right Horizontal Slider
            if (pendingDebts.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "WHO OWES WHOM",
                                style = com.ljku.tripsplit.ui.theme.SectionTag,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${pendingDebts.size}",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = "Swipe to review ➔",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingDebts, key = { "${it.fromMemberId}_${it.toMemberId}" }) { tx ->
                            val fromMember = members.find { it.id == tx.fromMemberId }?.displayName(effectiveCurrentMemberId, effectiveViewerName) ?: "Someone"
                            val toMember = members.find { it.id == tx.toMemberId }?.displayName(effectiveCurrentMemberId, effectiveViewerName) ?: "Someone"
                            DebtTransferSliderCard(
                                fromName = fromMember,
                                toName = toMember,
                                amountStr = Formatters.money(tx.amount),
                                onSettleClick = {
                                    settleModalTx = tx
                                    showSettleModal = true
                                }
                            )
                        }
                    }
                }
            }

            // Segmented Tabs
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                LuxeSegmentedTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> ExpensesTab(
                    expenses = expenses,
                    members = members,
                    viewerMember = viewerMember,
                    canManage = isManager && !trip.isEffectivelyLocked(),
                    onExpenseClick = { expenseId ->
                        selectedExpenseDetail = expenses.find { it.id == expenseId }
                    },
                    onEditExpense = onEditExpense,
                    onDeleteExpense = { expenseId ->
                        val expenseToDelete = expenses.find { it.id == expenseId }
                        onDeleteExpense(expenseId)
                        if (expenseToDelete != null) {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Expense deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onUndoDeleteExpense(expenseToDelete)
                                }
                            }
                        }
                    }
                )
                1 -> BalancesTab(
                    members = members,
                    balances = balances,
                    pendingDebts = pendingDebts,
                    isManager = isManager,
                    currentMemberId = effectiveCurrentMemberId,
                    viewerName = effectiveViewerName,
                    onSettleUp = {
                        settleModalTx = null
                        showSettleModal = true
                    },
                    onQuickSettle = { memberId ->
                        settleModalTx = pendingDebts.find { it.fromMemberId == memberId || it.toMemberId == memberId }
                        showSettleModal = true
                    },
                    onMemberClick = onMemberClick
                )
                2 -> RequestsTab(
                    pendingRequests = pendingRequests,
                    members = members,
                    isManager = isManager,
                    currentMemberId = effectiveCurrentMemberId,
                    viewerName = effectiveViewerName,
                    onApproveRequest = { req -> onApproveRequest?.invoke(req) },
                    onRejectRequest = { reqId -> onRejectRequest?.invoke(reqId) },
                    onOpenRequests = onOpenRequests
                )
            }
        }
    }

    if (showSettleModal) {
        SettleUpModalBottomSheet(
            pendingDebts = pendingDebts,
            members = members,
            initialTransaction = settleModalTx,
            canManage = isManager,
            currentMemberId = effectiveCurrentMemberId,
            viewerName = effectiveViewerName,
            onDismiss = { showSettleModal = false },
            onMarkSettled = { tx ->
                if (isManager) {
                    onMarkSettled?.invoke(tx)
                }
                showSettleModal = false
            },
            onRequestSettlement = { tx ->
                onRequestSettlement?.invoke(tx)
                showSettleModal = false
            }
        )
    }

    if (showRequestExpenseModal) {
        RequestExpenseModalBottomSheet(
            tripId = trip.id,
            members = members,
            defaultPayerId = viewerMember?.id ?: members.firstOrNull()?.id,
            currentMemberId = effectiveCurrentMemberId,
            viewerName = effectiveViewerName,
            onDismiss = { showRequestExpenseModal = false },
            onSubmitRequest = { req ->
                onSubmitExpenseRequest?.invoke(req)
                scope.launch {
                    snackbarHostState.showSnackbar("Expense request sent to manager")
                }
            }
        )
    }

    selectedExpenseDetail?.let { expense ->
        ExpenseDetailModalBottomSheet(
            expense = expense,
            members = members,
            canManage = isManager && !trip.isEffectivelyLocked(),
            currentMemberId = effectiveCurrentMemberId,
            viewerName = effectiveViewerName,
            onEdit = {
                val expId = expense.id
                selectedExpenseDetail = null
                onEditExpense(expId)
            },
            onDelete = {
                val expId = expense.id
                val expenseToDelete = expense
                selectedExpenseDetail = null
                onDeleteExpense(expId)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Expense deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onUndoDeleteExpense(expenseToDelete)
                    }
                }
            },
            onDismiss = { selectedExpenseDetail = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpensesTab(
    expenses: List<Expense>,
    members: List<Member>,
    viewerMember: Member?,
    canManage: Boolean,
    onExpenseClick: (String) -> Unit,
    onEditExpense: (String) -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf("Date") }

    val categories = remember(expenses) { expenses.map { it.category }.distinct().filterNotNull() }

    val filteredExpenses = remember(expenses, searchQuery, filterCategory, sortOption) {
        val filtered = expenses.filter { expense ->
            val matchesSearch = searchQuery.isBlank() || expense.description.contains(searchQuery, ignoreCase = true)
            val matchesCat = filterCategory == null || expense.category == filterCategory
            matchesSearch && matchesCat
        }
        when (sortOption) {
            "Amount" -> filtered.sortedByDescending { it.totalAmount }
            "Category" -> filtered.sortedBy { it.category }
            else -> filtered.sortedByDescending { it.date }
        }
    }

    val groupedExpenses = remember(filteredExpenses) {
        filteredExpenses.groupBy { formatDateGroup(it.date) }.entries.toList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search expenses…", style = Typography.bodyMedium) },
                trailingIcon = {
                    Box {
                        var sortMenuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            listOf("Date", "Amount", "Category").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { sortOption = option; sortMenuExpanded = false },
                                    trailingIcon = {
                                        if (sortOption == option) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (categories.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filterCategory == null,
                            onClick = { filterCategory = null },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = filterCategory == cat,
                            onClick = { filterCategory = if (filterCategory == cat) null else cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (filteredExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No expenses logged",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap + to log an expense",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            groupedExpenses.forEach { (dateLabel, dayExpenses) ->
                item(key = "header_$dateLabel") {
                    Text(
                        text = dateLabel.uppercase(),
                        style = SectionTag,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )
                }
                item(key = "group_$dateLabel") {
                    LedgerGroupedContainer {
                        dayExpenses.forEachIndexed { index, expense ->
                            if (index > 0) {
                                LedgerHairlineDivider()
                            }
                            ExpenseLedgerRow(
                                expense = expense,
                                members = members,
                                viewerMember = viewerMember,
                                onClick = { onExpenseClick(expense.id) },
                                onLongClick = { if (canManage) onEditExpense(expense.id) }
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ExpenseLedgerRow(
    expense: Expense,
    members: List<Member>,
    viewerMember: Member?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val catStyle = categoryStyle(expense.category ?: "Other")
    val paidByName = expense.paidBy.firstOrNull()?.let { p ->
        members.find { it.id == p.memberId }?.displayName(viewerMember?.id, viewerMember?.name)
    } ?: "Someone"

    val viewerShare = viewerMember?.let { v ->
        expense.splitAmong.find { it.memberId == v.id }?.amountOwed
    }
    val viewerPaid = viewerMember?.let { v ->
        expense.paidBy.find { it.memberId == v.id }?.amountPaid
    }

    val shareText = when {
        viewerShare != null && viewerShare > 0 -> "Your share ${Formatters.money(viewerShare)}"
        viewerPaid != null && viewerPaid > 0 -> "You paid ${Formatters.money(viewerPaid)}"
        viewerMember != null -> "Not in split"
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIconBox(
            icon = catStyle.icon,
            tint = catStyle.color,
            size = 38.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.description,
                style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Paid by $paidByName • ${Formatters.time(expense.date)}",
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = Formatters.money(expense.totalAmount),
                style = LedgerAmount,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (shareText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = shareText,
                    style = MicroShare,
                    color = if (viewerShare != null && viewerShare > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BalancesTab(
    members: List<Member>,
    balances: Map<String, Long>,
    pendingDebts: List<com.ljku.tripsplit.domain.Transaction>,
    isManager: Boolean = true,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onSettleUp: () -> Unit,
    onQuickSettle: (String) -> Unit,
    onMemberClick: (String) -> Unit
) {
    val effectiveCurrentMemberId = currentMemberId ?: members.find { it.name.equals(viewerName, ignoreCase = true) }?.id
    val effectiveViewerName = members.find { it.id == effectiveCurrentMemberId }?.name ?: viewerName
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Suggested Payments section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suggested Settlements",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (pendingDebts.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onSettleUp,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(if (isManager) "Settle All" else "Request Settle", style = Typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (pendingDebts.isEmpty()) {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    elevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = BalancePositive,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "All settled up!",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Zero outstanding debts remain between members.",
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingDebts.forEach { tx ->
                        val fromMember = members.find { it.id == tx.fromMemberId }?.displayName(effectiveCurrentMemberId, effectiveViewerName) ?: "Someone"
                        val toMember = members.find { it.id == tx.toMemberId }?.displayName(effectiveCurrentMemberId, effectiveViewerName) ?: "Someone"
                        SettlementAvatarCard(
                            fromName = fromMember,
                            toName = toMember,
                            amountStr = Formatters.money(tx.amount),
                            onSettleClick = { onQuickSettle(tx.fromMemberId) }
                        )
                    }
                }
            }
        }

        // Member balances roster
        item {
            Text(
                text = "Member Balances",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                members.forEach { member ->
                    val bal = balances[member.id] ?: 0L
                    val balColor = when {
                        bal > 0 -> BalancePositive
                        bal < 0 -> BalanceNegative
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val balText = when {
                        bal > 0 -> "+${Formatters.money(bal)}"
                        bal < 0 -> "-${Formatters.money(abs(bal))}"
                        else -> "Settled"
                    }

                    GlassyCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp,
                        elevation = 0.dp,
                        onClick = {
                            if (bal < 0) onQuickSettle(member.id) else onMemberClick(member.id)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                LuxeMemberAvatar(name = member.name, size = 36.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = member.displayName(effectiveCurrentMemberId, effectiveViewerName),
                                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    if (member.upiId != null) {
                                        Text(
                                            text = "UPI: ${member.upiId}",
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = balText,
                                style = MoneyMedium,
                                color = balColor
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
private fun RequestsTab(
    pendingRequests: List<ExpenseRequest>,
    members: List<Member>,
    isManager: Boolean = true,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onApproveRequest: (ExpenseRequest) -> Unit,
    onRejectRequest: (String) -> Unit,
    onOpenRequests: () -> Unit
) {
    val memberMap = remember(members) { members.associateBy { it.id } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pendingRequests, key = { it.id }) { req ->
            val fromName = memberMap[req.fromMemberId]?.displayName(currentMemberId, viewerName) ?: "Someone"
            val isSettlement = req.category == "Settlement"

            GlassyCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSettlement) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (isSettlement) "SETTLEMENT REQUEST" else "EXPENSE REQUEST",
                                style = com.ljku.tripsplit.ui.theme.SectionTag.copy(fontSize = 10.sp),
                                color = if (isSettlement) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = Formatters.money(req.totalAmount),
                            style = com.ljku.tripsplit.ui.theme.MoneyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = req.description,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Requested by $fromName · ${Formatters.date(req.date)}",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isManager) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRejectRequest(req.id) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Text("Reject", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = { onApproveRequest(req) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.ljku.tripsplit.ui.theme.BalancePositive,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Accept", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⏳ Waiting for trip manager review",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
