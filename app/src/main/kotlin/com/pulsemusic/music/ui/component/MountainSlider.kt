/**
 * PulseMusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.pulsemusic.music.ui.component

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
    
    // Smooth transition for wave height when pausing/playing
    val waveHeightFactor by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(800),
        label = "WaveHeightFactor"
    )
    
    // Infinite transition for the "dancing" wave effect
    val infiniteTransition = rememberInfiniteTransition(label = "DancingWave")
    
    // Phase for horizontal movement (slight)
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase"
    )
    
    // Main "beat" amplitude that makes the whole wave bounce up and down
    val beatAmplitude by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing), // Faster for a beat feel
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
                        val mappedValue = valueRange.start + dragValue * duration
                        onValueChange(mappedValue)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragValue = (dragValue + dragAmount / size.width).coerceIn(0f, 1f)
                        val mappedValue = valueRange.start + dragValue * duration
                        onValueChange(mappedValue)
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
        // 1. Slim Base Slider (Sharp background track)
        Canvas(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            val centerY = size.height / 2f
            val width = size.width
            val progressX = width * displayValue
            
            // Inactive slim line
            drawLine(
                color = inactiveColor.copy(alpha = 0.15f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Active slim line
            drawLine(
                color = activeColor.copy(alpha = 0.7f),
                start = Offset(0f, centerY),
                end = Offset(progressX, centerY),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 2. Continuous Dancing Wave (Sharp and Clear)
        if (waveHeightFactor > 0.001f) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f
                val progressX = width * displayValue
                
                if (progressX <= 1f) return@Canvas

                val path = Path().apply {
                    moveTo(0f, centerY)
                    
                    val segments = 150 // More segments for a smoother wave
                    for (i in 0..segments) {
                        val x = (i.toFloat() / segments) * progressX
                        
                        // We want a continuous wave from 0 to progressX
                        // The wave consists of several sine components that "dance" together.
                        val freq1 = 0.04f
                        val freq2 = 0.08f
                        val freq3 = 0.02f
                        
                        val sine1 = sin(phase + x * freq1) * 0.2f
                        val sine2 = sin(-phase * 1.5f + x * freq2) * 0.15f
                        val sine3 = sin(phase * 0.5f + x * freq3) * 0.1f
                        
                        // Base height that is constant across the played part
                        val baseHeight = 0.4f
                        
                        // Total height modulated by the fast "beat" and the play/pause factor
                        val h = (baseHeight + sine1 + sine2 + sine3) * (height * 0.6f) * waveHeightFactor * beatAmplitude
                        
                        // lineTo follows the dancing path
                        lineTo(x, centerY - h.coerceAtLeast(0f))
                    }
                    
                    // Connect back to the line at the thumb position
                    lineTo(progressX, centerY)
                    close()
                }

                // Fill with a visible, solid-ish color (no blur)
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.4f * waveHeightFactor),
                    style = Fill
                )
            }
        }

        // 3. Seeker Thumb (Sharp White Pill)
        Canvas(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            val centerY = size.height / 2f
            val progressX = size.width * displayValue
            
            val pillWidth = 4.dp.toPx()
            val pillHeight = 18.dp.toPx()
            
            drawRoundRect(
                color = Color.White.copy(alpha = 0.95f),
                topLeft = Offset(progressX - pillWidth / 2f, centerY - pillHeight / 2f),
                size = Size(pillWidth, pillHeight),
                cornerRadius = CornerRadius(pillWidth / 2f)
            )
        }
    }
}
