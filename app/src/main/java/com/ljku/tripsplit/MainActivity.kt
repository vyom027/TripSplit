package com.ljku.tripsplit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.ljku.tripsplit.data.AppDatabase
import com.ljku.tripsplit.data.TripRepository
import com.ljku.tripsplit.data.UserPreferences
import com.ljku.tripsplit.domain.InviteLinks
import com.ljku.tripsplit.domain.TripIdentity
import com.ljku.tripsplit.sync.CloudSync
import com.ljku.tripsplit.sync.createCloudSync
import com.ljku.tripsplit.ui.AppNavigation
import com.ljku.tripsplit.ui.screens.SplashScreen
import com.ljku.tripsplit.ui.theme.LocalThemePreferences
import com.ljku.tripsplit.ui.theme.ThemePreferences
import com.ljku.tripsplit.ui.theme.TripSplitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: TripRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var cloudSync: CloudSync
    private var initialTripId by mutableStateOf<String?>(null)
    private var navTarget by mutableStateOf<Pair<String, Long>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tripsplit_db"
        ).addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5)
         .fallbackToDestructiveMigration()
         .build()

        repository = TripRepository(database.tripDao())
        userPreferences = UserPreferences(applicationContext)
        cloudSync = createCloudSync(applicationContext)

        handleInviteIntent(intent)

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            val themePreferences = remember { ThemePreferences(applicationContext) }
            val themeMode by themePreferences.themeMode.collectAsState()

            CompositionLocalProvider(LocalThemePreferences provides themePreferences) {
                TripSplitTheme(themeMode = themeMode) {
                    if (showSplash) {
                        SplashScreen(
                            onAnimationComplete = { showSplash = false }
                        )
                    } else {
                        AppNavigation(
                            repository = repository,
                            userPrefs = userPreferences,
                            sync = cloudSync,
                            initialTripId = initialTripId,
                            navTarget = navTarget
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::repository.isInitialized) {
            handleInviteIntent(intent)
        }
    }

    private fun handleInviteIntent(intent: Intent?) {
        val token = InviteLinks.parseToken(intent?.data) ?: return
        lifecycleScope.launch {
            if (cloudSync.available) {
                val joinResult = cloudSync.join(token)
                if (joinResult != null) {
                    val memberName = joinResult.claimedMemberName ?: "this member"
                    if (joinResult.alreadyClaimed) {
                        val currentIdentity = userPreferences.identityFor(joinResult.identity.tripId)
                        if (currentIdentity?.memberId == joinResult.identity.memberId) {
                            initialTripId = joinResult.identity.tripId
                            navTarget = joinResult.identity.tripId to System.currentTimeMillis()
                            Toast.makeText(applicationContext, "Opened ${joinResult.snapshot.trip.name} as $memberName", Toast.LENGTH_SHORT).show()
                            return@launch
                        } else {
                            Toast.makeText(applicationContext, "This invite has already been claimed by $memberName.", Toast.LENGTH_LONG).show()
                            return@launch
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
                    initialTripId = joinResult.identity.tripId
                    navTarget = joinResult.identity.tripId to System.currentTimeMillis()
                    Toast.makeText(applicationContext, "Joined ${joinResult.snapshot.trip.name} as ${joinedMember?.name ?: "Member"}${if (isManager) " (Manager)" else ""}!", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            val localMatch = repository.findByInviteToken(token)
            if (localMatch != null) {
                val (trip, member) = localMatch
                val isManager = trip.managerRecoveryToken == token ||
                                member.role == com.ljku.tripsplit.domain.MemberRole.MANAGER ||
                                trip.managerMemberId == member.id
                val currentIdentity = userPreferences.identityFor(trip.id)
                if (currentIdentity != null && currentIdentity.memberId.isNotBlank() && currentIdentity.memberId != member.id && !isManager) {
                    initialTripId = trip.id
                    navTarget = trip.id to System.currentTimeMillis()
                    Toast.makeText(applicationContext, "You are already joined as ${userPreferences.displayName.value} in ${trip.name}", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val role = if (isManager) com.ljku.tripsplit.domain.MemberRole.MANAGER else com.ljku.tripsplit.domain.MemberRole.MEMBER
                val effectiveToken = if (isManager && !trip.managerRecoveryToken.isNullOrBlank()) trip.managerRecoveryToken else token
                userPreferences.claim(TripIdentity(trip.id, member.id, role, effectiveToken), memberName = member.name, upi = member.upiId)
                initialTripId = trip.id
                navTarget = trip.id to System.currentTimeMillis()
                Toast.makeText(applicationContext, "Opened ${trip.name} as ${member.name}${if (isManager) " (Manager)" else ""}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            Toast.makeText(applicationContext, "Invite link could not be resolved", Toast.LENGTH_LONG).show()
        }
    }
}
