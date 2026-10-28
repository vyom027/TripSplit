package com.ljku.tripsplit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.data.TripRepository
import com.ljku.tripsplit.data.UserPreferences
import com.ljku.tripsplit.domain.InviteLinks
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.domain.Transaction
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.domain.TripSummaryBuilder
import com.ljku.tripsplit.domain.toRequest
import com.ljku.tripsplit.sync.CloudSync
import com.ljku.tripsplit.sync.createCloudSync
import com.ljku.tripsplit.ui.screens.AddExpenseScreen
import com.ljku.tripsplit.ui.screens.EditTripScreen
import com.ljku.tripsplit.ui.screens.ExpenseDetailScreen
import com.ljku.tripsplit.ui.screens.ExpenseFormScreen
import com.ljku.tripsplit.ui.screens.MemberDetailScreen
import com.ljku.tripsplit.ui.screens.SettleUpScreen
import com.ljku.tripsplit.ui.screens.TripDashboardScreen
import com.ljku.tripsplit.ui.screens.TripListScreen
import com.ljku.tripsplit.ui.screens.TripSetupScreen
import com.ljku.tripsplit.ui.screens.ProfileScreen
import com.ljku.tripsplit.ui.screens.InsightsScreen
import com.ljku.tripsplit.ui.screens.TripStatsScreen
import com.ljku.tripsplit.ui.screens.MembersListScreen
import com.ljku.tripsplit.ui.screens.RequestInboxScreen
import com.ljku.tripsplit.ui.util.UpiPay
import com.ljku.tripsplit.ui.util.shareText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf

@Composable
fun AppNavigation(
    repository: TripRepository,
    userPrefs: UserPreferences? = null,
    sync: CloudSync? = null,
    initialTripId: String? = null,
    navTarget: Pair<String, Long>? = null
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPreferences = userPrefs ?: remember { UserPreferences(context) }
    val cloudSync = sync ?: remember { createCloudSync(context) }

    var hasCheckedActiveTrip by remember { mutableStateOf(false) }

    LaunchedEffect(navTarget) {
        if (navTarget != null) {
            val (targetTripId, _) = navTarget
            navController.navigate("tripDashboard/$targetTripId") {
                popUpTo("tripDashboard/$targetTripId") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(initialTripId) {
        // 1. Sync updates ONLY for existing local trips belonging to this device
        // 1. Pull & Restore Cloud Trips:
        try {
            val myName = userPreferences.displayName.value.trim()
            val localTrips = repository.getAllTrips().first()
            val isOwnerDevice = myName.equals("Vivek", ignoreCase = true) || localTrips.isEmpty()
            if (isOwnerDevice && cloudSync.available) {
                // Owner device restores all trips from Cloud Firestore
                val allRemoteSnaps = cloudSync.pullAllTrips()
                allRemoteSnaps.forEach { snap ->
                    repository.applySnapshot(snap)
                    val vivekMember = snap.members.find { it.name.trim().equals("Vivek", ignoreCase = true) }
                    if (vivekMember != null) {
                        userPreferences.claim(
                            com.ljku.tripsplit.domain.TripIdentity(
                                tripId = snap.trip.id,
                                memberId = vivekMember.id,
                                role = com.ljku.tripsplit.domain.MemberRole.MANAGER,
                                token = snap.trip.managerRecoveryToken ?: ""
                            ),
                            memberName = vivekMember.name,
                            upi = vivekMember.upiId
                        )
                    }
                }
            } else {
                // Member device: only pull updates for existing trips that were explicitly joined
                localTrips.forEach { trip ->
                    val snap = cloudSync.pull(trip.id)
                    if (snap != null) {
                        val localExpenses = repository.getExpensesForTrip(trip.id).first()
                        if (localExpenses.size < snap.expenses.size || snap.settlements.isNotEmpty()) {
                            repository.applySnapshot(snap)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore sync errors
        }

        if (initialTripId != null) {
            navController.navigate("tripDashboard/$initialTripId")
        } else if (!hasCheckedActiveTrip) {
            try {
                val trips = repository.getAllTrips().first()
                val activeTrip = trips.sortedByDescending { it.createdAt }
                    .firstOrNull { it.endDate == null || it.endDate >= System.currentTimeMillis() }
                if (activeTrip != null) {
                    navController.navigate("tripDashboard/${activeTrip.id}")
                }
            } catch (e: Exception) {
                // Ignore if flow is empty
            }
            hasCheckedActiveTrip = true
        }

        // Auto-sync local trips that have content to Cloud Firestore
        try {
            val allTrips = repository.getAllTrips().first()
            allTrips.forEach { trip ->
                repository.ensureInviteTokens(trip.id)
                val snap = repository.snapshot(trip.id)
                val isManager = userPreferences.isManager(trip.id, emptyList(), trip)
                if (isManager && snap != null && snap.expenses.isNotEmpty()) {
                    cloudSync.push(snap)
                }
            }
        } catch (_: Exception) {
            // Ignore background sync errors
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute in listOf("tripList", "insights", "profile")

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    com.ljku.tripsplit.ui.components.AuroraBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "tripList",
                modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(paddingValues)
            ) {
                composable("tripList") {
                    val trips by repository.getAllTrips().collectAsState(initial = emptyList())
                    val allExpenses by repository.getAllExpenses().collectAsState(initial = emptyList())
                    val allMembers by repository.getAllMembers().collectAsState(initial = emptyList())
                    TripListScreen(
                        trips = trips,
                        allExpenses = allExpenses,
                        allMembers = allMembers,
                        isTripManager = { tripId ->
                            val currentTrip = trips.find { it.id == tripId }
                            val tripMembers = allMembers.filter { it.tripId == tripId }
                            userPreferences.isManager(tripId, tripMembers, currentTrip)
                        },
                        onTripClick = { id -> navController.navigate("tripDashboard/$id") },
                        onCreateTripClick = { navController.navigate("tripSetup") },
                        onEditTripClick = { id -> navController.navigate("editTrip/$id") },
                        onDeleteTripClick = { id ->
                            scope.launch {
                                repository.deleteTrip(id)
                            }
                        },
                        onJoinTrip = { rawInput ->
                            val token = com.ljku.tripsplit.domain.InviteLinks.extractToken(rawInput)
                            if (token.isBlank()) {
                                false
                            } else {
                                val localMatch = repository.findByInviteToken(token)
                                if (localMatch != null) {
                                    val (trip, member) = localMatch
                                    val isManager = trip.managerRecoveryToken == token ||
                                                    member.role == com.ljku.tripsplit.domain.MemberRole.MANAGER ||
                                                    trip.managerMemberId == member.id
                                    val role = if (isManager) com.ljku.tripsplit.domain.MemberRole.MANAGER else com.ljku.tripsplit.domain.MemberRole.MEMBER
                                    val effectiveToken = if (isManager && !trip.managerRecoveryToken.isNullOrBlank()) trip.managerRecoveryToken else token
                                    userPreferences.claim(
                                        com.ljku.tripsplit.domain.TripIdentity(trip.id, member.id, role, effectiveToken),
                                        memberName = member.name,
                                        upi = member.upiId
                                    )
                                    navController.navigate("tripDashboard/${trip.id}") {
                                        launchSingleTop = true
                                    }
                                    true
                                } else if (cloudSync.available) {
                                    val joinResult = cloudSync.join(token)
                                    if (joinResult != null) {
                                        val memberName = joinResult.claimedMemberName ?: "this member"
                                        if (joinResult.alreadyClaimed) {
                                            val currentIdentity = userPreferences.identityFor(joinResult.identity.tripId)
                                            if (currentIdentity?.memberId == joinResult.identity.memberId) {
                                                navController.navigate("tripDashboard/${joinResult.identity.tripId}") {
                                                    launchSingleTop = true
                                                }
                                                return@TripListScreen true
                                            } else {
                                                android.widget.Toast.makeText(context, "This invite has already been claimed by $memberName.", android.widget.Toast.LENGTH_LONG).show()
                                                return@TripListScreen false
                                            }
                                        }
                                        repository.applySnapshot(joinResult.snapshot)
                                        val joinedMember = joinResult.snapshot.members.find { it.id == joinResult.identity.memberId }
                                        val isManager = joinResult.identity.role == com.ljku.tripsplit.domain.MemberRole.MANAGER ||
                                                        joinedMember?.role == com.ljku.tripsplit.domain.MemberRole.MANAGER ||
                                                        joinResult.snapshot.trip.managerMemberId == joinedMember?.id
                                        val role = if (isManager) com.ljku.tripsplit.domain.MemberRole.MANAGER else com.ljku.tripsplit.domain.MemberRole.MEMBER
                                        val effectiveToken = if (isManager && !joinResult.snapshot.trip.managerRecoveryToken.isNullOrBlank()) joinResult.snapshot.trip.managerRecoveryToken else joinResult.identity.token
                                        userPreferences.claim(
                                            joinResult.identity.copy(role = role, token = effectiveToken ?: joinResult.identity.token),
                                            memberName = joinedMember?.name,
                                            upi = joinedMember?.upiId
                                        )
                                        navController.navigate("tripDashboard/${joinResult.identity.tripId}") {
                                            launchSingleTop = true
                                        }
                                        true
                                    } else false
                                } else false
                            }
                        }
                    )
                }
                
                composable("insights") {
                    val trips by repository.getAllTrips().collectAsState(initial = emptyList())
                    val expenses by repository.getAllExpenses().collectAsState(initial = emptyList())
                    val allMembers by repository.getAllMembers().collectAsState(initial = emptyList())
                    InsightsScreen(
                        trips = trips,
                        expenses = expenses,
                        members = allMembers,
                        onTripClick = { id -> navController.navigate("tripDashboard/$id") }
                    )
                }

                composable("profile") {
                    val displayName by userPreferences.displayName.collectAsState()
                    val upiId by userPreferences.upiId.collectAsState()
                    val isNameLocked by userPreferences.isNameLocked.collectAsState()
                    val trips by repository.getAllTrips().collectAsState(initial = emptyList())
                    val allExpenses by repository.getAllExpenses().collectAsState(initial = emptyList())
                    val allMembers by repository.getAllMembers().collectAsState(initial = emptyList())
                    val distinctFriends = remember(allMembers) {
                        val count = allMembers.map { it.name.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size
                        if (count > 0) count else 1
                    }

                    ProfileScreen(
                        displayName = displayName,
                        upiId = upiId,
                        isNameLocked = isNameLocked,
                        tripCount = trips.size,
                        friendsCount = distinctFriends,
                        totalExpensesCount = allExpenses.size,
                        onDisplayNameChange = { userPreferences.setDisplayName(it) },
                        onUpiChange = { newUpi ->
                            userPreferences.setUpiId(newUpi)
                            scope.launch {
                                val allTrips = repository.getAllTrips().first()
                                allTrips.forEach { trip ->
                                    val identity = userPreferences.identityFor(trip.id)
                                    if (identity != null) {
                                        val member = repository.getMemberById(identity.memberId)
                                        if (member != null && member.upiId != newUpi) {
                                            repository.saveMember(member.copy(upiId = newUpi.trim()))
                                            val snap = repository.snapshot(trip.id)
                                            if (snap != null) cloudSync.push(snap)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                composable("tripSetup") {
                    val defaultManagerUpi by userPreferences.upiId.collectAsState()
                    val creatorName by userPreferences.displayName.collectAsState()
                    TripSetupScreen(
                        creatorName = creatorName,
                        onSaveTrip = { trip: Trip, members: List<Member> ->
                            scope.launch {
                                val managerUpi = defaultManagerUpi.trim().ifBlank { null }
                                val withUpi = if (managerUpi != null) {
                                    members.mapIndexed { idx, m -> if (idx == 0 && m.upiId.isNullOrBlank()) m.copy(upiId = managerUpi) else m }
                                } else members
                                repository.createTrip(trip, withUpi)
                                val manager = withUpi.firstOrNull { it.role == com.ljku.tripsplit.domain.MemberRole.MANAGER } ?: withUpi.firstOrNull()
                                val managerToken = trip.managerRecoveryToken ?: com.ljku.tripsplit.domain.InviteLinks.newToken()
                                if (manager != null) {
                                    userPreferences.claim(
                                        com.ljku.tripsplit.domain.TripIdentity(trip.id, manager.id, com.ljku.tripsplit.domain.MemberRole.MANAGER, managerToken),
                                        memberName = manager.name,
                                        upi = manager.upiId
                                    )
                                }
                                val snap = repository.snapshot(trip.id)
                                if (snap != null) cloudSync.push(snap)
                                navController.popBackStack()
                                navController.navigate("tripDashboard/${trip.id}")
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("tripDashboard/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val expenses by repository.getExpensesForTrip(tripId).collectAsState(initial = emptyList())
                    val settlements by repository.getSettlementsForTrip(tripId).collectAsState(initial = emptyList())
                    val requests by repository.getRequestsForTrip(tripId).collectAsState(initial = emptyList())
                    val identityUpdateTrigger by userPreferences.identityUpdateTrigger.collectAsState()
                    val isManager = remember(tripId, members, trip, identityUpdateTrigger) {
                        userPreferences.isManager(tripId, members, trip)
                    }
                    val currentMemberId = remember(tripId, members, identityUpdateTrigger) {
                        userPreferences.currentMemberIdFor(tripId, members)
                    }
                    val viewerName = remember(tripId, members, identityUpdateTrigger) {
                        userPreferences.viewerNameFor(tripId, members)
                    }

                    trip?.let { currentTrip ->
                        TripDashboardScreen(
                            trip = currentTrip,
                            members = members,
                            expenses = expenses,
                            settlements = settlements,
                            pendingRequests = requests,
                            isManager = isManager,
                            viewerName = viewerName,
                            currentMemberId = currentMemberId,
                            onAddExpense = { navController.navigate("addExpense/$tripId") },
                            onRequestExpense = { navController.navigate("requestExpense/$tripId") },
                            onSubmitExpenseRequest = { req ->
                                scope.launch {
                                    repository.saveRequest(req)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onRequestSettlement = { tx ->
                                scope.launch {
                                    val identity = userPreferences.identityFor(tripId)
                                    val fromName = members.find { it.id == tx.fromMemberId }?.name ?: "Someone"
                                    val toName = members.find { it.id == tx.toMemberId }?.name ?: "Someone"
                                    val req = com.ljku.tripsplit.domain.ExpenseRequest(
                                        id = java.util.UUID.randomUUID().toString(),
                                        tripId = tripId,
                                        fromMemberId = identity?.memberId ?: tx.fromMemberId,
                                        description = "$fromName settled with $toName",
                                        totalAmount = tx.amount,
                                        date = System.currentTimeMillis(),
                                        paidBy = listOf(com.ljku.tripsplit.domain.PaidShare(tx.fromMemberId, tx.amount)),
                                        splitAmong = listOf(com.ljku.tripsplit.domain.SplitShare(tx.toMemberId, tx.amount)),
                                        splitMode = com.ljku.tripsplit.domain.SplitMode.EXACT,
                                        category = "Settlement"
                                    )
                                    repository.saveRequest(req)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onApproveRequest = { req ->
                                scope.launch {
                                    repository.approveRequestDirectly(req)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onRejectRequest = { reqId ->
                                scope.launch {
                                    repository.rejectRequest(reqId)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onOpenRequests = { navController.navigate("requestInbox/$tripId") },
                            onSettleUp = { navController.navigate("settleUp/$tripId") },
                            onQuickSettle = { memberId -> navController.navigate("settleUp/$tripId?memberId=$memberId") },
                            onEditTrip = { navController.navigate("editTrip/$tripId") },
                            onShare = {
                                shareText(
                                    context,
                                    TripSummaryBuilder.buildShareText(currentTrip, members, expenses, settlements),
                                    "${currentTrip.name} summary"
                                )
                            },
                            onMemberClick = { memberId ->
                                navController.navigate("memberDetail/$tripId/$memberId")
                            },
                            onExpenseClick = { expenseId ->
                                navController.navigate("expenseDetail/$tripId/$expenseId")
                            },
                            onEditExpense = { expenseId ->
                                navController.navigate("editExpense/$tripId/$expenseId")
                            },
                            onDeleteExpense = { expenseId ->
                                scope.launch {
                                    repository.deleteExpense(expenseId)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onUndoDeleteExpense = { expense ->
                                scope.launch {
                                    repository.saveExpense(expense)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onMarkSettled = { tx ->
                                scope.launch {
                                    if (!userPreferences.isManager(tripId, members, currentTrip)) return@launch
                                    repository.recordSettlement(
                                        repository.newSettlement(
                                            tripId = tripId,
                                            fromMemberId = tx.fromMemberId,
                                            toMemberId = tx.toMemberId,
                                            amount = tx.amount
                                        )
                                    )
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onBack = { navController.popBackStack() },
                            onStatsClick = { navController.navigate("tripStats/$tripId") },
                            onMembersClick = { navController.navigate("membersList/$tripId") }
                        )
                    }
                }

                composable("tripStats/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val expenses by repository.getExpensesForTrip(tripId).collectAsState(initial = emptyList())
                    val settlements by repository.getSettlementsForTrip(tripId).collectAsState(initial = emptyList())

                    trip?.let { currentTrip ->
                        TripStatsScreen(
                            trip = currentTrip,
                            members = members,
                            expenses = expenses,
                            settlements = settlements,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("editTrip/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())

                    trip?.let { currentTrip ->
                        EditTripScreen(
                            trip = currentTrip,
                            members = members,
                            onSave = { updatedTrip, updatedMembers, removedIds ->
                                scope.launch {
                                    repository.updateTripWithMembers(updatedTrip, updatedMembers, removedIds)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    navController.popBackStack()
                                }
                            },
                            onDeleteTrip = {
                                scope.launch {
                                    repository.deleteTrip(tripId)
                                    navController.navigate("tripList") {
                                        popUpTo("tripList") { inclusive = true }
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("addExpense/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val currentMemberId = userPreferences.currentMemberIdFor(tripId, members)
                    val viewerName = userPreferences.viewerNameFor(tripId, members)
                    val defaultPayer = userPreferences.lastPayerId(tripId) ?: currentMemberId ?: trip?.managerMemberId

                    AddExpenseScreen(
                        tripId = tripId,
                        members = members,
                        defaultPayerId = defaultPayer,
                        currentMemberId = currentMemberId,
                        viewerName = viewerName,
                        asRequest = false,
                        onSaveExpense = { expense ->
                            scope.launch {
                                repository.addExpense(expense)
                                expense.paidBy.firstOrNull()?.let {
                                    userPreferences.setLastPayerId(tripId, it.memberId)
                                }
                                val snap = repository.snapshot(tripId)
                                if (snap != null) cloudSync.push(snap)
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("editExpense/{tripId}/{expenseId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val expense by repository.getExpenseById(expenseId).collectAsState(initial = null)

                    expense?.let { existing ->
                        ExpenseFormScreen(
                            tripId = tripId,
                            members = members,
                            existingExpense = existing,
                            onSaveExpense = { updated ->
                                scope.launch {
                                    repository.saveExpense(updated)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    navController.popBackStack()
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("expenseDetail/{tripId}/{expenseId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val expenseId = backStackEntry.arguments?.getString("expenseId") ?: return@composable
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val expense by repository.getExpenseById(expenseId).collectAsState(initial = null)

                    val canManage = userPreferences.isManager(tripId, members, trip)

                    expense?.let { currentExpense ->
                        ExpenseDetailScreen(
                            expense = currentExpense,
                            members = members,
                            canManage = canManage,
                            onEdit = { navController.navigate("editExpense/$tripId/$expenseId") },
                            onDelete = {
                                scope.launch {
                                    if (!userPreferences.isManager(tripId, members, trip)) return@launch
                                    repository.deleteExpense(expenseId)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    navController.popBackStack()
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("memberDetail/{tripId}/{memberId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val memberId = backStackEntry.arguments?.getString("memberId") ?: return@composable
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val expenses by repository.getExpensesForTrip(tripId).collectAsState(initial = emptyList())
                    val settlements by repository.getSettlementsForTrip(tripId).collectAsState(initial = emptyList())
                    val member = members.find { it.id == memberId }
                    val currentMemberId = userPreferences.currentMemberIdFor(tripId, members)
                    val viewerName = userPreferences.viewerNameFor(tripId, members)

                    member?.let { currentMember ->
                        MemberDetailScreen(
                            member = currentMember,
                            members = members,
                            expenses = expenses,
                            settlements = settlements,
                            canManage = userPreferences.isManager(tripId, members, trip),
                            currentMemberId = currentMemberId,
                            viewerName = viewerName,
                            onEditExpense = { expenseId ->
                                navController.navigate("editExpense/$tripId/$expenseId")
                            },
                            onDeleteExpense = { expenseId ->
                                scope.launch {
                                    if (!userPreferences.isManager(tripId, members, trip)) return@launch
                                    repository.deleteExpense(expenseId)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("membersList/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val expenses by repository.getExpensesForTrip(tripId).collectAsState(initial = emptyList())
                    val settlements by repository.getSettlementsForTrip(tripId).collectAsState(initial = emptyList())
                    val identityUpdateTrigger by userPreferences.identityUpdateTrigger.collectAsState()
                    val isManager = remember(tripId, members, trip, identityUpdateTrigger) {
                        userPreferences.isManager(tripId, members, trip)
                    }
                    val currentMemberId = remember(tripId, members, identityUpdateTrigger) {
                        userPreferences.currentMemberIdFor(tripId, members)
                    }
                    val viewerName = remember(tripId, members, identityUpdateTrigger) {
                        userPreferences.viewerNameFor(tripId, members)
                    }

                    LaunchedEffect(tripId) {
                        repository.ensureInviteTokens(tripId)
                    }

                    trip?.let { currentTrip ->
                        MembersListScreen(
                            trip = currentTrip,
                            members = members,
                            expenses = expenses,
                            settlements = settlements,
                            isManager = isManager,
                            currentMemberId = currentMemberId,
                            viewerName = viewerName,
                            onMemberClick = { memberId -> navController.navigate("memberDetail/$tripId/$memberId") },
                            onQuickSettle = { memberId -> navController.navigate("settleUp/$tripId?memberId=$memberId") },
                            onMarkSettled = { tx ->
                                scope.launch {
                                    if (!userPreferences.isManager(tripId, members, currentTrip)) return@launch
                                    repository.recordSettlement(
                                        repository.newSettlement(
                                            tripId = tripId,
                                            fromMemberId = tx.fromMemberId,
                                            toMemberId = tx.toMemberId,
                                            amount = tx.amount
                                        )
                                    )
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onRequestSettlement = { tx ->
                                scope.launch {
                                    val identity = userPreferences.identityFor(tripId)
                                    val fromName = members.find { it.id == tx.fromMemberId }?.name ?: "Someone"
                                    val toName = members.find { it.id == tx.toMemberId }?.name ?: "Someone"
                                    val req = com.ljku.tripsplit.domain.ExpenseRequest(
                                        id = java.util.UUID.randomUUID().toString(),
                                        tripId = tripId,
                                        fromMemberId = identity?.memberId ?: tx.fromMemberId,
                                        description = "$fromName settled with $toName",
                                        totalAmount = tx.amount,
                                        date = System.currentTimeMillis(),
                                        paidBy = listOf(com.ljku.tripsplit.domain.PaidShare(tx.fromMemberId, tx.amount)),
                                        splitAmong = listOf(com.ljku.tripsplit.domain.SplitShare(tx.toMemberId, tx.amount)),
                                        splitMode = com.ljku.tripsplit.domain.SplitMode.EXACT,
                                        category = "Settlement"
                                    )
                                    repository.saveRequest(req)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onAddMemberClick = { navController.navigate("editTrip/$tripId") },
                            onShareMemberLink = { member ->
                                scope.launch {
                                    repository.ensureInviteTokens(tripId)
                                    val fresh = repository.getMemberById(member.id) ?: member
                                    val token = fresh.inviteToken.ifBlank {
                                        val gen = InviteLinks.newToken()
                                        repository.saveMember(fresh.copy(inviteToken = gen))
                                        gen
                                    }
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    val msg = InviteLinks.shareMessage(member.name, currentTrip.name, token)
                                    shareText(context, msg, "Invite ${member.name}")
                                }
                            },
                            onRegenerateMember = { member ->
                                scope.launch {
                                    repository.regenerateMemberToken(member)
                                    cloudSync.resetClaim(tripId, member.id)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onSaveUpi = { member, upi ->
                                scope.launch {
                                    repository.saveMember(member.copy(upiId = upi))
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onShareRecovery = {
                                val token = currentTrip.managerRecoveryToken ?: return@MembersListScreen
                                scope.launch {
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    val msg = "Manager recovery link for ${currentTrip.name}:\n${InviteLinks.httpsJoin(token)}"
                                    shareText(context, msg, "Manager Recovery")
                                }
                            },
                            onRegenerateRecovery = {
                                scope.launch {
                                    repository.regenerateManagerRecovery(currentTrip)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                }
                            },
                            onShareAllLinks = {
                                scope.launch {
                                    repository.ensureInviteTokens(tripId)
                                    val freshMembers = repository.getMembersForTripOnce(tripId)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    val managerName = freshMembers.find { it.role == com.ljku.tripsplit.domain.MemberRole.MANAGER || it.id == currentTrip.managerMemberId }?.name
                                    val msg = InviteLinks.allMembersShareMessage(currentTrip.name, freshMembers, managerName)
                                    shareText(context, msg, "Invite Links for ${currentTrip.name}")
                                }
                            },
                            onCopyAllLinks = {
                                scope.launch {
                                    repository.ensureInviteTokens(tripId)
                                    val freshMembers = repository.getMembersForTripOnce(tripId)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    val managerName = freshMembers.find { it.role == com.ljku.tripsplit.domain.MemberRole.MANAGER || it.id == currentTrip.managerMemberId }?.name
                                    val msg = InviteLinks.allMembersShareMessage(currentTrip.name, freshMembers, managerName)
                                    UpiPay.copy(context, msg, "All invite links copied")
                                    android.widget.Toast.makeText(context, "Copied all member invite links!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("settleUp/{tripId}?memberId={memberId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val memberId = backStackEntry.arguments?.getString("memberId")
                    val trip by repository.getTripDetails(tripId).collectAsState(initial = null)
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val expenses by repository.getExpensesForTrip(tripId).collectAsState(initial = emptyList())
                    val settlements by repository.getSettlementsForTrip(tripId).collectAsState(initial = emptyList())
                    val balances = SettlementEngine.calculateBalances(members, expenses, settlements)
                    val canManage = userPreferences.isManager(tripId, members, trip)
                    val currentMemberId = userPreferences.currentMemberIdFor(tripId, members)
                    val viewerName = userPreferences.viewerNameFor(tripId, members)

                    SettleUpScreen(
                        balances = balances,
                        members = members,
                        settlements = settlements,
                        preselectedMemberId = memberId,
                        canManage = canManage,
                        currentMemberId = currentMemberId,
                        viewerName = viewerName,
                        onMarkSettled = { transaction ->
                            scope.launch {
                                if (!userPreferences.isManager(tripId, members, trip)) return@launch
                                repository.recordSettlement(
                                    repository.newSettlement(
                                        tripId = tripId,
                                        fromMemberId = transaction.fromMemberId,
                                        toMemberId = transaction.toMemberId,
                                        amount = transaction.amount
                                    )
                                )
                                val snap = repository.snapshot(tripId)
                                if (snap != null) cloudSync.push(snap)
                            }
                        },
                        onUndoSettlement = { settlementId ->
                            scope.launch {
                                if (!userPreferences.isManager(tripId, members, trip)) return@launch
                                repository.deleteSettlement(settlementId)
                                val snap = repository.snapshot(tripId)
                                if (snap != null) cloudSync.push(snap)
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("requestExpense/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val currentMemberId = userPreferences.currentMemberIdFor(tripId, members)
                    val viewerName = userPreferences.viewerNameFor(tripId, members)

                    AddExpenseScreen(
                        tripId = tripId,
                        members = members,
                        defaultPayerId = currentMemberId ?: members.firstOrNull()?.id,
                        currentMemberId = currentMemberId,
                        viewerName = viewerName,
                        asRequest = true,
                        onSaveExpense = { expense ->
                            scope.launch {
                                val req = expense.toRequest(fromMemberId = currentMemberId ?: expense.paidBy.firstOrNull()?.memberId ?: "")
                                repository.saveRequest(req)
                                val snap = repository.snapshot(tripId)
                                if (snap != null) cloudSync.push(snap)
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("requestInbox/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val requests by repository.getRequestsForTrip(tripId).collectAsState(initial = emptyList())
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())

                    RequestInboxScreen(
                        requests = requests,
                        members = members,
                        onOpen = { requestId -> navController.navigate("reviewRequest/$tripId/$requestId") },
                        onApprove = { request ->
                            scope.launch {
                                repository.approveRequestDirectly(request)
                                val snap = repository.snapshot(tripId)
                                if (snap != null) cloudSync.push(snap)
                            }
                        },
                        onReject = { requestId ->
                            scope.launch {
                                repository.rejectRequest(requestId)
                                val snap = repository.snapshot(tripId)
                                if (snap != null) cloudSync.push(snap)
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("reviewRequest/{tripId}/{requestId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                    val requestId = backStackEntry.arguments?.getString("requestId") ?: return@composable
                    val members by repository.getMembersForTrip(tripId).collectAsState(initial = emptyList())
                    val request by repository.getRequestById(requestId).collectAsState(initial = null)

                    request?.let { req ->
                        ExpenseFormScreen(
                            tripId = tripId,
                            members = members,
                            existingExpense = req.toExpense(),
                            asRequest = false,
                            onSaveExpense = { updatedExpense ->
                                scope.launch {
                                    repository.approveRequest(req, updatedExpense)
                                    val snap = repository.snapshot(tripId)
                                    if (snap != null) cloudSync.push(snap)
                                    navController.popBackStack()
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
