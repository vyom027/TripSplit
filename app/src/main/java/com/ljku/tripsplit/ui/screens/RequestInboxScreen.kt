package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.domain.ExpenseRequest
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.theme.BalancePositive
import com.ljku.tripsplit.ui.theme.MoneyMedium
import com.ljku.tripsplit.ui.theme.SectionTag
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestInboxScreen(
    requests: List<ExpenseRequest>,
    members: List<Member>,
    onOpen: (String) -> Unit,
    onApprove: (ExpenseRequest) -> Unit = {},
    onReject: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Pending Requests", style = Typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (requests.isEmpty()) {
                item {
                    Text("No pending requests.", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(requests, key = { it.id }) { request ->
                val from = members.find { it.id == request.fromMemberId }?.name ?: "Someone"
                val isSettlement = request.category == "Settlement"
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                color = if (isSettlement) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isSettlement) "SETTLEMENT REQUEST" else "EXPENSE REQUEST",
                                    style = SectionTag.copy(fontSize = 10.sp),
                                    color = if (isSettlement) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                Formatters.money(request.totalAmount),
                                style = MoneyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(request.description, style = Typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            "Requested by $from · ${Formatters.date(request.date)}",
                            style = Typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { onReject(request.id) }) {
                                Text("Reject", color = MaterialTheme.colorScheme.error)
                            }
                            if (!isSettlement) {
                                TextButton(onClick = { onOpen(request.id) }) { Text("Review") }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Button(
                                onClick = { onApprove(request) },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = BalancePositive,
                                    contentColor = Color.White
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ) {
                                Text("Accept")
                            }
                        }
                    }
                }
            }
        }
    }
}
