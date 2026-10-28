package com.ljku.tripsplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.ui.theme.Typography
import kotlin.math.absoluteValue

@Composable
fun AvatarGroup(
    members: List<Member>,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3,
    size: Dp = 32.dp,
    overlap: Dp = 8.dp
) {
    val visibleMembers = members.take(maxVisible)
    val remaining = members.size - maxVisible

    Row(modifier = modifier) {
        visibleMembers.forEachIndexed { index, member ->
            val color = remember(member.id) { getColorForId(member.id) }
            Box(
                modifier = Modifier
                    .offset(x = -(overlap * index))
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = member.name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                Text(
                    text = initials.ifBlank { "?" },
                    style = Typography.labelSmall,
                    color = Color.White
                )
            }
        }
        
        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .offset(x = -(overlap * maxVisible))
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remaining",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

val avatarColors = listOf(
    Color(0xFFE07A5F),
    Color(0xFF8A9A5B),
    Color(0xFF7C5CFC),
    Color(0xFF4ECDC4),
    Color(0xFFFF6B6B),
    Color(0xFFFFBE0B)
)

fun getColorForId(id: String): Color {
    val index = id.hashCode().absoluteValue % avatarColors.size
    return avatarColors[index]
}
