package com.ljku.tripsplit.data

import android.content.Context
import com.ljku.tripsplit.domain.MemberRole
import com.ljku.tripsplit.domain.TripIdentity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _displayName = MutableStateFlow(prefs.getString(KEY_NAME, "") ?: "")
    val displayName: StateFlow<String> = _displayName

    private val _upiId = MutableStateFlow(prefs.getString(KEY_UPI, "") ?: "")
    val upiId: StateFlow<String> = _upiId

    private val _isNameLocked = MutableStateFlow(prefs.getBoolean(KEY_NAME_LOCKED, false))
    val isNameLocked: StateFlow<Boolean> = _isNameLocked

    private val _identityUpdateTrigger = MutableStateFlow(System.currentTimeMillis())
    val identityUpdateTrigger: StateFlow<Long> = _identityUpdateTrigger

    fun setDisplayName(value: String) {
        prefs.edit().putString(KEY_NAME, value).apply()
        _displayName.value = value
    }

    fun setNameLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_NAME_LOCKED, locked).apply()
        _isNameLocked.value = locked
    }

    fun setUpiId(value: String) {
        prefs.edit().putString(KEY_UPI, value).apply()
        _upiId.value = value
    }

    fun lastPayerId(tripId: String): String? = prefs.getString("$KEY_PAYER$tripId", null)

    fun setLastPayerId(tripId: String, memberId: String) {
        prefs.edit().putString("$KEY_PAYER$tripId", memberId).apply()
    }

    fun identityFor(tripId: String): TripIdentity? {
        val raw = prefs.getString("$KEY_ID$tripId", null) ?: return null
        return runCatching { json.decodeFromString<StoredIdentity>(raw) }.getOrNull()?.toDomain()
    }

    fun claim(identity: TripIdentity, memberName: String? = null, upi: String? = null) {
        prefs.edit().putString(
            "$KEY_ID${identity.tripId}",
            json.encodeToString(
                StoredIdentity(identity.tripId, identity.memberId, identity.role.name, identity.token)
            )
        ).apply()

        if (!memberName.isNullOrBlank()) {
            setDisplayName(memberName)
            setNameLocked(identity.role == MemberRole.MEMBER)
        }
        if (!upi.isNullOrBlank()) {
            setUpiId(upi)
        }
        _identityUpdateTrigger.value = System.currentTimeMillis()
    }

    fun isManager(
        tripId: String,
        members: List<com.ljku.tripsplit.domain.Member> = emptyList(),
        trip: com.ljku.tripsplit.domain.Trip? = null
    ): Boolean {
        val identity = identityFor(tripId)
        if (identity != null) {
            // Check if this identity belongs to the designated trip manager
            val isManagerMember = (trip?.managerMemberId != null && identity.memberId == trip.managerMemberId) ||
                    members.any { it.id == identity.memberId && it.role == MemberRole.MANAGER }

            if (identity.token == trip?.managerRecoveryToken || isManagerMember) {
                if (identity.role != MemberRole.MANAGER) {
                    claim(identity.copy(role = MemberRole.MANAGER, token = trip?.managerRecoveryToken ?: identity.token))
                }
                return true
            }

            // If the user holds a member invite token for a non-manager member, they are view-only
            if (trip != null && !trip.managerRecoveryToken.isNullOrBlank() && identity.token.isNotBlank()) {
                if (identity.token != trip.managerRecoveryToken && !isManagerMember) {
                    return false
                }
            }
            return identity.role == MemberRole.MANAGER
        }
        // If no explicit identity exists on this device yet:
        val myName = displayName.value.trim()
        if (myName.isNotBlank() && members.isNotEmpty()) {
            val manager = members.find { it.role == MemberRole.MANAGER }
                ?: (trip?.managerMemberId?.let { mid -> members.find { it.id == mid } })
            if (manager != null && manager.name.trim().equals(myName, ignoreCase = true)) {
                claim(
                    TripIdentity(tripId, manager.id, MemberRole.MANAGER, trip?.managerRecoveryToken ?: ""),
                    memberName = manager.name,
                    upi = manager.upiId
                )
                return true
            }
        }
        return false
    }

    fun currentMemberIdFor(
        tripId: String,
        members: List<com.ljku.tripsplit.domain.Member> = emptyList()
    ): String? {
        val identity = identityFor(tripId)
        if (identity != null && (members.isEmpty() || members.any { it.id == identity.memberId })) {
            if (_displayName.value.isBlank() && members.isNotEmpty()) {
                members.find { it.id == identity.memberId }?.let { m ->
                    setDisplayName(m.name)
                }
            }
            return identity.memberId
        }
        val myName = displayName.value.trim()
        if (myName.isNotBlank() && members.isNotEmpty()) {
            val match = members.find {
                it.name.trim().equals(myName, ignoreCase = true) ||
                it.name.replace(Regex("\\s*\\([Yy]ou\\)$"), "").trim().equals(myName, ignoreCase = true)
            }
            if (match != null) {
                claim(
                    TripIdentity(tripId, match.id, match.role, match.inviteToken),
                    memberName = match.name,
                    upi = match.upiId
                )
                return match.id
            }
        }
        if (isManager(tripId, members)) {
            val manager = members.find { it.role == MemberRole.MANAGER }
            if (manager != null) return manager.id
        }
        return identity?.memberId
    }

    fun viewerNameFor(
        tripId: String,
        members: List<com.ljku.tripsplit.domain.Member> = emptyList()
    ): String? {
        val mid = currentMemberIdFor(tripId, members)
        if (mid != null) {
            val name = members.find { it.id == mid }?.name
            if (!name.isNullOrBlank()) return name
        }
        return displayName.value.takeIf { it.isNotBlank() }
    }

    fun deviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing
        val newId = java.util.UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        return newId
    }

    companion object {
        private const val KEY_NAME = "display_name"
        private const val KEY_UPI = "upi_id"
        private const val KEY_PAYER = "last_payer_"
        private const val KEY_ID = "identity_"
        private const val KEY_NAME_LOCKED = "name_locked"
        private const val KEY_DEVICE_ID = "device_uuid"
    }
}

@Serializable
private data class StoredIdentity(
    val tripId: String,
    val memberId: String,
    val role: String,
    val token: String
) {
    fun toDomain() = TripIdentity(
        tripId = tripId,
        memberId = memberId,
        role = runCatching { MemberRole.valueOf(role) }.getOrDefault(MemberRole.MEMBER),
        token = token
    )
}
