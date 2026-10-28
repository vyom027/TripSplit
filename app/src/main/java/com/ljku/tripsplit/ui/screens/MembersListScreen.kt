package com.ljku.tripsplit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.InviteLinks
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.MemberRole
import com.ljku.tripsplit.domain.Settlement
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.domain.SpendTotals
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.motion.StaggeredEnterAnimation
import com.ljku.tripsplit.ui.components.LedgerCard
import com.ljku.tripsplit.ui.components.LuxeMemberAvatar
import com.ljku.tripsplit.ui.components.LuxeSegmentedTabs
import com.ljku.tripsplit.domain.Transaction
import com.ljku.tripsplit.ui.components.SettleUpModalBottomSheet
import com.ljku.tripsplit.ui.components.SettlementAvatarCard
import com.ljku.tripsplit.ui.components.DebtTransferSliderCard
import com.ljku.tripsplit.ui.components.pressAnimation
import com.ljku.tripsplit.ui.theme.BalanceNegative
import com.ljku.tripsplit.ui.theme.BalancePositive
import com.ljku.tripsplit.ui.theme.LedgerAmount
import com.ljku.tripsplit.ui.theme.MastheadTitle
import com.ljku.tripsplit.ui.theme.MicroShare
import com.ljku.tripsplit.ui.theme.SectionTag
import com.ljku.tripsplit.ui.theme.SignalLime
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import com.ljku.tripsplit.ui.util.UpiPay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersListScreen(
    trip: Trip,
    members: List<Member>,
    expenses: List<Expense>,
    settlements: List<Settlement> = emptyList(),
    isManager: Boolean,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onMemberClick: ((String) -> Unit)? = null,
    onQuickSettle: ((String) -> Unit)? = null,
    onMarkSettled: ((Transaction) -> Unit)? = null,
    onRequestSettlement: ((Transaction) -> Unit)? = null,
    onAddMemberClick: () -> Unit,
    onShareMemberLink: (Member) -> Unit,
    onRegenerateMember: (Member) -> Unit,
    onSaveUpi: (Member, String?) -> Unit,
    onShareRecovery: () -> Unit,
    onRegenerateRecovery: () -> Unit,
    onShareAllLinks: (() -> Unit)? = null,
    onCopyAllLinks: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val effectiveCurrentMemberId = currentMemberId ?: members.find { it.name.equals(viewerName, ignoreCase = true) }?.id
    val effectiveViewerName = members.find { it.id == effectiveCurrentMemberId }?.name ?: viewerName
    var editingUpi by remember { mutableStateOf<Member?>(null) }
    var upiDraft by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Roster, 1 = Debts, 2 = Missing UPI
    var settleModalTx by remember { mutableStateOf<Transaction?>(null) }
    var showSettleModal by remember { mutableStateOf(false) }

    // Financial calculations
    val totalPooled = remember(expenses) { SpendTotals.group(expenses) }
    val balances = remember(members, expenses, settlements) {
        SettlementEngine.calculateBalances(members, expenses, settlements)
    }
    val pendingDebts = remember(balances) { SettlementEngine.simplifyDebts(balances) }

    val membersWithUpi = remember(members) { members.count { !it.upiId.isNullOrBlank() } }

    val topContributor = remember(members, expenses) {
        members.maxByOrNull { m ->
            expenses.filter { !it.isPersonal }.sumOf { exp ->
                exp.paidBy.find { it.memberId == m.id }?.amountPaid ?: 0L
            }
        }
    }

    val topContributorAmount = remember(topContributor, expenses) {
        if (topContributor == null) 0L
        else expenses.filter { !it.isPersonal }.sumOf { exp ->
            exp.paidBy.find { it.memberId == topContributor.id }?.amountPaid ?: 0L
        }
    }

    // Filtered members list
    val filteredMembers = remember(members, searchQuery, selectedTab) {
        members.filter { m ->
            val matchesQuery = searchQuery.isBlank() ||
                    m.name.contains(searchQuery, ignoreCase = true) ||
                    (m.upiId?.contains(searchQuery, ignoreCase = true) == true)
            val matchesFilter = when (selectedTab) {
                2 -> m.upiId.isNullOrBlank()
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (isManager) {
                ExtendedFloatingActionButton(
                    onClick = onAddMemberClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                    shape = CircleShape,
                    modifier = Modifier.pressAnimation(),
                    icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = {
                        Text(
                            "Add Member",
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- Architectural Masthead & Navigation (Compact) ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (onCopyAllLinks != null) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onCopyAllLinks() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy All",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Copy All",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            if (onShareAllLinks != null) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onShareAllLinks() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = "Share All",
                                            tint = SignalLime,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Share All",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            if (isManager) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onShareRecovery() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Key,
                                            contentDescription = null,
                                            tint = SignalLime,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Master Key",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "EXPEDITION ROSTER // DIRECTORY",
                        style = SectionTag,
                        color = SignalLime
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Trip Members",
                        style = MastheadTitle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${members.size} members • ${Formatters.money(totalPooled)} total expenses",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Compact Metrics Strip (Single Row) ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TOP: ",
                            style = SectionTag.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = topContributor?.name ?: "None",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (topContributorAmount > 0) {
                            Text(
                                text = " (${Formatters.money(topContributorAmount)})",
                                style = Typography.labelSmall,
                                color = BalancePositive
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "UPI: ",
                            style = SectionTag.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$membersWithUpi/${members.size} Ready",
                            style = Typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (membersWithUpi == members.size && members.isNotEmpty()) BalancePositive else SignalLime
                            )
                        )
                    }
                }
            }

            // --- Segmented Tabs (Who Owes Whom & Roster) ---
            item {
                LuxeSegmentedTabs(
                    tabs = listOf(
                        "Roster (${members.size})",
                        "Who Owes (${pendingDebts.size})",
                        "Missing UPI (${members.size - membersWithUpi})"
                    ),
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // --- Tab 1: Who Owes Whom (Vertical Ledger List) ---
            if (selectedTab == 1) {
                if (pendingDebts.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DIRECT SETTLEMENT TRANSFERS",
                                style = SectionTag,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${pendingDebts.size} pending",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(pendingDebts, key = { "${it.fromMemberId}_${it.toMemberId}" }) { tx ->
                        val fromMember = members.find { it.id == tx.fromMemberId }?.displayName(effectiveCurrentMemberId, effectiveViewerName) ?: "Someone"
                        val toMember = members.find { it.id == tx.toMemberId }?.displayName(effectiveCurrentMemberId, effectiveViewerName) ?: "Someone"
                        SettlementAvatarCard(
                            fromName = fromMember,
                            toName = toMember,
                            amountStr = Formatters.money(tx.amount),
                            onSettleClick = if (isManager) {
                                {
                                    settleModalTx = tx
                                    showSettleModal = true
                                }
                            } else null
                        )
                    }
                } else {
                    item {
                        LedgerCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                            Row(
                                modifier = Modifier.padding(18.dp),
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
                                        "All Settled Up!",
                                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Zero outstanding debts between trip members.",
                                        style = Typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // --- Tab 0 or 2: Roster / Missing UPI ---
                // Search field (compact)
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search member or UPI...",
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                }

                // Quick Share All Directory Card (Visible on main Roster tab)
                if (selectedTab == 0 && searchQuery.isBlank() && members.isNotEmpty() && (onShareAllLinks != null || onCopyAllLinks != null)) {
                    item {
                        LedgerCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 14.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SignalLime.copy(alpha = 0.15f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Share,
                                                    contentDescription = null,
                                                    tint = SignalLime,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "INVITE DIRECTORY",
                                                style = SectionTag.copy(fontSize = 9.sp),
                                                color = SignalLime
                                            )
                                            Text(
                                                text = "Share All Member Links",
                                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Send or copy a single message with personal invite links for all ${members.size} members.",
                                    style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (onCopyAllLinks != null) {
                                        OutlinedButton(
                                            onClick = onCopyAllLinks,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ContentCopy,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                "Copy All",
                                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    if (onShareAllLinks != null) {
                                        Button(
                                            onClick = onShareAllLinks,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(34.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                "Share All",
                                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredMembers.isNotEmpty()) {
                    itemsIndexed(filteredMembers, key = { _, member -> member.id }) { index, member ->
                        StaggeredEnterAnimation(index = index) {
                            val groupPaid = expenses.filter { !it.isPersonal }.sumOf { exp ->
                                exp.paidBy.find { it.memberId == member.id }?.amountPaid ?: 0L
                            }
                            val personalPaid = expenses.filter { it.isPersonal }.sumOf { exp ->
                                exp.paidBy.find { it.memberId == member.id }?.amountPaid ?: 0L
                            }
                            val totalPaid = groupPaid + personalPaid
                            val netBalance = balances[member.id] ?: 0L

                            CompactSwissMemberCard(
                                member = member,
                                totalPaid = totalPaid,
                                netBalance = netBalance,
                                totalTripSpend = totalPooled,
                                isManager = isManager,
                                currentMemberId = effectiveCurrentMemberId,
                                viewerName = effectiveViewerName,
                                onCardClick = { onMemberClick?.invoke(member.id) },
                                onShareInvite = { onShareMemberLink(member) },
                                onCopyInvite = {
                                    val token = member.inviteToken.ifBlank { InviteLinks.newToken() }
                                    UpiPay.copy(context, InviteLinks.httpsJoin(token), "Invite Link for ${member.name}")
                                },
                                onEditUpi = {
                                    upiDraft = member.upiId.orEmpty()
                                    editingUpi = member
                                }
                            )
                        }
                    }
                } else {
                    item {
                        LedgerCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No matching members",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Try adjusting your search query.",
                                    style = Typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // --- Organizer Recovery Key Vault Card (Compact) ---
            if (isManager && !trip.managerRecoveryToken.isNullOrBlank()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    CompactOrganizerVaultCard(
                        trip = trip,
                        onShareRecovery = onShareRecovery,
                        onRegenerateRecovery = onRegenerateRecovery,
                        onCopy = {
                            val token = trip.managerRecoveryToken ?: return@CompactOrganizerVaultCard
                            UpiPay.copy(context, InviteLinks.httpsJoin(token), "Manager Recovery Key")
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }

    // Modal Bottom Sheet for UPI Setting (Sleek Bottom Sheet format)
    editingUpi?.let { member ->
        SetUpiBottomSheet(
            member = member,
            currentDraft = upiDraft,
            onDraftChange = { upiDraft = it },
            onSave = {
                onSaveUpi(member, upiDraft.trim().ifBlank { null })
                editingUpi = null
            },
            onDismiss = { editingUpi = null }
        )
    }

    if (showSettleModal) {
        SettleUpModalBottomSheet(
            pendingDebts = pendingDebts,
            members = members,
            initialTransaction = settleModalTx,
            canManage = isManager,
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
}

/**
 * Highly Compact Swiss Member Card (~90dp height).
 * Eliminates bloated spacing, keeps rich financial readability.
 */
@Composable
private fun CompactSwissMemberCard(
    member: Member,
    totalPaid: Long,
    netBalance: Long,
    totalTripSpend: Long,
    isManager: Boolean,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onCardClick: () -> Unit,
    onShareInvite: () -> Unit,
    onCopyInvite: () -> Unit,
    onEditUpi: () -> Unit
) {
    val contributionPercent = if (totalTripSpend > 0) {
        ((totalPaid.toDouble() / totalTripSpend.toDouble()) * 100).toInt()
    } else 0

    LedgerCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        onClick = onCardClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Avatar, Name + Lead Tag, Net Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LuxeMemberAvatar(name = member.name, size = 38.dp)

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = member.displayName(currentMemberId, viewerName),
                            style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (member.role == MemberRole.MANAGER) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SignalLime.copy(alpha = 0.15f),
                                border = BorderStroke(0.8.dp, SignalLime)
                            ) {
                                Text(
                                    text = "LEAD",
                                    style = SectionTag.copy(fontSize = 8.sp, letterSpacing = 0.6.sp),
                                    color = SignalLime,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = "Paid ${Formatters.money(totalPaid)} ($contributionPercent%)",
                        style = MicroShare,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Net Balance Badge
                Column(horizontalAlignment = Alignment.End) {
                    when {
                        netBalance > 0 -> {
                            Text(
                                text = "+${Formatters.money(netBalance)}",
                                style = LedgerAmount.copy(fontSize = 15.sp),
                                color = BalancePositive
                            )
                            Text(
                                text = "Gets back",
                                style = Typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = BalancePositive.copy(alpha = 0.85f)
                            )
                        }
                        netBalance < 0 -> {
                            Text(
                                text = Formatters.money(netBalance),
                                style = LedgerAmount.copy(fontSize = 15.sp),
                                color = BalanceNegative
                            )
                            Text(
                                text = "Owes group",
                                style = Typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = BalanceNegative.copy(alpha = 0.85f)
                            )
                        }
                        else -> {
                            Text(
                                text = "₹0",
                                style = LedgerAmount.copy(fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Settled",
                                style = Typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: UPI pill & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // UPI Indicator / Link Button
                if (!member.upiId.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onEditUpi() }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FlashOn,
                            contentDescription = null,
                            tint = SignalLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = member.upiId,
                            style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .clickable { onEditUpi() }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = SignalLime,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Link UPI",
                            style = Typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Compact Right Actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isManager) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onShareInvite() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Invite", style = Typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold))
                            }
                        }

                        IconButton(onClick = onCopyInvite, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCardClick() }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payments",
                            style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact Organizer Vault Card.
 */
@Composable
private fun CompactOrganizerVaultCard(
    trip: Trip,
    onShareRecovery: () -> Unit,
    onRegenerateRecovery: () -> Unit,
    onCopy: () -> Unit
) {
    val token = trip.managerRecoveryToken.orEmpty()
    val maskedToken = if (token.length >= 8) "••••-••••-${token.takeLast(4).uppercase()}" else "••••-••••"

    LedgerCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SignalLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = SignalLime, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("MASTER ORGANIZER KEY", style = SectionTag.copy(fontSize = 8.5.sp), color = SignalLime)
                    Text(maskedToken, style = LedgerAmount.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(13.dp))
                }
                IconButton(onClick = onShareRecovery, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(13.dp))
                }
                IconButton(onClick = onRegenerateRecovery, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Regen", modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

/**
 * Bottom Sheet for configuring Member UPI handle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetUpiBottomSheet(
    member: Member,
    currentDraft: String,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val upiProviders = listOf("@okhdfcbank", "@okicici", "@okaxis", "@paytm", "@ybl")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LuxeMemberAvatar(name = member.name, size = 42.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Set UPI for ${member.name}",
                        style = Typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enables instant 1-tap settlements directly to their account",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = currentDraft,
                onValueChange = onDraftChange,
                label = { Text("UPI ID (e.g. name@okhdfcbank)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Suffix Suggestions
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(upiProviders) { suffix ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val prefix = currentDraft.substringBefore("@")
                                onDraftChange(if (prefix.isBlank()) suffix else "$prefix$suffix")
                            }
                    ) {
                        Text(
                            text = suffix,
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Save UPI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
