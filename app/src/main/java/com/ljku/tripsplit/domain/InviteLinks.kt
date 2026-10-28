package com.ljku.tripsplit.domain

import android.net.Uri
import java.util.UUID

object InviteLinks {
    const val HTTPS_HOST = "trip-split-f7805.web.app"
    const val APP_SCHEME = "tripsplit"

    fun newToken(): String =
        UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").take(8)

    fun httpsJoin(token: String): String = "https://$HTTPS_HOST/join-trip/$token"

    fun appJoin(token: String): String = "$APP_SCHEME://join-trip/$token"

    fun shareMessage(memberName: String, tripName: String, token: String): String =
        "✈️ TripSplit Invitation\n" +
        "You're invited to join \"$tripName\" as $memberName!\n\n" +
        "🔗 Tap to join:\n${httpsJoin(token)}\n\n" +
        "🔑 Trip Passcode: $token\n\n" +
        "(Open TripSplit, tap \"+ Join\" and paste the passcode or link)"

    fun allMembersShareMessage(tripName: String, members: List<Member>, managerName: String? = null): String {
        val sb = StringBuilder()
        sb.append("✈️ TripSplit — Join \"$tripName\"\n")
        if (!managerName.isNullOrBlank()) {
            val cleanManager = managerName.replace(Regex("\\s*\\([Yy]ou\\)$"), "").trim()
            sb.append("Organized by $cleanManager\n")
        }
        sb.append("\nTap your personal link below to join directly:\n\n")
        val invitees = members.filter { it.role != MemberRole.MANAGER }
        val listToShare = if (invitees.isNotEmpty()) invitees else members
        listToShare.forEach { m ->
            val cleanName = m.name.replace(Regex("\\s*\\([Yy]ou\\)$"), "").trim()
            val token = m.inviteToken.ifBlank { "pending" }
            sb.append("• $cleanName:\n  ${httpsJoin(token)}\n\n")
        }
        sb.append("Each link is unique so you'll be automatically recognized in the trip!")
        return sb.toString().trim()
    }

    fun parseToken(uri: Uri?): String? {
        if (uri == null) return null
        val host = uri.host ?: return null
        val path = uri.path ?: ""
        return when {
            uri.scheme == APP_SCHEME && (host == "join-trip" || host == "invite" || host == "join" || host == "j") ->
                uri.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: uri.lastPathSegment
            (uri.scheme == "https" || uri.scheme == "http") &&
                (host == HTTPS_HOST || host == "trip-split-f7805.web.app" || host == "trip-split-f7805.firebaseapp.com" || host == "tripsplit.app") &&
                (path.startsWith("/join-trip/") || path.startsWith("/invite/") || path.startsWith("/join/") || path.startsWith("/j/")) ->
                uri.lastPathSegment
            else -> null
        }
    }

    fun extractToken(input: String): String {
        val trimmed = input.trim()
        val uri = try { Uri.parse(trimmed) } catch (_: Exception) { null }
        val parsed = parseToken(uri)
        if (parsed != null && parsed.isNotBlank()) return parsed
        val knownPrefixes = listOf("/join-trip/", "/invite/", "/join/", "/j/")
        for (prefix in knownPrefixes) {
            if (trimmed.contains(prefix)) {
                return trimmed.substringAfterLast(prefix).substringBefore("?").substringBefore("/").substringBefore(" ").trim()
            }
        }
        // Extract 24-40 char hex token if whole message was pasted
        val hexMatch = Regex("[a-fA-F0-9]{24,40}").find(trimmed)
        if (hexMatch != null) return hexMatch.value
        return trimmed
    }
}
