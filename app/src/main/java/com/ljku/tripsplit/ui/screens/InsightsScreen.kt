package com.ljku.tripsplit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.domain.Expense
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.SpendTotals
import com.ljku.tripsplit.domain.Trip
import com.ljku.tripsplit.ui.components.AnimatedCountUp
import com.ljku.tripsplit.ui.components.GlassyCard
import com.ljku.tripsplit.ui.components.LuxeMemberAvatar
import com.ljku.tripsplit.ui.components.pressAnimation
import com.ljku.tripsplit.ui.theme.AuroraVioletLight
import com.ljku.tripsplit.ui.theme.MoneyMedium
import com.ljku.tripsplit.ui.theme.Typography
import com.ljku.tripsplit.ui.util.Formatters

private data class ContributorStat(
    val name: String,
    val totalPaid: Long,
    val expenseCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    trips: List<Trip>,
    expenses: List<Expense>,
    members: List<Member> = emptyList(),
    onTripClick: ((String) -> Unit)? = null
) {
    val totalSpent = remember(expenses) { SpendTotals.group(expenses) }
    val personalSpent = remember(expenses) { SpendTotals.personal(expenses) }
    val totalTrips = trips.size
    val totalExpenses = expenses.size

    val categoryTotals = remember(expenses) {
        expenses.filter { !it.isPersonal }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- 2026 Neo-Modern Editorial Header ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Financial Insights",
                        style = Typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                                text = "$totalTrips journeys • ${Formatters.money(totalSpent)} lifetime spend",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- 2026 Neo-Modern Hero Spend Card ---
            item {
                InsightsHeroCard(
                    totalSpent = totalSpent,
                    personalSpent = personalSpent,
                    totalTrips = totalTrips,
                    totalExpenses = totalExpenses
                )
            }

            if (expenses.isNotEmpty()) {
                // --- Unified Category Donut Card (Zero Duplication) ---
                if (categoryTotals.isNotEmpty()) {
                    item {
                        UnifiedCategoryDonutCard(
                            categoryTotals = categoryTotals,
                            totalSpent = totalSpent
                        )
                    }
                }

                // --- Top Contributors Spotlight ---
                item {
                    TopContributorsSection(
                        expenses = expenses,
                        members = members
                    )
                }

                // --- Spend by Journey Comparison ---
                if (trips.isNotEmpty()) {
                    item {
                        SpendByJourneySection(
                            trips = trips,
                            expenses = expenses,
                            totalSpent = totalSpent,
                            onTripClick = onTripClick
                        )
                    }
                }

                // --- Largest High-Ticket Expenses ---
                item {
                    LargestExpensesSection(
                        expenses = expenses,
                        members = members
                    )
                }
            } else {
                item {
                    GlassyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
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
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(AuroraVioletLight.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Insights,
                                    contentDescription = null,
                                    tint = AuroraVioletLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No expenses recorded yet",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Log expenses in your journeys to see category distributions, top contributors, and journey benchmarks.",
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun InsightsHeroCard(
    totalSpent: Long,
    personalSpent: Long,
    totalTrips: Int,
    totalExpenses: Int
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val liquidMeshBrush = remember(isDark) {
        val colors = if (isDark) {
            listOf(Color(0xFF3B2043), Color(0xFF2C193D), Color(0xFF1E2148), Color(0xFF162544))
        } else {
            listOf(Color(0xFFFFB5A7), Color(0xFFFCD5CE), Color(0xFFE2C4F6), Color(0xFFC7D2FE))
        }
        Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )
    }

    val cardShape = RoundedCornerShape(26.dp)

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
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.65f),
                cardShape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Tag & Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            imageVector = Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "LIFETIME GROUP SPENT",
                            style = Typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 10.sp
                            ),
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$totalTrips trips • $totalExpenses expenses",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp),
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Animated Amount
            AnimatedCountUp(
                amount = totalSpent.toDouble() / 100.0,
                currencySymbol = "₹",
                isLarge = true,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Frosted Glass Bottom Shelf: Personal Share
            Surface(
                shape = RoundedCornerShape(16.dp),
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Your Personal Share",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = Formatters.money(personalSpent),
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun UnifiedCategoryDonutCard(
    categoryTotals: List<Pair<String, Long>>,
    totalSpent: Long
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
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
                    text = "Category Distribution",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${categoryTotals.size} categories",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Donut Chart
                CategoryDonutChart(
                    categoryTotals = categoryTotals,
                    totalSpent = totalSpent,
                    modifier = Modifier.padding(end = 16.dp)
                )

                // Right: Legend with Category Color, Percentage, and Amount
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryTotals.take(4).forEach { (category, amount) ->
                        val percent = if (totalSpent > 0) (amount.toFloat() / totalSpent.toFloat() * 100).toInt() else 0
                        val color = getCategoryColor(category)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category,
                                    style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = color.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "$percent%",
                                        style = Typography.labelSmall.copy(
                                            color = color,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = Formatters.money(amount),
                                    style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDonutChart(
    categoryTotals: List<Pair<String, Long>>,
    totalSpent: Long,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val topCategory = categoryTotals.firstOrNull()
    val topCategoryPercent = if (totalSpent > 0 && topCategory != null) {
        (topCategory.second.toFloat() / totalSpent.toFloat() * 100).toInt()
    } else 0

    Box(
        modifier = modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = size.width - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            val arcRect = Size(arcSize, arcSize)

            // Background track
            drawArc(
                color = if (isDark) Color(0xFF232733) else Color(0xFFF1F5F9),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcRect,
                style = Stroke(width = strokeWidthPx)
            )

            if (totalSpent > 0 && categoryTotals.isNotEmpty()) {
                var startAngle = -90f
                categoryTotals.forEach { (category, amount) ->
                    val sweepAngle = (amount.toFloat() / totalSpent.toFloat()) * 360f
                    if (sweepAngle > 0.5f) {
                        val color = getCategoryColor(category)
                        val gap = if (categoryTotals.size > 1) 3.5f else 0f
                        drawArc(
                            color = color,
                            startAngle = startAngle + gap / 2,
                            sweepAngle = (sweepAngle - gap).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcRect,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweepAngle
                }
            }
        }

        // Center metric focus
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (topCategory != null && totalSpent > 0) {
                Text(
                    text = "$topCategoryPercent%",
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = topCategory.first,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "₹0",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopContributorsSection(
    expenses: List<Expense>,
    members: List<Member>
) {
    val contributorStats = remember(expenses, members) {
        val memberPayments = mutableMapOf<String, Long>()
        val memberExpenseCounts = mutableMapOf<String, Int>()

        expenses.forEach { expense ->
            expense.paidBy.forEach { paidShare ->
                val prev = memberPayments.getOrDefault(paidShare.memberId, 0L)
                memberPayments[paidShare.memberId] = prev + paidShare.amountPaid
                val count = memberExpenseCounts.getOrDefault(paidShare.memberId, 0)
                memberExpenseCounts[paidShare.memberId] = count + 1
            }
        }

        memberPayments.entries
            .filter { it.value > 0 }
            .map { (memberId, totalPaid) ->
                val name = members.find { it.id == memberId }?.name ?: "Member"
                val count = memberExpenseCounts[memberId] ?: 0
                ContributorStat(name = name, totalPaid = totalPaid, expenseCount = count)
            }
            .sortedByDescending { it.totalPaid }
    }

    if (contributorStats.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP CONTRIBUTORS",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${contributorStats.size} spenders",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(contributorStats) { stat ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .width(135.dp)
                            .pressAnimation()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            LuxeMemberAvatar(name = stat.name, size = 36.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stat.name,
                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Formatters.money(stat.totalPaid),
                                style = Typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${stat.expenseCount} paid",
                                style = Typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendByJourneySection(
    trips: List<Trip>,
    expenses: List<Expense>,
    totalSpent: Long,
    onTripClick: ((String) -> Unit)? = null
) {
    if (trips.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPEND BY JOURNEY",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${trips.size} trips",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                trips.forEach { trip ->
                    val tripExpenses = expenses.filter { it.tripId == trip.id }
                    val tripTotal = SpendTotals.group(tripExpenses)
                    val memberCount = trip.memberIds.size.coerceAtLeast(1)
                    val perPerson = tripTotal / memberCount
                    val tripPercent = if (totalSpent > 0) tripTotal.toFloat() / totalSpent.toFloat() else 0f

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressAnimation()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = onTripClick != null) {
                                onTripClick?.invoke(trip.id)
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AuroraVioletLight.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Luggage,
                                            contentDescription = null,
                                            tint = AuroraVioletLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = trip.name,
                                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$memberCount members • ${Formatters.money(perPerson)}/person",
                                            style = Typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.money(tripTotal),
                                        style = MoneyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${(tripPercent * 100).toInt()}% of total",
                                        style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mini Progress Track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(tripPercent.coerceIn(0.02f, 1f))
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(AuroraVioletLight)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LargestExpensesSection(
    expenses: List<Expense>,
    members: List<Member>
) {
    val topExpenses = remember(expenses) {
        expenses.filter { !it.isPersonal }
            .sortedByDescending { it.totalAmount }
            .take(4)
    }

    if (topExpenses.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LARGEST EXPENSES",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Top ${topExpenses.size}",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                topExpenses.forEach { expense ->
                    val color = getCategoryColor(expense.category)
                    val icon = getCategoryIcon(expense.category)
                    val payerName = expense.paidBy.maxByOrNull { it.amountPaid }?.let { share ->
                        members.find { it.id == share.memberId }?.name
                    } ?: "Multiple"

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressAnimation()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = expense.description.ifBlank { expense.category },
                                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Paid by $payerName • ${Formatters.date(expense.date)}",
                                    style = Typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = Formatters.money(expense.totalAmount),
                                style = MoneyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "food", "food & drink", "dining", "restaurant" -> Color(0xFF10B981) // Emerald
        "transport", "travel", "fuel", "commute", "car" -> Color(0xFF0EA5E9) // Sky Blue
        "stay", "hotel", "accommodation", "resort" -> Color(0xFF8B5CF6) // Violet / Lavender
        "activities", "activity", "entertainment", "sightseeing", "games" -> Color(0xFFF59E0B) // Amber
        "shopping", "gifts", "souvenirs" -> Color(0xFFEC4899) // Rose
        else -> Color(0xFF64748B) // Slate
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "food", "food & drink", "dining", "restaurant" -> Icons.Outlined.Restaurant
        "transport", "travel", "fuel", "commute", "car" -> Icons.Outlined.DirectionsCar
        "stay", "hotel", "accommodation", "resort" -> Icons.Outlined.Hotel
        "activities", "activity", "entertainment", "sightseeing", "games" -> Icons.Outlined.SportsEsports
        else -> Icons.Outlined.Receipt
    }
}
