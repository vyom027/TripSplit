package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.Settlement
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.domain.Transaction
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.components.LuxeMemberAvatar
import com.ljku.tripsplit.ui.components.pressAnimation
import com.ljku.tripsplit.ui.theme.AuroraCoralLight
import com.ljku.tripsplit.ui.theme.AuroraEmeraldLight
import com.ljku.tripsplit.ui.theme.AuroraVioletGradient
import com.ljku.tripsplit.ui.theme.AuroraVioletLight
import com.ljku.tripsplit.ui.theme.MoneyLarge
import com.ljku.tripsplit.ui.theme.MoneyMedium
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters
import com.ljku.tripsplit.ui.util.UpiPay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    balances: Map<String, Long>,
    members: List<Member>,
    settlements: List<Settlement>,
    preselectedMemberId: String? = null,
    canManage: Boolean = true,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onMarkSettled: (Transaction) -> Unit,
    onUndoSettlement: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var partialFor by remember { mutableStateOf<Transaction?>(null) }
    var partialText by remember { mutableStateOf("") }
    val memberMap = remember(members) { members.associateBy { it.id } }

    val pending = remember(balances, preselectedMemberId) {
        val allDebts = SettlementEngine.simplifyDebts(balances)
        if (preselectedMemberId != null) {
            allDebts.filter { it.fromMemberId == preselectedMemberId || it.toMemberId == preselectedMemberId }
        } else {
            allDebts
        }
    }

    val celebrationParties = remember {
        listOf(
            Party(
                speed = 0f,
                maxSpeed = 28f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xC8F530, 0x10B981, 0xF59E0B, 0x06B6D4),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(90),
                position = Position.Relative(0.5, 0.25)
            )
        )
    }

    partialFor?.let { tx ->
        val fromName = memberMap[tx.fromMemberId]?.name ?: "Member"
        val toName = memberMap[tx.toMemberId]?.name ?: "Member"

        ModalBottomSheet(
            onDismissRequest = { partialFor = null },
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
                    .imePadding()
                    .padding(bottom = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Record Partial Payment",
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$fromName paying $toName",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Total outstanding debt is ${Formatters.money(tx.amount)}. Enter the partial amount paid below to update the ledger.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = partialText,
                    onValueChange = { partialText = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { partialFor = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            val rupees = partialText.toDoubleOrNull() ?: 0.0
                            val paise = (rupees * 100).toLong().coerceIn(1L, tx.amount)
                            onMarkSettled(tx.copy(amount = paise))
                            partialFor = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settle Up",
                            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "SUGGESTED SETTLEMENTS",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (pending.isEmpty()) {
                    item {
                        GlassyCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            elevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(AuroraEmeraldLight.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = AuroraEmeraldLight,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "All Settled Up!",
                                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "No outstanding balances remain among trip members.",
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(pending, key = { "${it.fromMemberId}_${it.toMemberId}" }) { tx ->
                        val from = members.find { it.id == tx.fromMemberId }
                        val to = members.find { it.id == tx.toMemberId }
                        val fromName = from?.displayName(currentMemberId, viewerName) ?: "?"
                        val toName = to?.displayName(currentMemberId, viewerName) ?: "?"

                        GlassyCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            elevation = 0.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // From -> To Avatar Flow
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LuxeMemberAvatar(name = fromName, size = 38.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = fromName,
                                                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Pays",
                                                style = Typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                        contentDescription = "pays",
                                        tint = AuroraVioletLight,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = toName,
                                                style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Receives",
                                                style = Typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        LuxeMemberAvatar(name = toName, size = 38.dp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Amount Display & UPI Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Formatters.money(tx.amount),
                                        style = MoneyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    to?.upiId?.takeIf { it.isNotBlank() }?.let { upi ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Text(
                                                text = "UPI: $upi",
                                                style = Typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action buttons
                                val payUpi = to?.upiId
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!payUpi.isNullOrBlank()) {
                                        Button(
                                            onClick = {
                                                UpiPay.pay(context, payUpi, to.name, tx.amount, "TripSplit settle")
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.pressAnimation()
                                        ) {
                                            Icon(Icons.Outlined.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pay UPI", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                        OutlinedButton(
                                            onClick = { UpiPay.copy(context, payUpi) },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Copy", style = Typography.labelMedium)
                                        }
                                    }
                                    if (canManage) {
                                        Button(
                                            onClick = { onMarkSettled(tx) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.pressAnimation()
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Mark Paid", style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                                        }
                                        TextButton(onClick = {
                                            partialText = (tx.amount / 100.0).toString()
                                            partialFor = tx
                                        }) {
                                            Text("Partial", style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        "SETTLEMENT HISTORY",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (settlements.isEmpty()) {
                    item {
                        Text(
                            "No past settlements recorded yet.",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(settlements, key = { it.id }) { settlement ->
                        val from = members.find { it.id == settlement.fromMemberId }?.name ?: "?"
                        val to = members.find { it.id == settlement.toMemberId }?.name ?: "?"
                        GlassyCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 16.dp,
                            elevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = AuroraEmeraldLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "$from → $to",
                                            style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        Formatters.dateTime(settlement.settledAt),
                                        style = Typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (canManage) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        TextButton(
                                            onClick = { onUndoSettlement(settlement.id) },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Undo settlement", style = Typography.labelSmall, color = AuroraCoralLight)
                                        }
                                    }
                                }
                                Text(
                                    Formatters.money(settlement.amount),
                                    style = MoneyMedium,
                                    color = AuroraEmeraldLight
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(28.dp)) }
            }
        }

        // Celebration Confetti
        if (pending.isEmpty() && settlements.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = celebrationParties
            )
        }
    }
}
