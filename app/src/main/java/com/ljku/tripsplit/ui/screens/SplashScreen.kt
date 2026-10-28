package com.ljku.tripsplit.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ljku.tripsplit.ui.theme.LocalThemePreferences
import com.ljku.tripsplit.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashPurple = Color(0xFF7C3AED)
private val SplashGreen = Color(0xFF059669)

@Composable
fun SplashScreen(
    onAnimationComplete: () -> Unit = {}
) {
    val themePreferences = LocalThemePreferences.current
    val currentThemeMode = themePreferences?.themeMode?.collectAsState()?.value ?: ThemeMode.SYSTEM
    val isDark = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Adaptive theme colors
    val bgColor = if (isDark) Color(0xFF0F1013) else Color(0xFFFAFAFB)
    val badgeBg = if (isDark) Color(0xFF1D1A28) else Color(0xFFFFFFFF)
    val badgeBorder = if (isDark) Color(0x1FFFFFFF) else Color(0x0F000000)
    val tripColor = if (isDark) Color(0xFFEDE9F8) else Color(0xFF18151F)
    val tStrokeColor = if (isDark) Color(0xFFEDE9F8) else Color(0xFF18151F)
    val ghostLoopColor = if (isDark) Color(0xFFDDD8EE).copy(alpha = 0.20f) else Color(0xFF1C1924).copy(alpha = 0.12f)
    val dotDarkColor = if (isDark) Color(0xFFDDD8EE) else Color(0xFF1C1924)

    // Animation progress drivers
    val constellationLineAlpha = remember { Animatable(0f) }
    val constellationProgress = remember { Animatable(0f) }
    val loopProgress = remember { Animatable(0f) }
    val loopAlpha = remember { Animatable(1f) }
    val badgeScale = remember { Animatable(0.18f) }
    val badgeAlpha = remember { Animatable(0f) }
    val badgePulseScale = remember { Animatable(1f) }
    val tStrokeProgress = remember { Animatable(0f) }
    val sStrokeProgress = remember { Animatable(0f) }
    val letterProgress = remember { Animatable(0f) }
    val underlineProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Phase 1: Constellation nodes & dashed lines (0ms - 460ms)
        launch {
            constellationLineAlpha.animateTo(
                targetValue = 0.32f,
                animationSpec = tween(120, easing = LinearEasing)
            )
            constellationLineAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(340, easing = FastOutSlowInEasing)
            )
        }
        launch {
            constellationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(440, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f))
            )
        }

        // Phase 2: Pathfinder Loop (Lemniscate) draws ONCE with nib dot (460ms - 1380ms)
        delay(460)
        launch {
            loopProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(880, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
            )
        }

        // Loop fades out smoothly
        delay(920)
        launch {
            loopAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            )
        }

        // Phase 3: Badge pops up from loop center (1520ms - 2150ms)
        delay(140)
        launch {
            badgeAlpha.animateTo(1f, tween(180))
        }
        launch {
            badgeScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }

        // TS Monogram path drawing inside badge
        delay(140)
        launch {
            tStrokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(360, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }
        delay(80)
        launch {
            sStrokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(420, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }

        // Badge settle pulse
        delay(320)
        launch {
            badgePulseScale.animateTo(1.04f, tween(120))
            badgePulseScale.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
        }

        // Phase 4: Staggered Wordmark letter reveal (2150ms - 2500ms)
        delay(120)
        launch {
            letterProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(420, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }

        // Underline sweep under "Split"
        delay(220)
        launch {
            underlineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(320, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            )
        }

        // Final hold & transition
        delay(400)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stage containing Constellation, Lemniscate Loop, and TS Badge (280x280)
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Kinetic Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val s = size.width / 280f

                    // 1. Constellation Phase
                    val tConst = constellationProgress.value
                    if (tConst < 1f || constellationLineAlpha.value > 0f) {
                        // 5 Nodes: start, control, and end points
                        val p01 = Offset(cx - 116f * s, cy - 116f * s)
                        val c1 = Offset(cx - 68f * s, cy - 60f * s)
                        val pos1 = quadBezier(p01, c1, Offset(cx, cy), tConst)

                        val p02 = Offset(cx + 116f * s, cy - 116f * s)
                        val c2 = Offset(cx + 68f * s, cy - 60f * s)
                        val pos2 = quadBezier(p02, c2, Offset(cx, cy), tConst)

                        val p03 = Offset(cx - 116f * s, cy + 116f * s)
                        val c3 = Offset(cx - 68f * s, cy + 60f * s)
                        val pos3 = quadBezier(p03, c3, Offset(cx, cy), tConst)

                        val p04 = Offset(cx + 116f * s, cy + 116f * s)
                        val c4 = Offset(cx + 68f * s, cy + 60f * s)
                        val pos4 = quadBezier(p04, c4, Offset(cx, cy), tConst)

                        val p05 = Offset(cx, cy - 130f * s)
                        val c5 = Offset(cx, cy - 68f * s)
                        val pos5 = quadBezier(p05, c5, Offset(cx, cy), tConst)

                        // Draw dashed constellation lines
                        if (constellationLineAlpha.value > 0f) {
                            val lineAlpha = constellationLineAlpha.value
                            val lineColor = SplashPurple.copy(alpha = lineAlpha)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f)

                            drawLine(lineColor, p01, p02, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                            drawLine(lineColor, p02, p04, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                            drawLine(lineColor, p04, p03, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                            drawLine(lineColor, p03, p01, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                            drawLine(lineColor, p01, p04, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                            drawLine(lineColor, p02, p03, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                            drawLine(lineColor, p05, p01, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                            drawLine(lineColor, p05, p02, strokeWidth = 1.dp.toPx(), pathEffect = dashEffect)
                        }

                        // Draw moving dots with alpha fade
                        val dotAlpha = when {
                            tConst < 0.1f -> tConst / 0.1f
                            tConst > 0.8f -> (1f - tConst) / 0.2f
                            else -> 1f
                        }.coerceIn(0f, 1f)

                        if (dotAlpha > 0f) {
                            drawCircle(SplashPurple.copy(alpha = dotAlpha), radius = 5.5.dp.toPx(), center = pos1)
                            drawCircle(dotDarkColor.copy(alpha = dotAlpha), radius = 5.dp.toPx(), center = pos2)
                            drawCircle(SplashPurple.copy(alpha = dotAlpha * 0.85f), radius = 6.dp.toPx(), center = pos3)
                            drawCircle(SplashGreen.copy(alpha = dotAlpha), radius = 4.5.dp.toPx(), center = pos4)
                            drawCircle(dotDarkColor.copy(alpha = dotAlpha * 0.6f), radius = 4.dp.toPx(), center = pos5)
                        }
                    }

                    // 2. Pathfinder Lemniscate Loop
                    if (loopProgress.value > 0f && loopAlpha.value > 0f) {
                        val currentLoopAlpha = loopAlpha.value
                        val loopPath = Path().apply {
                            moveTo(cx, cy)
                            cubicTo(cx - 36f * s, cy - 46f * s, cx - 96f * s, cy - 46f * s, cx - 96f * s, cy)
                            cubicTo(cx - 96f * s, cy + 46f * s, cx - 36f * s, cy + 46f * s, cx, cy)
                            cubicTo(cx + 36f * s, cy - 46f * s, cx + 96f * s, cy - 46f * s, cx + 96f * s, cy)
                            cubicTo(cx + 96f * s, cy + 46f * s, cx + 36f * s, cy + 46f * s, cx, cy)
                        }

                        val pathMeasure = PathMeasure()
                        pathMeasure.setPath(loopPath, false)
                        val totalLen = pathMeasure.length

                        // Draw ghost echo
                        drawPath(
                            path = loopPath,
                            color = ghostLoopColor.copy(alpha = ghostLoopColor.alpha * currentLoopAlpha),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw animated purple stroke
                        val partialPath = Path()
                        pathMeasure.getSegment(0f, totalLen * loopProgress.value, partialPath, true)
                        drawPath(
                            path = partialPath,
                            color = SplashPurple.copy(alpha = currentLoopAlpha),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Exactly ONE pass of the leading nib dot riding the stroke tip
                        if (loopProgress.value in 0.02f..0.98f) {
                            val nibPos = pathMeasure.getPosition(totalLen * loopProgress.value)
                            drawCircle(
                                color = SplashPurple.copy(alpha = currentLoopAlpha),
                                radius = 7.dp.toPx(),
                                center = nibPos
                            )
                        }
                    }
                }

                // 3. TS Geometric Badge
                val totalBadgeScale = badgeScale.value * badgePulseScale.value
                if (badgeAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .graphicsLayer {
                                scaleX = totalBadgeScale
                                scaleY = totalBadgeScale
                                alpha = badgeAlpha.value
                            }
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(26.dp),
                                ambientColor = SplashPurple.copy(alpha = 0.25f),
                                spotColor = SplashPurple.copy(alpha = 0.35f)
                            )
                            .background(badgeBg, RoundedCornerShape(26.dp))
                            .border(1.5.dp, badgeBorder, RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(46.dp)) {
                            val bs = size.width / 46f

                            // Draw T Horizontal Bar
                            val tBarPath = Path().apply {
                                moveTo(8f * bs, 13f * bs)
                                lineTo(38f * bs, 13f * bs)
                            }
                            // Draw T Vertical Stem
                            val tStemPath = Path().apply {
                                moveTo(23f * bs, 13f * bs)
                                lineTo(23f * bs, 37f * bs)
                            }

                            if (tStrokeProgress.value > 0f) {
                                val tBarMeasure = PathMeasure().apply { setPath(tBarPath, false) }
                                val partialBar = Path()
                                tBarMeasure.getSegment(0f, tBarMeasure.length * tStrokeProgress.value, partialBar, true)

                                val tStemMeasure = PathMeasure().apply { setPath(tStemPath, false) }
                                val partialStem = Path()
                                tStemMeasure.getSegment(0f, tStemMeasure.length * tStrokeProgress.value, partialStem, true)

                                val tStroke = Stroke(
                                    width = 5.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                                drawPath(partialBar, color = tStrokeColor, style = tStroke)
                                drawPath(partialStem, color = tStrokeColor, style = tStroke)
                            }

                            // Draw S Interlocking Curve
                            if (sStrokeProgress.value > 0f) {
                                val sPath = Path().apply {
                                    moveTo(36f * bs, 19f * bs)
                                    cubicTo(36f * bs, 14f * bs, 26f * bs, 14f * bs, 19f * bs, 21f * bs)
                                    cubicTo(12f * bs, 27f * bs, 9f * bs, 32f * bs, 9f * bs, 37f * bs)
                                    cubicTo(9f * bs, 42f * bs, 20f * bs, 44f * bs, 28f * bs, 41f * bs)
                                }
                                val sMeasure = PathMeasure().apply { setPath(sPath, false) }
                                val partialS = Path()
                                sMeasure.getSegment(0f, sMeasure.length * sStrokeProgress.value, partialS, true)

                                drawPath(
                                    path = partialS,
                                    color = SplashPurple,
                                    style = Stroke(
                                        width = 5.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Clean Kinetic Wordmark (No Tagline)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.height(44.dp)
                ) {
                    val word = "TripSplit"
                    word.forEachIndexed { index, char ->
                        val charStart = index * 0.07f
                        val charProgress = ((letterProgress.value - charStart) / 0.35f).coerceIn(0f, 1f)
                        val charColor = if (index < 4) tripColor else SplashPurple

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .clipToBounds(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = char.toString(),
                                style = TextStyle(
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.8).sp,
                                    color = charColor
                                ),
                                modifier = Modifier.graphicsLayer {
                                    translationY = (1f - charProgress) * 44.dp.toPx()
                                    alpha = charProgress
                                }
                            )
                        }
                    }
                }

                // Underline sweep under "Split"
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.width(148.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .width(88.dp * underlineProgress.value)
                            .height(2.5.dp)
                            .background(SplashPurple, RoundedCornerShape(1.5.dp))
                    )
                }
            }
        }
    }
}

/**
 * Calculates a point along a quadratic bezier curve at parameter t in [0, 1].
 */
private fun quadBezier(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val oneMinusT = 1f - t
    val x = oneMinusT * oneMinusT * p0.x + 2f * oneMinusT * t * p1.x + t * t * p2.x
    val y = oneMinusT * oneMinusT * p0.y + 2f * oneMinusT * t * p1.y + t * t * p2.y
    return Offset(x, y)
}
