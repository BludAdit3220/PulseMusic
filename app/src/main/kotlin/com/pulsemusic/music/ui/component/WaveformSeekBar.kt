/**
 * PulseMusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.pulsemusic.music.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.pulsemusic.music.ui.player.ProgressState
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaveformSeekBar(
    progressState: ProgressState,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    enabled: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant,
    waveform: FloatArray? = null, // Optional amplitude data
    squiggly: Boolean = true,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    
    val currentProgress = if (isDragging) dragProgress else progressState.progress
    
    val playbackAnimationFactor by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "PlaybackAnimationFactor"
    )

    // Squiggly animation state
    var phaseOffset by remember { mutableFloatStateOf(0f) }
    val phaseSpeed = 4f // Radians per second
    
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
    val amplitudeMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Amplitude"
    )

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastFrameTime = withFrameMillis { it }
        while (isActive) {
            withFrameMillis { frameTimeMillis ->
                val deltaTime = (frameTimeMillis - lastFrameTime) / 1000f
                phaseOffset += deltaTime * phaseSpeed
                lastFrameTime = frameTimeMillis
            }
        }
    }

    // Default waveform if none provided (simulated)
    val displayWaveform = remember(waveform) {
        waveform ?: FloatArray(100) { index ->
            val t = index.toFloat() / 100f
            (sin(t * PI.toFloat() * 10f) * 0.5f + 0.5f) * 0.8f + 0.2f
        }
    }

    val boxModifier = modifier
        .fillMaxWidth()
        .then(
            if (enabled) {
                Modifier.pointerInput(progressState.duration) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isDragging = true
                            dragProgress = (down.position.x / size.width).coerceIn(0f, 1f)
                            
                            val pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == pointerId }
                                
                                if (change == null || change.changedToUp()) {
                                    isDragging = false
                                    onSeek((dragProgress * progressState.duration).toLong())
                                    break
                                } else {
                                    dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                                    if (change.positionChange() != Offset.Zero) {
                                        change.consume()
                                    }
                                }
                            }
                        }
                    }
                }
            } else Modifier
        )

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val barWidth = 3.dp.toPx()
            val barGap = 2.dp.toPx()
            val totalBarWidth = barWidth + barGap
            val barCount = (width / totalBarWidth).toInt()
            
            val progressPx = currentProgress * width

            for (i in 0 until barCount) {
                val x = i * totalBarWidth
                
                // Get amplitude for this bar
                val waveformIndex = (i.toFloat() / barCount * displayWaveform.size).toInt()
                val baseAmp = displayWaveform[waveformIndex]
                
                // When paused, we want a flat line. When playing, we transition to the waveform height.
                val activeAmp = lerp(0.02f, baseAmp, playbackAnimationFactor)

                // Apply "squiggly" sine wave displacement/animation
                val squigglyAmp = if (squiggly) {
                    val waveValue = sin(i.toFloat() * 0.2f + phaseOffset)
                    // Only apply squiggly effect when playing (via playbackAnimationFactor)
                    activeAmp * (1f + 0.3f * waveValue * playbackAnimationFactor) * (0.9f + 0.1f * amplitudeMultiplier * playbackAnimationFactor)
                } else {
                    activeAmp
                }
                
                // Cap the height to 75% of available height to avoid "going too up"
                val maxBarHeight = height * 0.75f
                val barHeight = (squigglyAmp * maxBarHeight).coerceAtLeast(3.dp.toPx())
                
                val color = if (x <= progressPx) activeColor else inactiveColor
                
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, centerY - barHeight / 2),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
            
            // Draw thumb line (only during drag)
            if (isDragging) {
                val thumbX = progressPx.coerceIn(0f, width)
                drawLine(
                    color = activeColor.copy(alpha = 0.8f),
                    start = Offset(thumbX, 0f),
                    end = Offset(thumbX, height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}
