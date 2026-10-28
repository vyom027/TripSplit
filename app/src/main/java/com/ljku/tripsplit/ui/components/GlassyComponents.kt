package com.ljku.tripsplit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.Send
import com.ljku.tripsplit.domain.ExpenseRequest
import com.ljku.tripsplit.domain.Member
import com.ljku.tripsplit.domain.PaidShare
import com.ljku.tripsplit.domain.SettlementEngine
import com.ljku.tripsplit.domain.SplitMode
import com.ljku.tripsplit.domain.SplitShare
import com.ljku.tripsplit.domain.Transaction
import com.ljku.tripsplit.ui.util.Formatters
import com.ljku.tripsplit.ui.util.UpiPay
import java.util.UUID
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.motion.MotionTokens
import com.ljku.tripsplit.ui.theme.BalanceNegative
import com.ljku.tripsplit.ui.theme.BalancePositive
import com.ljku.tripsplit.ui.theme.HeroAmount
import com.ljku.tripsplit.ui.theme.HeroBalance
import com.ljku.tripsplit.ui.theme.LedgerAmount
import com.ljku.tripsplit.ui.theme.MoneyMedium
import com.ljku.tripsplit.ui.theme.SectionTag
import com.ljku.tripsplit.ui.theme.SignalLime
import com.ljku.tripsplit.ui.theme.Typography
import com.valentinilk.shimmer.shimmer
import java.util.Locale

/**
 * Tactile micro-interaction: smooth snappy press down for high-feedback feel.
 */
fun Modifier.pressAnimation(
    scaleDown: Float = 0.98f
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = if (isPressed) MotionTokens.springSnappy() else MotionTokens.springBouncy(),
        label = "PressScale"
    )

    this
        .scale(scale)
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}

/**
 * Swiss Ledger Card: High-density, utilitarian flat card with a crisp 1px hairline border.
 * Zero blurry drop-shadows or bloated gradients.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LedgerCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = MotionTokens.springSnappy(),
        label = "LedgerCardScale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val cardBg = backgroundColor ?: MaterialTheme.colorScheme.surface
    val effectiveBorder = borderStroke ?: BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline
    )

    val baseModifier = modifier
        .scale(scale)
        .clip(shape)
        .background(cardBg)
        .border(effectiveBorder, shape)

    val interactModifier = if (onClick != null || onLongClick != null) {
        baseModifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                onClick = onClick ?: {},
                onLongClick = onLongClick
            )
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
    } else {
        baseModifier
    }

    Box(modifier = interactModifier, content = content)
}

// Backward-compatibility alias
@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    elevation: Dp = 0.dp,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    LedgerCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        borderStroke = borderStroke,
        onClick = onClick,
        onLongClick = onLongClick,
        content = content
    )
}

/**
 * Editorial Hero Container (Replaces bloated purple gradient box with disciplined Swiss styling).
 */
@Composable
fun AuroraGradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush? = null,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val bg = if (isDark) Color(0xFF14161C) else Color(0xFFFFFFFF)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(20.dp),
        content = content
    )
}

/**
 * Animated Financial Counter in Tabular Monospace Numeral styling.
 */
@Composable
fun AnimatedCountUp(
    amount: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier,
    isLarge: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val targetVal = amount.toFloat()
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetVal) {
        animatedValue.animateTo(
            targetValue = targetVal,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val style = if (isLarge) HeroBalance else MoneyMedium
    val displayVal = String.format(Locale.getDefault(), "%,d", Math.round(animatedValue.value.toDouble()))

    Text(
        text = "$currencySymbol$displayVal",
        style = style,
        color = color,
        modifier = modifier
    )
}

/**
 * Connected Ledger Grouped List Container.
 * Renders connected rows with 0.5dp hairline dividers between them (no isolated card clutter).
 */
@Composable
fun LedgerGroupedContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape),
        content = content
    )
}

/**
 * Hairline Divider for inside connected ledger rows.
 */
@Composable
fun LedgerHairlineDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/**
 * Universal Stat Box for Financial Analytics & Insights.
 */
@Composable
fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    subValue: String? = null,
    icon: ImageVector? = null
) {
    LedgerCard(
        modifier = modifier,
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    style = SectionTag,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = LedgerAmount,
                color = color
            )
            if (subValue != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subValue,
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Shimmer Loading Skeleton Card.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val baseColor = if (isDark) Color(0xFF1E2129) else Color(0xFFECECE8)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .shimmer()
            .background(baseColor)
    )
}

/**
 * Member Avatar Stack (+N overflow badge).
 */
@Composable
fun MemberAvatarGroup(
    names: List<String>,
    maxVisible: Int = 4,
    avatarSize: Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    val visibleNames = names.take(maxVisible)
    val remainingCount = names.size - maxVisible

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        visibleNames.forEachIndexed { index, name ->
            Box(
                modifier = Modifier
                    .offset(x = (-6 * index).dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                LuxeMemberAvatar(name = name, size = avatarSize)
            }
        }
        if (remainingCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-6 * visibleNames.size).dp)
                    .size(avatarSize)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remainingCount",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Clean Category Badge (Subtle tone container with crisp 1px border).
 */
@Composable
fun CategoryIconBox(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.25f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

/**
 * Direct Transfer Voucher Card for Settlements (Vertical list layout).
 */
@Composable
fun SettlementAvatarCard(
    fromName: String,
    toName: String,
    amountStr: String,
    modifier: Modifier = Modifier,
    onSettleClick: (() -> Unit)? = null
) {
    LedgerCard(
        modifier = modifier
            .fillMaxWidth()
            .pressAnimation(),
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Debtor Avatar
                LuxeMemberAvatar(name = fromName, size = 36.dp)
                Spacer(modifier = Modifier.width(8.dp))

                // Direction Indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(11.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                // Creditor Avatar
                LuxeMemberAvatar(name = toName, size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))

                // Route labels
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fromName,
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "owes",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = BalanceNegative
                        )
                    }
                    Text(
                        text = "to $toName",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount + Settle Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = amountStr,
                    style = LedgerAmount.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (onSettleClick != null) {
                    FilledTonalButton(
                        onClick = onSettleClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Settle",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal Carousel Card for simplified direct debts ("Who Owes Whom").
 * Designed specifically for left-to-right horizontal sliders with tactile depth and clear route hierarchy.
 */
@Composable
fun DebtTransferSliderCard(
    fromName: String,
    toName: String,
    amountStr: String,
    modifier: Modifier = Modifier,
    onSettleClick: (() -> Unit)? = null
) {
    LedgerCard(
        modifier = modifier
            .width(280.dp)
            .pressAnimation(),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top: Transfer Route (From -> To)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Debtor
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    LuxeMemberAvatar(name = fromName, size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = fromName,
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "owes",
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Arrow Pill
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Creditor
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = toName,
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "receives",
                            style = Typography.labelSmall,
                            color = BalancePositive
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LuxeMemberAvatar(name = toName, size = 32.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom: Amount & Settle Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AMOUNT",
                        style = SectionTag.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = amountStr,
                        style = LedgerAmount.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (onSettleClick != null) {
                    Button(
                        onClick = onSettleClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Settle",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Inline Join/Expense Request Card with instant approve/reject buttons.
 */
@Composable
fun InlineRequestCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    LedgerCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = Typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BalanceNegative.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Reject",
                        tint = BalanceNegative,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onApprove,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BalancePositive.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Approve",
                        tint = BalancePositive,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Minimal status pill.
 */
@Composable
fun LuxuryStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dotColor: Color? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val border = MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = SectionTag,
            color = textColor
        )
    }
}

/**
 * Crisp segmented tab bar with spring physics and hairline borders.
 */
@Composable
fun LuxeSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val tabBg by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                animationSpec = spring(),
                label = "TabBg"
            )
            val tabText by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(),
                label = "TabText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(tabBg)
                    .border(
                        if (isSelected) 1.dp else 0.dp,
                        if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent,
                        RoundedCornerShape(7.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = Typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.5.sp
                    ),
                    color = tabText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Clean member avatar circle with deterministic high-contrast tints.
 */
@Composable
fun LuxeMemberAvatar(
    name: String,
    size: Dp = 34.dp,
    modifier: Modifier = Modifier
) {
    val initial = name.trim().take(1).uppercase().ifBlank { "?" }
    val colors = listOf(
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFF8B5CF6),
        Color(0xFF06B6D4)
    )
    val colorIndex = Math.abs(name.hashCode()) % colors.size
    val accent = colors[colorIndex]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = (size.value * 0.42f).sp),
            color = accent
        )
    }
}

data class AuroraNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Docked Chassis Bottom Bar (56dp height, 1px top border, no detached floating pill).
 * Perfectly anchored to physical chassis with high thumb ergonomics.
 */
@Composable
fun AuroraBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        AuroraNavItem(
            route = "tripList",
            title = "Trips",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        AuroraNavItem(
            route = "insights",
            title = "Insights",
            selectedIcon = Icons.Filled.Analytics,
            unselectedIcon = Icons.Outlined.Analytics
        ),
        AuroraNavItem(
            route = "profile",
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val tintColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    animationSpec = spring(),
                    label = "NavTint"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onNavigate(item.route)
                        }
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        style = Typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = tintColor
                    )
                }
            }
        }
    }
}

/**
 * Modern Swiss Ledger Modal Bottom Sheet for Settlements.
 * Provides instant 1-tap UPI payment, partial payment customization, and 1-tap ledger settlement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpModalBottomSheet(
    pendingDebts: List<Transaction>,
    members: List<Member>,
    initialTransaction: Transaction? = null,
    canManage: Boolean = true,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onDismiss: () -> Unit,
    onMarkSettled: (Transaction) -> Unit,
    onRequestSettlement: ((Transaction) -> Unit)? = null
) {
    val context = LocalContext.current
    val memberMap = remember(members) { members.associateBy { it.id } }

    var activeTx by remember(initialTransaction, pendingDebts) {
        mutableStateOf(initialTransaction ?: pendingDebts.firstOrNull())
    }

    var partialAmountText by remember(activeTx) {
        mutableStateOf(activeTx?.let { Math.round(it.amount / 100.0).toString() } ?: "")
    }
    var isEditingAmount by remember(activeTx) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settle Debt",
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Direct transfer between trip members",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (pendingDebts.isEmpty()) {
                // All settled state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = BalancePositive,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "All Settled Up!",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Zero outstanding balances remain between members.",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // If there are multiple debts and no specific initial transaction was clicked, allow picking
                if (pendingDebts.size > 1 && initialTransaction == null) {
                    Text(
                        text = "SELECT DEBT TO SETTLE (${pendingDebts.size})",
                        style = SectionTag,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pendingDebts) { tx ->
                            val from = memberMap[tx.fromMemberId]?.displayName(currentMemberId, viewerName) ?: "Someone"
                            val to = memberMap[tx.toMemberId]?.displayName(currentMemberId, viewerName) ?: "Someone"
                            val isSelected = tx == activeTx
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { activeTx = tx }
                            ) {
                                Text(
                                    text = "$from → $to",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                activeTx?.let { tx ->
                    val fromMember = memberMap[tx.fromMemberId]
                    val toMember = memberMap[tx.toMemberId]
                    val fromName = fromMember?.displayName(currentMemberId, viewerName) ?: "Payer"
                    val toName = toMember?.displayName(currentMemberId, viewerName) ?: "Receiver"

                    // Debt Route Card
                    LedgerCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LuxeMemberAvatar(name = fromName, size = 36.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = fromName,
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "owes",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = BalanceNegative
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = toName,
                                            style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "receives",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = BalancePositive
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    LuxeMemberAvatar(name = toName, size = 36.dp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Section
                    if (!isEditingAmount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SETTLEMENT AMOUNT",
                                    style = SectionTag.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.money(tx.amount),
                                    style = LedgerAmount.copy(fontSize = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(
                                onClick = {
                                    partialAmountText = Math.round(tx.amount / 100.0).toString()
                                    isEditingAmount = true
                                }
                            ) {
                                Text(
                                    text = "Custom Amount",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Column {
                            OutlinedTextField(
                                value = partialAmountText,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) partialAmountText = input
                                },
                                label = { Text("Amount Paid (₹)") },
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    TextButton(onClick = {
                                        partialAmountText = Math.round(tx.amount / 100.0).toString()
                                        isEditingAmount = false
                                    }) {
                                        Text("Full Amount", style = Typography.labelSmall)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Full outstanding amount: ${Formatters.money(tx.amount)}",
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // UPI Pay Section (if receiver has UPI)
                    val toUpi = toMember?.upiId?.takeIf { it.isNotBlank() }
                    if (toUpi != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SignalLime.copy(alpha = 0.08f))
                                .border(1.dp, SignalLime.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.FlashOn,
                                    contentDescription = null,
                                    tint = SignalLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "UPI ID: $toUpi",
                                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Ready for instant UPI transfer",
                                        style = Typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { UpiPay.copy(context, toUpi, "UPI for $toName") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val enteredRupees = partialAmountText.toLongOrNull() ?: Math.round(tx.amount / 100.0)
                                val paise = (enteredRupees * 100L).coerceIn(1L, tx.amount)
                                UpiPay.pay(context, toUpi, toName, paise, "TripSplit settle")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SignalLime,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .pressAnimation()
                        ) {
                            Icon(Icons.Outlined.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pay with UPI App",
                                style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Action buttons (Cancel & Mark as Settled / Request Settle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(if (canManage) "Cancel" else "Close", style = Typography.labelLarge)
                        }

                        if (canManage) {
                            Button(
                                onClick = {
                                    val enteredRupees = partialAmountText.toLongOrNull() ?: Math.round(tx.amount / 100.0)
                                    val paise = (enteredRupees * 100L).coerceIn(1L, tx.amount)
                                    onMarkSettled(tx.copy(amount = paise))
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .pressAnimation()
                            ) {
                                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Paid", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        } else {
                            Button(
                                onClick = {
                                    val enteredRupees = partialAmountText.toLongOrNull() ?: Math.round(tx.amount / 100.0)
                                    val paise = (enteredRupees * 100L).coerceIn(1L, tx.amount)
                                    onRequestSettlement?.invoke(tx.copy(amount = paise))
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .pressAnimation()
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Request Settle", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    if (!canManage) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Your settlement request will be sent to the trip manager for approval.",
                                    style = Typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern Modal Bottom Sheet for Requesting an Expense.
 * Allows members to submit an expense for manager review without opening a new screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestExpenseModalBottomSheet(
    tripId: String,
    members: List<Member>,
    defaultPayerId: String? = null,
    currentMemberId: String? = null,
    viewerName: String? = null,
    onDismiss: () -> Unit,
    onSubmitRequest: (ExpenseRequest) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var payerId by remember(members, defaultPayerId) {
        mutableStateOf(defaultPayerId ?: members.firstOrNull()?.id ?: "")
    }
    var includedMemberIds by remember(members) {
        mutableStateOf(members.map { it.id }.toSet())
    }

    val categories = listOf("Food", "Transport", "Stay", "Activities", "Other")

    val amountRupees = amountText.toLongOrNull() ?: 0L
    val amountCents = amountRupees * 100L
    val canSubmit = description.isNotBlank() && amountRupees > 0 && includedMemberIds.isNotEmpty() && payerId.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Request Expense",
                        style = Typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Submit an expense for manager review & approval",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Amount Field - Large & Whole Rupees
            Text(
                text = "AMOUNT (₹)",
                style = SectionTag,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) amountText = input
                },
                placeholder = { Text("0") },
                prefix = { Text("₹ ", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            Text(
                text = "WHAT WAS IT FOR?",
                style = SectionTag,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("e.g. Dinner, Fuel, Entry tickets") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips
            Text(
                text = "CATEGORY",
                style = SectionTag,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            selectedCategory = cat
                            if (description.isBlank()) description = cat
                        }
                    ) {
                        Text(
                            text = cat,
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Who Paid Selector
            Text(
                text = "WHO PAID?",
                style = SectionTag,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members) { m ->
                    val isSelected = m.id == payerId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { payerId = m.id }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            LuxeMemberAvatar(name = m.name, size = 22.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = m.displayName(currentMemberId ?: defaultPayerId, viewerName),
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split Among Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPLIT EQUALLY AMONG (${includedMemberIds.size})",
                    style = SectionTag,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        includedMemberIds = if (includedMemberIds.size == members.size) {
                            setOf(payerId)
                        } else {
                            members.map { it.id }.toSet()
                        }
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (includedMemberIds.size == members.size) "Select None" else "Select All",
                        style = Typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members) { m ->
                    val isIncluded = m.id in includedMemberIds
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isIncluded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.clickable {
                            includedMemberIds = if (isIncluded) {
                                if (includedMemberIds.size > 1) includedMemberIds - m.id else includedMemberIds
                            } else {
                                includedMemberIds + m.id
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isIncluded) Icons.Outlined.Check else Icons.Rounded.Close,
                                contentDescription = null,
                                tint = if (isIncluded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = m.displayName(currentMemberId ?: defaultPayerId, viewerName),
                                style = Typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text("Cancel", style = Typography.labelLarge)
                }

                Button(
                    onClick = {
                        val splits = SettlementEngine.buildEqualSplits(amountCents, includedMemberIds.toList())
                        val req = ExpenseRequest(
                            id = UUID.randomUUID().toString(),
                            tripId = tripId,
                            fromMemberId = payerId,
                            description = description.trim(),
                            totalAmount = amountCents,
                            date = System.currentTimeMillis(),
                            paidBy = listOf(PaidShare(payerId, amountCents)),
                            splitAmong = splits,
                            splitMode = SplitMode.EQUAL,
                            category = selectedCategory
                        )
                        onSubmitRequest(req)
                        onDismiss()
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp).pressAnimation(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Request", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
