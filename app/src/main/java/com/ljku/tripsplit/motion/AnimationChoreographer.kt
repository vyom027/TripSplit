package com.ljku.tripsplit.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun StaggeredEnterAnimation(
    index: Int,
    content: @Composable () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        // 40ms stagger per item
        delay(index * 40L)
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = MotionTokens.tweenEntrance()) +
                slideInVertically(
                    initialOffsetY = { 50 }, // slide up slightly
                    animationSpec = MotionTokens.tweenEntrance()
                )
    ) {
        content()
    }
}
