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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class WaveType {
    BLOB,
    SQUARE_BARS,
    CIRCULAR_RING
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
            WaveType.CIRCULAR_RING -> {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val minSize = size.width.coerceAtMost(size.height)
                val baseRadius = minSize / 2 * 0.82f
                val maxAmplitude = minSize / 2 * 0.18f

                // Render 72 thick bars for a high-density premium look
                val barCount = 72
                val angleStep = (2.0 * Math.PI / barCount).toFloat()

                for (i in 0 until barCount) {
                    val angle = i * angleStep
                    
                    // High-speed procedural height calculation
                    // Sum of different frequencies for "punchy" complex motion
                    val speedFactor = 2.5 // Increased speed
                    val wave1 = sin(angle * 2.0 + wavePhase * speedFactor)
                    val wave2 = cos(angle * 4.0 - wavePhase * speedFactor * 1.5)
                    val wave3 = sin(angle * 1.0 + wavePhase * speedFactor * 0.7)
                    
                    val combined = (wave1 * 0.4 + wave2 * 0.3 + wave3 * 0.3).coerceIn(-1.0, 1.0)
                    val normalizedHeight = (combined * 0.5 + 0.5) * (if (isPlaying) 1.0 else 0.15)
                    
                    val barLen = maxAmplitude * normalizedHeight.toFloat() * intensity
                    
                    // Ensure a minimum visible height even at 0 amplitude
                    val effectiveBarLen = barLen.coerceAtLeast(2.dp.toPx())
                    
                    val startR = baseRadius
                    val endR = baseRadius + effectiveBarLen
                    
                    val startX = centerX + startR * cos(angle.toDouble()).toFloat()
                    val startY = centerY + startR * sin(angle.toDouble()).toFloat()
                    val endX = centerX + endR * cos(angle.toDouble()).toFloat()
                    val endY = centerY + endR * sin(angle.toDouble()).toFloat()
                    
                    // Main bar stroke
                    drawLine(
                        brush = Brush.sweepGradient(
                            colors = listOf(currentColor, secondaryColor, currentColor),
                            center = Offset(centerX, centerY)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 4.dp.toPx(), // Thicker bars
                        cap = StrokeCap.Round
                    )
                    
                    // Subtle glow/shadow behind the bar
                    drawLine(
                        color = currentColor.copy(alpha = 0.2f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 10.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
