package com.ferdidrgn.anlikdepremler.core.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

object AppAnimations {

    // 1. Shimmer (Parlama) Yükleme Animasyonu (Modifier)
    fun Modifier.shimmer(): Modifier = composed {
        var size by remember { mutableStateOf(IntSize.Zero) }
        val transition = rememberInfiniteTransition(label = "shimmer")
        val startOffsetX by transition.animateFloat(
            initialValue = -2 * size.width.toFloat(),
            targetValue = 2 * size.width.toFloat(),
            animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
            label = "shimmerOffset"
        )

        background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE0E0E0),
                    Color(0xFFF5F5F5),
                    Color(0xFFE0E0E0),
                ),
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            )
        ).onGloballyPositioned { size = it.size }
    }

    // 2. Pulse (Sismik Atım) Ölçek Animasyon Yardımcısı
    @Composable
    fun rememberPulseScale(
        targetScale: Float = 1.25f,
        durationMillis: Int = 1000
    ): State<Float> {
        val transition = rememberInfiniteTransition(label = "pulse")
        return transition.animateFloat(
            initialValue = 1f,
            targetValue = targetScale,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    }

    // 3. Yaylanarak Açılan Liste İtemi Giriş Animasyonu Wrapper'ı
    @Composable
    fun SpringEntranceContainer(
        visible: Boolean = true,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            content()
        }
    }
}