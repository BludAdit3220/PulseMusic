/**
 * PulseMusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.pulsemusic.music.ui.player

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.Stable

/**
 * Stable wrapper for progress state - reads values only during draw phase
 * This prevents recomposition when position/duration change
 */
@Stable
class ProgressState(
    private val positionState: MutableLongState,
    private val durationState: MutableLongState,
) {
    val progress: Float
        get() {
            val duration = durationState.longValue
            return if (duration > 0) (positionState.longValue.toFloat() / duration).coerceIn(0f, 1f) else 0f
        }

    val position: Long
        get() = positionState.longValue

    val duration: Long
        get() = durationState.longValue
}
