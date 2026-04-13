package com.pulsemusic.music

/**
 * PulseMusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun MountainSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    enabled: Boolean = true,
    isPlaying: Boolean = false,
) {
    val duration = valueRange.endInclusive - valueRange.start
    val normalizedValue = if (duration > 0) (value - valueRange.start) / duration else 0f

    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(normalizedValue) }

    val displayValue = if (isDragging) dragValue else normalizedValue

    val activeColor = colors.activeTrackColor
    val inactiveColor = colors.inactiveTrackColor

    // Wave entry/exit animation
    val waveHeightFactor by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(1000),
        label = "WaveHeightFactor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "DancingWave")

    // Smooth horizontal phase shifts for each harmonic
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "P1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "P2"
    )

    // Rhythm/Beat amplitude (DJ pulse effect)
    val beatAmplitude by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Beat"
    )

    val interactiveModifier = if (enabled) {
        modifier
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val newValue = (offset.x / size.width).coerceIn(0f, 1f)
                    val mappedValue = valueRange.start + newValue * duration
                    onValueChange(mappedValue)
                    onValueChangeFinished?.invoke()
                }
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragValue = (offset.x / size.width).coerceIn(0f, 1f)
                        onValueChange(valueRange.start + dragValue * duration)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragValue = (dragValue + dragAmount / size.width).coerceIn(0f, 1f)
                        onValueChange(valueRange.start + dragValue * duration)
                    }
                )
            }
    } else {
        modifier
    }

    Box(
        modifier = interactiveModifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Slim Base Track
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)) {
            val centerY = size.height / 2f
            val progressX = size.width * displayValue

            drawLine(
                color = inactiveColor.copy(alpha = 0.15f),
                start = Offset(0f, centerY), end = Offset(size.width, centerY),
                strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round
            )
            drawLine(
                color = activeColor.copy(alpha = 0.7f),
                start = Offset(0f, centerY), end = Offset(progressX, centerY),
                strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round
            )
        }

        // 2. Continuous Dancing Wave
        if (waveHeightFactor > 0.001f) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)) {
                val height = size.height
                val centerY = height / 2f
                val progressX = size.width * displayValue

                if (progressX <= 1f) return@Canvas

                val path = Path().apply {
                    moveTo(0f, centerY)

                    // Use a FIXED step size in pixels to prevent "snapping" or stretching
                    val stepPx = 3f
                    var x = 0f
                    while (x <= progressX) {
                        // Envelope: Creates the "Mountain" hump from start to thumb
                        // This ensures the wave starts at 0 and ends at 0 at the thumb
                        val t = x / progressX
                        val envelope = sin(PI * t).pow(0.8).toFloat()

                        // Dancing Harmonics: Using absolute 'x' ensures they don't slide as the thumb moves
                        val wave1 = sin(x * 0.05f + phase1) * 0.25f
                        val wave2 = sin(x * 0.08f + phase2) * 0.15f
                        val wave3 = sin(x * 0.02f + (phase1 + phase2) * 0.5f) * 0.1f

                        val totalWave = (0.4f + wave1 + wave2 + wave3) * (height * 0.65f)
                        val h = totalWave * envelope * waveHeightFactor * beatAmplitude

                        lineTo(x, centerY - h.coerceAtLeast(0f))
                        x += stepPx
                    }
                    lineTo(progressX, centerY)
                    close()
                }

                drawPath(path = path, color = Color.White.copy(alpha = 0.45f), style = Fill)
            }
        }

        // 3. Seeker Thumb
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)) {
            val centerY = size.height / 2f
            val progressX = size.width * displayValue
            val pillWidth = 4.dp.toPx()
            val pillHeight = 18.dp.toPx()

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(progressX - pillWidth / 2f, centerY - pillHeight / 2f),
                size = Size(pillWidth, pillHeight),
                cornerRadius = CornerRadius(pillWidth / 2f)
            )
        }
    }
}