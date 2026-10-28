package com.ljku.tripsplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.Transaction
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters

@Composable
fun SettlementCard(
    transaction: Transaction,
    members: List<Member>,
    modifier: Modifier = Modifier
) {
    val fromMember = members.find { it.id == transaction.fromMemberId }
    val toMember = members.find { it.id == transaction.toMemberId }
    val fromName = fromMember?.name ?: "Someone"
    val toName = toMember?.name ?: "Someone"

    GlassyCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        elevation = 0.dp
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
                modifier = Modifier.weight(1f)
            ) {
                LuxeMemberAvatar(name = fromName, size = 30.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = fromName,
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "pays",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 8.dp).size(14.dp)
                )
                LuxeMemberAvatar(name = toName, size = 30.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = toName,
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = Formatters.money(transaction.amount),
                style = Typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
