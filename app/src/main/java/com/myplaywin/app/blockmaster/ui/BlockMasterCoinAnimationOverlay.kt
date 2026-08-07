package com.myplaywin.app.blockmaster.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class AnimatedCoin(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val delayMs: Int,
    val size: Float,
    val durationMs: Int = 900
)

@Composable
fun BlockMasterCoinAnimationOverlay(
    trigger: Boolean,
    coinCount: Int = 12,
    onAnimationEnd: () -> Unit = {}
) {
    if (!trigger) return

    var animProgress by remember { mutableFloatStateOf(0f) }

    val animatedValue = animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "coin_anim",
        finishedListener = {
            onAnimationEnd()
        }
    )

    val coins = remember(trigger) {
        List(coinCount) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val distance = Random.nextFloat() * 250f + 80f
            AnimatedCoin(
                startX = 0.5f,
                startY = 0.5f,
                endX = 0.5f + (Math.cos(angle) * distance / 1000f).toFloat(),
                endY = 0.5f + (Math.sin(angle) * distance / 1000f).toFloat() - 0.2f,
                delayMs = Random.nextInt(0, 250),
                size = Random.nextFloat() * 12f + 16f
            )
        }
    }

    LaunchedEffect(trigger) {
        animProgress = 1.0f
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = animatedValue.value

        for (coin in coins) {
            val progress = ((t * 1200 - coin.delayMs) / coin.durationMs).coerceIn(0f, 1f)
            if (progress <= 0f) continue

            val currentX = coin.startX * w + (coin.endX * w - coin.startX * w) * progress
            // Parabolic arc for physics movement
            val arcY = Math.sin(progress * Math.PI).toFloat() * 120f
            val currentY = coin.startY * h + (coin.endY * h - coin.startY * h) * progress - arcY

            val alpha = if (progress > 0.8f) (1f - progress) * 5f else 1f

            scale(scale = (1f - progress * 0.3f), pivot = Offset(currentX, currentY)) {
                // Draw Outer Gold Ring
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = coin.size,
                    center = Offset(currentX, currentY)
                )
                // Draw Inner Shinier Ring
                drawCircle(
                    color = Color(0xFFFFF176).copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = coin.size * 0.65f,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}
