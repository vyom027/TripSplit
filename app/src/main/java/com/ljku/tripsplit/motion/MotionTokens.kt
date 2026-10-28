package com.ljku.tripsplit.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object MotionTokens {
    // Easing curves matching web standards
    val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val EaseInOutQuart = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
    val EaseOutCirc = CubicBezierEasing(0.0f, 0.55f, 0.45f, 1f)

    // Standard durations
    const val DurationShort = 200
    const val DurationMedium = 350
    const val DurationLong = 500

    // Spring specs for different weights
    fun <T> springBouncy() = spring<T>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> springSnappy() = spring<T>(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> springSmooth() = spring<T>(
        dampingRatio = 1.0f,
        stiffness = Spring.StiffnessLow
    )

    // Tween specs
    fun <T> tweenEntrance(delay: Int = 0) = tween<T>(
        durationMillis = DurationMedium,
        delayMillis = delay,
        easing = EaseOutExpo
    )

    fun <T> tweenTransition() = tween<T>(
        durationMillis = DurationMedium,
        easing = EaseInOutQuart
    )
}
