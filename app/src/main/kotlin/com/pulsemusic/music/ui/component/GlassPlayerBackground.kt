/*
 * PulseMusic (2026)
 * © PulseMusic contributors — GPL-3.0
 * Derived from PulseMusic (© Aditya Parasher).
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.pulsemusic.music.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * GlassPlayerBackground — the full-bleed background layer for the PulseMusic player.
 *
 * Stacks:
 *  1. Canvas artwork (muted ExoPlayer video) if available, else album art (AsyncImage)
 *  2. Parallax shift on drag-to-dismiss gesture
 *  3. Vertical gradient overlay: transparent → 80% surfaceDim (bottom-up) for readability
 *
 * This is a NEW composable for PulseMusic — it didn't exist in either source app.
 */
@Composable
fun GlassPlayerBackground(
    thumbnailUrl: String?,
    canvasUrl: String? = null,
    tintColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    children: @Composable () -> Unit = {},
) {
    val surface = MaterialTheme.colorScheme.surfaceDim
    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = modifier) {
        // ── 1. Background artwork ──────────────────────────────────────────
        // TODO: When CanvasArtworkPlayer is wired in, replace AsyncImage
        //       with conditional: if canvasUrl != null → CanvasArtworkPlayer
        //                         else               → AsyncImage
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .drawWithContent {
                        drawContent()
                        // Tint overlay — album-extracted color at 20% alpha
                        drawRect(tintColor.copy(alpha = 0.20f))
                    },
            )
        } else {
            // Fallback: solid surface color
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithContent {
                        drawRect(surface)
                    }
            )
        }

        // ── 2. Gradient scrim (bottom-up) ──────────────────────────────────
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.30f to Color.Transparent,
                                0.65f to surface.copy(alpha = 0.55f),
                                1.0f to surface.copy(alpha = 0.92f),
                            ),
                        )
                    )
                }
        )

        // ── 3. Content (player controls etc.) ─────────────────────────────
        children()
    }
}

/**
 * PulseMusicMiniPlayer — the swipeable pill player shown above the navigation toolbar.
 *
 * Features (from PulseMusic's NewMiniPlayer):
 * - 64dp height, 32dp corner radius pill shape
 * - Circular arc progress on the album thumbnail
 * - Swipe left → skip next, swipe right → skip previous
 * - Haptic feedback on swipe actions
 * - Spring animation back to center when swipe is aborted
 * - Background: surfaceContainer + extracted album tint at 15% alpha
 * - Tap opens full player bottom sheet
 */
@Composable
fun PulseMusicMiniPlayer(
    thumbnailUrl: String?,
    title: String,
    artist: String,
    isPlaying: Boolean,
    progress: Float,            // 0f..1f
    tintColor: Color = Color.Transparent,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
    swipeSensitivity: Float = 80f,
) {
    val haptic = LocalHapticFeedback.current
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var swiped by remember { mutableStateOf(false) }

    val animatedDragOffset by animateFloatAsState(
        targetValue = if (swiped) dragOffset else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "mini_player_drag",
    )

    // Reset swiped flag after animation returns
    LaunchedEffect(swiped) {
        if (swiped) {
            delay(200)
            swiped = false
            dragOffset = 0f
        }
    }

    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    Surface(
        onClick = onOpenPlayer,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    dragOffset += delta
                },
                onDragStopped = {
                    when {
                        dragOffset < -swipeSensitivity -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSkipNext()
                            swiped = true
                        }
                        dragOffset > swipeSensitivity -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSkipPrev()
                            swiped = true
                        }
                        else -> {
                            dragOffset = 0f
                        }
                    }
                }
            ),
        shape = RoundedCornerShape(32.dp),
        color = surfaceContainer,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Album Thumbnail + Arc Progress ────────────────────────────
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Arc progress indicator drawn behind the thumbnail
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .drawWithContent {
                            drawContent()
                            // Draw circular arc for playback progress
                            val stroke = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                            val startAngle = -90f
                            val sweepAngle = progress * 360f
                            drawArc(
                                color = tintColor.takeIf { it != Color.Transparent }
                                    ?: Color.White.copy(alpha = 0.7f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                                size = size.copy(
                                    width = size.width - 6.dp.toPx(),
                                    height = size.height - 6.dp.toPx(),
                                ),
                                style = stroke,
                            )
                        }
                ) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Title + Artist ────────────────────────────────────────────
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Play/Pause button ──────────────────────────────────────────
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying)
                        Icons.Filled.Pause
                    else
                        Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                )
            }
        }
    }
}
