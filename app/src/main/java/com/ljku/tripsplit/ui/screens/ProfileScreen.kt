package com.ljku.tripsplit.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.ui.components.pressAnimation
import com.ljku.tripsplit.ui.theme.AuroraEmeraldLight
import com.ljku.tripsplit.ui.theme.AuroraVioletLight
import com.ljku.tripsplit.ui.theme.LocalThemePreferences
import com.ljku.tripsplit.ui.theme.ThemeMode
import com.ljku.tripsplit.ui.theme.Typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    displayName: String,
    upiId: String,
    isNameLocked: Boolean = false,
    tripCount: Int = 0,
    friendsCount: Int = 0,
    totalExpensesCount: Int = 0,
    onDisplayNameChange: (String) -> Unit = {},
    onUpiChange: (String) -> Unit = {}
) {
    val themePreferences = LocalThemePreferences.current
    val currentTheme by themePreferences?.themeMode?.collectAsState(initial = ThemeMode.SYSTEM) ?: return
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 2026 Neo-Modern Editorial Header ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Settings & Profile",
                        style = Typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "Local-First & Private • v1.0.4",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- HERO TRAVEL PASS CARD (Liquid Mesh 26dp Squircle) ---
            item {
                NeoModernHeroProfileCard(
                    displayName = displayName,
                    upiId = upiId,
                    tripCount = tripCount,
                    friendsCount = friendsCount,
                    totalExpensesCount = totalExpensesCount
                )
            }

            // --- ACCOUNT & SETTLEMENT SETTINGS ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ACCOUNT & SETTLEMENT",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            else Color(0xFFF0F0F6)
                        ),
                        shadowElevation = if (isDark) 0.dp else 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Display Name Input
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { if (!isNameLocked) onDisplayNameChange(it) },
                                readOnly = isNameLocked,
                                label = { Text("Display Name") },
                                placeholder = { Text("Your full name or nickname") },
                                supportingText = if (isNameLocked) {
                                    {
                                        Text(
                                            "Assigned by trip creator • You can update your UPI below",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            style = Typography.labelSmall
                                        )
                                    }
                                } else null,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = AuroraVioletLight
                                    )
                                },
                                trailingIcon = if (isNameLocked) {
                                    {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = "Assigned by creator",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AuroraVioletLight,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                            )

                            // UPI ID Input
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = onUpiChange,
                                label = { Text("UPI ID (For Settlements)") },
                                placeholder = { Text("username@okhdfcbank") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Payment,
                                        contentDescription = null,
                                        tint = AuroraVioletLight
                                    )
                                },
                                trailingIcon = if (upiId.isNotBlank()) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.VerifiedUser,
                                            contentDescription = "Configured",
                                            tint = AuroraEmeraldLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AuroraVioletLight,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                            )

                            // Direct P2P Info Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = "⚡", fontSize = 16.sp)
                                    Text(
                                        text = "Direct P2P: Settlements trigger native UPI deep-links directly in GPay, PhonePe, or Paytm. Zero commission cuts, no tracking.",
                                        style = Typography.labelSmall.copy(lineHeight = 16.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- APPEARANCE & THEME ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "APPEARANCE & THEME",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            else Color(0xFFF0F0F6)
                        ),
                        shadowElevation = if (isDark) 0.dp else 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Interface Theme",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                NeoThemeSegment(
                                    title = "System",
                                    icon = Icons.Outlined.BrightnessAuto,
                                    selected = currentTheme == ThemeMode.SYSTEM,
                                    modifier = Modifier.weight(1f),
                                    onClick = { themePreferences.setThemeMode(ThemeMode.SYSTEM) }
                                )
                                NeoThemeSegment(
                                    title = "Light",
                                    icon = Icons.Outlined.LightMode,
                                    selected = currentTheme == ThemeMode.LIGHT,
                                    modifier = Modifier.weight(1f),
                                    onClick = { themePreferences.setThemeMode(ThemeMode.LIGHT) }
                                )
                                NeoThemeSegment(
                                    title = "Dark",
                                    icon = Icons.Outlined.DarkMode,
                                    selected = currentTheme == ThemeMode.DARK,
                                    modifier = Modifier.weight(1f),
                                    onClick = { themePreferences.setThemeMode(ThemeMode.DARK) }
                                )
                            }
                        }
                    }
                }
            }

            // --- PRIVACY & ARCHITECTURE ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DATA & PRIVACY",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                            else Color(0xFFF0F0F6)
                        ),
                        shadowElevation = if (isDark) 0.dp else 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AuroraEmeraldLight.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Security,
                                    contentDescription = null,
                                    tint = AuroraEmeraldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "100% Local-First & Private",
                                        style = Typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AuroraEmeraldLight.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Encrypted",
                                            style = Typography.labelSmall.copy(
                                                color = AuroraEmeraldLight,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "All splits, ledgers, and transactions run strictly on your device via Room SQLite. No ads, tracking, or user data profiling.",
                                    style = Typography.labelSmall.copy(lineHeight = 16.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // --- MINIMALIST FOOTER ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TripSplit • Neo-Modern 2026 Edition",
                        style = Typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Offline First • Real-time Sync • Zero-Fee P2P",
                        style = Typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}

// ======================================================================================
// 🌟 2026 NEO-MODERN HERO PROFILE CARD (Liquid Gradient Mesh)
// ======================================================================================

@Composable
private fun NeoModernHeroProfileCard(
    displayName: String,
    upiId: String,
    tripCount: Int,
    friendsCount: Int,
    totalExpensesCount: Int
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    val liquidMeshBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD6C9), // Soft peach
            Color(0xFFFFE5D9), // Light warm peach
            Color(0xFFE8DEF8), // Lavender mist
            Color(0xFFD0BCFF)  // Soft lilac
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0xFFD0BCFF).copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
    ) {
        Box(
            modifier = Modifier
                .background(liquidMeshBrush)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Top Identity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Luxe Squircle Avatar with Verified Ring
                    Box(modifier = Modifier.size(68.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(1).uppercase().ifBlank { "Y" },
                                    style = Typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF2D2A32)
                                    )
                                )
                            }
                        }

                        // Emerald Verified Badge
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Name & Identity Chip
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName.ifBlank { "You" },
                            style = Typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                fontSize = 24.sp
                            ),
                            color = Color(0xFF1E1A22)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color.White.copy(alpha = 0.65f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.9f))
                        ) {
                            Text(
                                text = "Personal Travel Identity",
                                style = Typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF5A4D6B)
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Frosted Glass UPI Pill Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Payment,
                                    contentDescription = null,
                                    tint = AuroraVioletLight,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RECEIVING UPI ID",
                                    style = Typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp,
                                        color = Color(0xFF7E728B)
                                    )
                                )
                                Text(
                                    text = upiId.ifBlank { "Tap below to link UPI" },
                                    style = Typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1A22)
                                    ),
                                    maxLines = 1
                                )
                            }
                        }

                        if (upiId.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (copied) Color(0xFF10B981) else Color.White,
                                border = BorderStroke(
                                    0.5.dp,
                                    if (copied) Color(0xFF10B981) else Color(0xFFD6CEE2)
                                ),
                                modifier = Modifier
                                    .pressAnimation()
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(upiId))
                                        copied = true
                                        Toast.makeText(context, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copied) Icons.Rounded.Check else Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = if (copied) Color.White else Color(0xFF5A4D6B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (copied) "Copied!" else "Copy",
                                        style = Typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (copied) Color.White else Color(0xFF5A4D6B)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Frosted Travel Footprint Shelf (3 Metric Pills)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileFootprintMetric(
                            count = tripCount.toString(),
                            label = "Journeys",
                            icon = Icons.Outlined.FlightTakeoff
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(Color(0xFFD6CEE2).copy(alpha = 0.6f))
                        )
                        ProfileFootprintMetric(
                            count = friendsCount.toString(),
                            label = "Friends Split",
                            icon = Icons.Outlined.Group
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(Color(0xFFD6CEE2).copy(alpha = 0.6f))
                        )
                        ProfileFootprintMetric(
                            count = totalExpensesCount.toString(),
                            label = "Expenses",
                            icon = Icons.Outlined.ReceiptLong
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileFootprintMetric(
    count: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF5A4D6B),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = count,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF1E1A22)
                )
            )
        }
        Text(
            text = label,
            style = Typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6E627C)
            )
        )
    }
}

// ======================================================================================
// 🎛️ TACTILE MODERN THEME SEGMENT
// ======================================================================================

@Composable
private fun NeoThemeSegment(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val activeBg = if (isDark) Color(0xFF352F3D) else Color.White
    val activeContentColor = if (isDark) Color(0xFFD0BCFF) else AuroraVioletLight
    val inactiveContentColor = if (isDark) Color(0xFFA69EB0) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) activeBg else Color.Transparent)
            .then(
                if (selected && isDark) {
                    Modifier.border(BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.35f)), RoundedCornerShape(10.dp))
                } else Modifier
            )
            .shadow(
                elevation = if (selected && !isDark) 2.dp else 0.dp,
                shape = RoundedCornerShape(10.dp)
            )
            .pressAnimation()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeContentColor else inactiveContentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = Typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) activeContentColor else inactiveContentColor
            )
        }
    }
}
