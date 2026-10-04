package com.ferdidrgn.anlikdepremler.ui.web

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ferdidrgn.anlikdepremler.ui.theme.magnitudeHeatColor
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

/**
 * The web dashboard's visual signature: the shared USGS-style magnitude heat scale
 * ([magnitudeHeatColor]) plus a seismograph waveform motif, used instead of generic Material
 * default colors/dots so the app reads as a seismic monitoring tool rather than a stock SaaS
 * dashboard template.
 */

/** An epicenter-style radar ping instead of a static dot for the "live" indicator. */
@Composable
fun PulsingLiveDot(color: Color = Color(0xFF22C55E), modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "livePulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "pulseScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.size(10.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * A continuously animated seismograph strip-chart trace - the dashboard's signature decorative
 * motif, grounded in the actual subject matter instead of a generic abstract shape.
 */
@Composable
fun SeismicWaveform(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF22C55E),
    strokeWidth: Dp = 1.5.dp
) {
    val transition = rememberInfiniteTransition(label = "seismicTrace")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "tracePhase"
    )

    Canvas(modifier = modifier) {
        val midY = size.height / 2f
        val amplitude = size.height * 0.42f
        val segments = 140
        val path = Path()

        for (i in 0..segments) {
            val t = i.toFloat() / segments * 10f + phase
            val y = midY + (sin(t) * 0.5f + sin(t * 2.7f) * 0.3f + sin(t * 5.3f) * 0.2f) * amplitude
            val x = size.width * i / segments
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

/** Fades + slides an item in, offset by [index] so a list reveals in a staggered cascade. */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    maxDelaySteps: Int = 10,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((minOf(index, maxDelaySteps) * 55L).milliseconds)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 4 },
        modifier = modifier
    ) {
        content()
    }
}
