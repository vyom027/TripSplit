package com.ljku.tripsplit.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =============================================================================
// SWISS LEDGER PALETTE — HIGH-CONTRAST EDITORIAL UTILITY
// (Anti-AI-Slop: Zero purple gradients, pure utilitarian financial precision)
// =============================================================================

// Dark Canvas & Surfaces (Matte Obsidian & Precision Hairlines)
val CanvasDark          = Color(0xFF0F1013) // Deep neutral graphite / matte obsidian
val SurfaceDark         = Color(0xFF17191E) // First elevation card surface
val SurfaceRaisedDark   = Color(0xFF20232B) // Interactive inputs, segmented controls & chips
val BorderDark          = Color(0xFF262A34) // Crisp hairline 1px border
val BorderSubtleDark    = Color(0xFF1C1F26) // Internal list dividers (0.5dp)

// Light Canvas & Surfaces (Chalk Alabaster & Crisp Paper)
val CanvasLight         = Color(0xFFF6F6F3) // Warm unbleached ledger paper
val SurfaceLight        = Color(0xFFFFFFFF) // Pure white card
val SurfaceRaisedLight  = Color(0xFFEEEEEE) // Inset container & controls
val BorderLight         = Color(0xFFE2E2DC) // Hairline slate border
val BorderSubtleLight   = Color(0xFFECECE8) // Internal list dividers (0.5dp)

// High-Contrast Typographic Inks
val InkPrimaryLight     = Color(0xFF0F1013) // Pitch black
val InkSecondaryLight   = Color(0xFF6B7280) // Muted slate-500
val InkTertiaryLight    = Color(0xFF9CA3AF) // Slate-400
val InkPrimaryDark      = Color(0xFFF9FAFB) // Stark white
val InkSecondaryDark    = Color(0xFF9CA3AF) // Muted slate
val InkTertiaryDark     = Color(0xFF6B7280) // Deep slate

// Signature Signal Accent (Deliberate & High-Utility)
val SignalLime          = Color(0xFFC8F530) // Electric Chartreuse (Dark mode primary anchor)
val SignalLimeDark      = Color(0xFF163300) // Deep Racing Green (Light mode primary anchor)
val SignalLimeContainer = Color(0xFFE9FBC0)

// Semantic Financial Balances
val BalancePositive     = Color(0xFF10B981) // Crisp Mint (Owed to you / +)
val BalancePositiveDark = Color(0xFF34D399) // Vivid Mint for dark mode
val BalanceNegative     = Color(0xFFEF4444) // Signal Red (You owe / -)
val BalanceNegativeDark = Color(0xFFF87171) // Soft Coral for dark mode
val BalanceNeutral      = Color(0xFF6B7280) // Even / Settled
val BalanceAmber        = Color(0xFFF59E0B) // Amber-500
val BalanceAmberDark    = Color(0xFFFBBF24) // Radiant amber
val SignalCyan          = Color(0xFF06B6D4) // Cyan category

// Hairline Linear Gradients (Restrained, high-tech utilitarian, never fuzzy purple)
val LedgerCardGradientDark = Brush.verticalGradient(
    listOf(Color(0xFF1A1C23), Color(0xFF14161B))
)
val LedgerCardGradientLight = Brush.verticalGradient(
    listOf(Color(0xFFFFFFFF), Color(0xFFFBFBFA))
)

// =============================================================================
// COMPATIBILITY ALIASES (Ensures existing screen code works seamlessly with new palette)
// =============================================================================
val BackgroundLight = CanvasLight
val SurfaceVariantLight = SurfaceRaisedLight
val SurfaceGlassLight = SurfaceLight
val BorderGlassLight = BorderLight
val TextPrimaryLight = InkPrimaryLight
val TextSecondaryLight = InkSecondaryLight
val TextTertiaryLight = InkTertiaryLight

val BackgroundDark = CanvasDark
val SurfaceVariantDark = SurfaceRaisedDark
val SurfaceGlassDark = SurfaceDark
val BorderGlassDark = BorderDark
val TextPrimaryDark = InkPrimaryDark
val TextSecondaryDark = InkSecondaryDark
val TextTertiaryDark = InkTertiaryDark

val AuroraVioletLight = Color(0xFF1E293B) // Replaced purple with crisp Slate-800
val AuroraVioletHover = Color(0xFF0F172A)
val AuroraVioletDark = SignalLime        // Dark mode primary is Signal Lime
val AuroraVioletContainerLight = Color(0xFFE2E8F0)
val AuroraVioletContainerDark = Color(0xFF20232B)

val AuroraEmeraldLight = BalancePositive
val AuroraEmeraldDark = BalancePositiveDark
val AuroraCoralLight = BalanceNegative
val AuroraCoralDark = BalanceNegativeDark
val AuroraGoldLight = BalanceAmber
val AuroraGoldDark = BalanceAmberDark
val AuroraCyan = SignalCyan
val AuroraRose = Color(0xFFF43F5E)
val AuroraIndigo = Color(0xFF3B82F6)

// Restrained utilitarian gradient aliases
val AuroraHeroGradient = Brush.verticalGradient(
    listOf(Color(0xFF181B22), Color(0xFF111317))
)
val AuroraCardGradientDark = LedgerCardGradientDark
val AuroraVioletGradient = Brush.horizontalGradient(
    listOf(SignalLime, Color(0xFFA3E635))
)
val AuroraEmeraldGradient = Brush.horizontalGradient(
    listOf(Color(0xFF059669), BalancePositive)
)
val AuroraCoralGradient = Brush.horizontalGradient(
    listOf(Color(0xFFDC2626), BalanceNegative)
)

val BrandTeal = AuroraVioletLight
val BrandTealLight = AuroraVioletDark
val BrandTealDark = AuroraVioletDark
val BrandEmerald = AuroraEmeraldLight
val BrandTerracotta = AuroraCoralLight
val BrandTerracottaHover = AuroraCoralLight
val BrandSage = AuroraEmeraldLight
val BrandGold = AuroraGoldLight
val BrandIndigo = AuroraVioletLight
val BrandTerracottaDark = AuroraCoralDark
val BrandSageDark = AuroraEmeraldDark
val BrandGoldDark = AuroraGoldDark
val BrandIndigoDark = AuroraVioletDark

val BackgroundDimWhite = BackgroundLight
val SurfaceGlassy = SurfaceLight
val TextCharcoal = TextPrimaryLight
val TextGray = TextSecondaryLight
val AccentTeal = AuroraVioletLight
val MutedSageGreen = AuroraEmeraldLight
val MutedTerracotta = AuroraCoralLight
val DividerGray = BorderLight
val ShadowSoft = Color(0x08090A0F)
val SurfaceElevated = SurfaceVariantDark
val AccentViolet = AuroraVioletLight
val AccentCoral = AuroraCoralDark
val AccentMint = AuroraEmeraldDark
val AccentAmber = AuroraGoldDark
val DividerGrayDark = BorderDark