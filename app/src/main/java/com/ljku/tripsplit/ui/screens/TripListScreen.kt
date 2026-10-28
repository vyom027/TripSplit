package com.ljku.tripsplit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.SpendTotals
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.motion.StaggeredEnterAnimation
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.components.LuxeMemberAvatar
import com.ljku.tripsplit.ui.components.LuxeSegmentedTabs
import com.ljku.tripsplit.ui.components.pressAnimation
import com.ljku.tripsplit.ui.theme.AuroraEmeraldLight
import com.ljku.tripsplit.ui.theme.AuroraVioletGradient
import com.ljku.tripsplit.ui.theme.AuroraVioletLight
import com.ljku.tripsplit.ui.theme.MoneyMedium
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    trips: List<Trip>,
    allExpenses: List<Expense> = emptyList(),
    allMembers: List<Member> = emptyList(),
    isTripManager: (String) -> Boolean = { true },
    onTripClick: (String) -> Unit,
    onCreateTripClick: () -> Unit,
    onEditTripClick: ((String) -> Unit)? = null,
    onDeleteTripClick: ((String) -> Unit)? = null,
    onJoinTrip: (suspend (String) -> Boolean)? = null
) {
    val now = System.currentTimeMillis()
    val activeTrips = remember(trips) { trips.filter { it.endDate == null || it.endDate >= now } }
    val pastTrips = remember(trips) { trips.filter { it.endDate != null && it.endDate < now } }
    val totalSpent = remember(allExpenses) { SpendTotals.group(allExpenses) }

    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Active, 2 = Past
    var selectedTripForAction by remember { mutableStateOf<Trip?>(null) }
    var tripToDelete by remember { mutableStateOf<Trip?>(null) }
    var showJoinModal by remember { mutableStateOf(false) }

    val displayedTrips = remember(selectedFilter, trips, activeTrips, pastTrips) {
        when (selectedFilter) {
            1 -> activeTrips
            2 -> pastTrips
            else -> trips
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateTripClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                shape = CircleShape,
                modifier = Modifier.pressAnimation(),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = {
                    Text(
                        "New Trip",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- 2026 Neo-Modern Editorial Header ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Journeys",
                                style = Typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Status summary counter badge
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = "${trips.size} trips • ${Formatters.money(totalSpent)} pooled",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onJoinTrip != null) {
                                Surface(
                                    onClick = { showJoinModal = true },
                                    shape = RoundedCornerShape(100.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.pressAnimation()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.GroupAdd,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Join",
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onCreateTripClick,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.pressAnimation()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "New",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    LuxeSegmentedTabs(
                        tabs = listOf(
                            "All (${trips.size})",
                            "Active (${activeTrips.size})",
                            "Past (${pastTrips.size})"
                        ),
                        selectedIndex = selectedFilter,
                        onTabSelected = { selectedFilter = it }
                    )
                }
            }

            // --- 2026 Neo-Modern Trip Cards ---
            if (displayedTrips.isNotEmpty()) {
                itemsIndexed(displayedTrips, key = { _, trip -> trip.id }) { index, trip ->
                    StaggeredEnterAnimation(index = index) {
                        val tripExpenses = allExpenses.filter { it.tripId == trip.id }
                        val isPastTrip = trip.endDate != null && trip.endDate < now
                        val tripTotal = SpendTotals.group(tripExpenses)
                        val tripMembers = allMembers.filter { it.tripId == trip.id }

                        val isHeroTrip = !isPastTrip || (index == 0 && selectedFilter == 0 && activeTrips.isEmpty())

                        if (isHeroTrip) {
                            NeoModernActiveTripCard(
                                trip = trip,
                                members = tripMembers,
                                totalSpent = tripTotal,
                                isPast = isPastTrip,
                                onClick = { onTripClick(trip.id) },
                                onLongClick = { selectedTripForAction = trip }
                            )
                        } else {
                            NeoModernPastTripCard(
                                trip = trip,
                                members = tripMembers,
                                totalSpent = tripTotal,
                                onClick = { onTripClick(trip.id) },
                                onLongClick = { selectedTripForAction = trip }
                            )
                        }
                    }
                }
            } else {
                item {
                    AuroraEmptyStateView(
                        filter = selectedFilter,
                        onCreateTripClick = onCreateTripClick,
                        onJoinTripClick = if (onJoinTrip != null) { { showJoinModal = true } } else null
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Trip Actions (Edit & Delete)
    selectedTripForAction?.let { trip ->
        ModalBottomSheet(
            onDismissRequest = { selectedTripForAction = null },
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
                Text(
                    text = trip.name,
                    style = Typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trip Settings & Options",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                val isManagerOfTrip = isTripManager(trip.id)

                // Edit Option (Managers only)
                if (isManagerOfTrip) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val id = trip.id
                                selectedTripForAction = null
                                onEditTripClick?.invoke(id)
                            }
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Trip",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Edit Trip Settings",
                                style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Rename, change dates or manage members",
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Delete / Remove Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val target = trip
                            selectedTripForAction = null
                            tripToDelete = target
                        }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = if (isManagerOfTrip) "Delete Trip" else "Remove Trip",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isManagerOfTrip) "Delete Trip" else "Remove Trip from Device",
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (isManagerOfTrip) "Permanently remove trip, members & ledger" else "Remove this trip from your device view",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Bottom Sheet (Standardized Pop-up Form)
    tripToDelete?.let { trip ->
        ModalBottomSheet(
            onDismissRequest = { tripToDelete = null },
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Delete \"${trip.name}\"?",
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Irreversible action",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to permanently delete this trip? All registered members, logged expenses, and settled ledger transactions will be permanently deleted from this device.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { tripToDelete = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            val id = trip.id
                            tripToDelete = null
                            onDeleteTripClick?.invoke(id)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Delete Trip", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showJoinModal && onJoinTrip != null) {
        JoinTripModalBottomSheet(
            onDismissRequest = { showJoinModal = false },
            onJoin = onJoinTrip
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinTripModalBottomSheet(
    onDismissRequest: () -> Unit,
    onJoin: suspend (String) -> Boolean
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var inputCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-detect if clipboard contains invite token or link
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val clipText = clipboardManager.getText()?.text?.trim() ?: ""
        if (clipText.isNotBlank()) {
            val token = com.ljku.tripsplit.domain.InviteLinks.extractToken(clipText)
            if (token.length >= 20) {
                inputCode = token
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GroupAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Join a Journey",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Paste the invite code or link from your organizer",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            androidx.compose.material3.OutlinedTextField(
                value = inputCode,
                onValueChange = { 
                    inputCode = it
                    errorMessage = null 
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Invite code or https://tripsplit.app/j/...", style = Typography.bodyMedium) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    TextButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text?.trim() ?: ""
                            if (clip.isNotBlank()) {
                                inputCode = com.ljku.tripsplit.domain.InviteLinks.extractToken(clip)
                                errorMessage = null
                            }
                        }
                    ) {
                        Text("Paste", fontWeight = FontWeight.Bold, style = Typography.labelMedium)
                    }
                },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = Typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (inputCode.isBlank()) {
                        errorMessage = "Please enter or paste an invite code"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val success = onJoin(inputCode)
                        isLoading = false
                        if (success) {
                            onDismissRequest()
                        } else {
                            errorMessage = "Could not find trip with this code. Make sure the organizer has opened the app."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .pressAnimation(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isLoading && inputCode.isNotBlank()
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Joining Journey...")
                } else {
                    Text("Join Journey", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NeoModernActiveTripCard(
    trip: Trip,
    members: List<Member>,
    totalSpent: Long,
    isPast: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // 2026 Liquid Gradient Mesh Palette: dynamic peach, coral, lavender & soft sky
    val liquidMeshBrush = remember(trip.id, isDark) {
        val baseColors = if (isDark) {
            when (Math.abs(trip.id.hashCode()) % 4) {
                0 -> listOf(Color(0xFF3B2043), Color(0xFF2C193D), Color(0xFF1E2148), Color(0xFF162544))
                1 -> listOf(Color(0xFF1B3237), Color(0xFF162A34), Color(0xFF1E2342), Color(0xFF271C3A))
                2 -> listOf(Color(0xFF382322), Color(0xFF351C2C), Color(0xFF261D3D), Color(0xFF17243B))
                else -> listOf(Color(0xFF252A42), Color(0xFF20233B), Color(0xFF321D3A), Color(0xFF261D32))
            }
        } else {
            when (Math.abs(trip.id.hashCode()) % 4) {
                0 -> listOf(Color(0xFFFFB5A7), Color(0xFFFCD5CE), Color(0xFFE2C4F6), Color(0xFFC7D2FE))
                1 -> listOf(Color(0xFFA7E8BD), Color(0xFFBAE6FD), Color(0xFFDDD6FE), Color(0xFFFBCFE8))
                2 -> listOf(Color(0xFFFED7AA), Color(0xFFFECDD3), Color(0xFFE9D5FF), Color(0xFFC7D2FE))
                else -> listOf(Color(0xFFBAE6FD), Color(0xFFC7D2FE), Color(0xFFFBCFE8), Color(0xFFFDE68A))
            }
        }
        Brush.linearGradient(
            colors = baseColors,
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )
    }

    val cardShape = RoundedCornerShape(26.dp)
    val outerBorder = BorderStroke(
        width = 1.dp,
        color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.65f)
    )

    val dateText = when {
        trip.startDate != null && trip.endDate != null ->
            "${Formatters.date(trip.startDate)} — ${Formatters.date(trip.endDate)}"
        trip.startDate != null -> "From ${Formatters.date(trip.startDate)}"
        trip.endDate != null -> "Until ${Formatters.date(trip.endDate)}"
        else -> Formatters.date(trip.createdAt)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressAnimation()
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color(0xFF9E77ED).copy(alpha = 0.2f),
                ambientColor = Color.Transparent
            )
            .clip(cardShape)
            .background(brush = liquidMeshBrush)
            .border(outerBorder, cardShape)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Date Pill & Active Glow Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frosted Date Pill
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.65f),
                    border = BorderStroke(
                        0.5.dp,
                        if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = dateText.uppercase(),
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                fontSize = 10.5.sp
                            ),
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B)
                        )
                    }
                }

                // Status Chip (Active or Settled)
                if (isPast) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.65f),
                        border = BorderStroke(
                            0.5.dp,
                            if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Settled",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isDark) Color(0xFF064E3B).copy(alpha = 0.6f) else Color(0xFFD1FAE5).copy(alpha = 0.85f),
                        border = BorderStroke(
                            0.5.dp,
                            if (isDark) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFF34D399).copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "Active",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF065F46)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Trip Destination Title
            Text(
                text = trip.name,
                style = Typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.4).sp,
                    fontSize = 25.sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Frosted Glass Bottom Shelf
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0xFF13151D).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.85f),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.9f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Overlapping Member Facepile
                    TripMemberFacepile(
                        members = members,
                        maxVisible = 3,
                        avatarSize = 30.dp
                    )

                    // Right: Total Pooled Amount
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "POOLED",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                fontSize = 9.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = Formatters.money(totalSpent),
                            style = Typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripMemberFacepile(
    members: List<Member>,
    maxVisible: Int = 3,
    avatarSize: Dp = 30.dp
) {
    if (members.isEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Solo Trip",
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val visibleMembers = members.take(maxVisible)
            val overflowCount = members.size - visibleMembers.size
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val borderColor = if (isDark) Color(0xFF1E222D) else Color.White

            Box(contentAlignment = Alignment.CenterStart) {
                visibleMembers.forEachIndexed { index, member ->
                    Box(
                        modifier = Modifier
                            .padding(start = (index * 20).dp)
                            .border(2.dp, borderColor, CircleShape)
                    ) {
                        LuxeMemberAvatar(
                            name = member.name,
                            size = avatarSize
                        )
                    }
                }
                if (overflowCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = (visibleMembers.size * 20).dp)
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, borderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$overflowCount",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val totalStartPadding = if (overflowCount > 0) 10.dp else 8.dp
            Spacer(modifier = Modifier.width(totalStartPadding))
            Text(
                text = "${members.size} ${if (members.size == 1) "member" else "members"}",
                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NeoModernPastTripCard(
    trip: Trip,
    members: List<Member>,
    totalSpent: Long,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val dateText = when {
        trip.startDate != null && trip.endDate != null ->
            "${Formatters.date(trip.startDate)} — ${Formatters.date(trip.endDate)}"
        trip.startDate != null -> "From ${Formatters.date(trip.startDate)}"
        trip.endDate != null -> "Until ${Formatters.date(trip.endDate)}"
        else -> Formatters.date(trip.createdAt)
    }

    val cardShape = RoundedCornerShape(20.dp)

    Surface(
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .pressAnimation()
            .clip(cardShape)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.name,
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Settled Badge Pill
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Settled",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = dateText,
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${members.size.coerceAtLeast(trip.memberIds.size)}",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = Formatters.money(totalSpent),
                    style = MoneyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AuroraEmptyStateView(
    filter: Int,
    onCreateTripClick: () -> Unit,
    onJoinTripClick: (() -> Unit)? = null
) {
    GlassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        cornerRadius = 20.dp,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AuroraVioletLight.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FlightTakeoff,
                    contentDescription = null,
                    tint = AuroraVioletLight,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            val title = when (filter) {
                1 -> "No active journeys"
                2 -> "No completed journeys"
                else -> "Ready for your next adventure?"
            }
            val subtitle = when (filter) {
                1 -> "You don't have any trips currently in progress."
                2 -> "Trips that have wrapped up will appear here."
                else -> "Create a trip to split expenses, invite friends, and settle seamlessly with UPI."
            }
            Text(
                text = title,
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (filter == 0) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCreateTripClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuroraVioletLight),
                        modifier = Modifier.pressAnimation()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Plan Trip", style = Typography.labelLarge)
                    }

                    if (onJoinTripClick != null) {
                        OutlinedButton(
                            onClick = onJoinTripClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.pressAnimation()
                        ) {
                            Icon(Icons.Outlined.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Join Trip", style = Typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
