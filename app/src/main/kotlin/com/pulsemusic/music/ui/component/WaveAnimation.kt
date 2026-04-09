/**
 * PulseMusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.pulsemusic.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class WaveType {
    BLOB,
    SQUARE_BARS,
    CIRCULAR_BARS
}

@Composable
fun WaveAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    intensity: Float = 1f,
    type: WaveType = WaveType.BLOB,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val wavePhaseState = if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (Math.PI * 2).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wavePhase"
        )
    } else {
        rememberUpdatedState(0f)
    }

    // Dynamic color shifting for an "alive" feel
    val colorAnimState = if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "colorAnim"
        )
    } else {
        rememberUpdatedState(0f)
    }

    // Add a "breathing" effect to the base radius
    val breathingFactorState = if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathing"
        )
    } else {
        rememberUpdatedState(1f)
    }

    val path1 = remember { Path() }
    val path2 = remember { Path() }
    val path3 = remember { Path() }
    val paths = listOf(path1, path2, path3)

    Canvas(
        modifier = modifier.then(
            if (type == WaveType.BLOB) Modifier.blur(if (intensity > 1.5f) 12.dp else 8.dp)
            else Modifier
        )
    ) {
        val wavePhase = wavePhaseState.value
        val colorAnim = colorAnimState.value
        val breathingFactor = breathingFactorState.value

        val currentColor = lerp(color, secondaryColor, colorAnim)

        when (type) {
            WaveType.BLOB -> {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val maxRadius = size.width.coerceAtMost(size.height) / 2

                for (i in 0 until 3) {
                    val phaseOffset = i * (Math.PI.toFloat() * 2 / 3)
                    val path = paths[i]
                    path.reset()
                    
                    val pulse = if (isPlaying) {
                        sin(wavePhase * 0.5f + phaseOffset) * (0.08f * intensity)
                    } else 0f

                    val steps = 32
                    for (step in 0..steps) {
                        val angle = (step.toFloat() / steps) * (Math.PI * 2).toFloat()
                        
                        val n1 = sin(angle * 2 + wavePhase + phaseOffset) * (0.12f * intensity)
                        val n2 = cos(angle * 4 - wavePhase * 0.7f + phaseOffset * 0.5f) * (0.06f * intensity)
                        
                        val baseRadiusFactor = ((0.85f - (i * 0.1f)) + (0.05f * intensity)) * breathingFactor
                        val currentRadius = maxRadius * (baseRadiusFactor + n1 + n2 + pulse)

                        val x = centerX + currentRadius * cos(angle)
                        val y = centerY + currentRadius * sin(angle)

                        if (step == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    path.close()

                    val alpha = (0.45f - i * 0.12f).coerceAtLeast(0.05f)
                    val layerColor = if (i % 2 == 0) currentColor else color
                    
                    drawPath(
                        path = path,
                        color = layerColor.copy(alpha = alpha),
                    )
                }
            }
            WaveType.SQUARE_BARS -> {
                val barCount = 12
                val barSpacing = 8.dp.toPx()
                val totalSpacing = barSpacing * (barCount - 1)
                val barWidth = (size.width - totalSpacing) / barCount
                val maxHeight = size.height

                for (i in 0 until barCount) {
                    val progress = i.toFloat() / barCount
                    val heightFactor = if (isPlaying) {
                        val phase = wavePhase + progress * Math.PI.toFloat() * 2
                        (sin(phase) * 0.4f + 0.6f) * intensity
                    } else {
                        0.2f
                    }
                    
                    val barHeight = (maxHeight * heightFactor).coerceIn(4.dp.toPx(), maxHeight)
                    val x = i * (barWidth + barSpacing)
                    val y = (maxHeight - barHeight) / 2

                    val barColor = lerp(color, secondaryColor, (sin(wavePhase + progress * 5f) * 0.5f + 0.5f))

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
            WaveType.CIRCULAR_BARS -> {
                val barCount = 48
                val centerX = size.width / 2
                val centerY = size.height / 2
                // Bars start at 80% of the radius and extend to 100%
                val innerRadius = (size.width.coerceAtMost(size.height) / 2) * 0.8f
                val maxBarHeight = (size.width.coerceAtMost(size.height) / 2) * 0.2f

                for (i in 0 until barCount) {
                    val progress = i.toFloat() / barCount
                    // Base angle is static relative to the canvas; parent handles rotation
                    val angle = (progress * 2 * Math.PI.toFloat())
                    
                    val heightFactor = if (isPlaying) {
                        // Reactive animation of the bars' height
                        (sin(wavePhase * 2f + progress * Math.PI.toFloat() * 8) * 0.4f + 0.6f) * intensity
                    } else {
                        0.1f
                    }
                    val barHeight = maxBarHeight * heightFactor
                    
                    val startX = centerX + innerRadius * cos(angle)
                    val startY = centerY + innerRadius * sin(angle)
                    val endX = centerX + (innerRadius + barHeight) * cos(angle)
                    val endY = centerY + (innerRadius + barHeight) * sin(angle)

                    val barColor = lerp(color, secondaryColor, (sin(wavePhase + progress * 5f) * 0.5f + 0.5f))

                    drawLine(
                        color = barColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
