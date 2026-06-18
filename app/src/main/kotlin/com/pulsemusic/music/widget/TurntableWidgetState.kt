/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.widget

import android.graphics.Bitmap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Singleton that holds the last-pushed turntable widget state and the live
 * visualizer wave points. Updated by [MusicServiceWidgetUpdater] and the
 * audio session callback; read by [TurntableWidgetRenderer].
 *
 * Using atomics avoids any synchronisation cost on the service thread.
 */
internal object TurntableWidgetState {

    // ── Playback state ─────────────────────────────────────────────────────

    @Volatile var isAvailable: Boolean = false
    @Volatile var isPlaying: Boolean = false
    @Volatile var title: String = ""
    @Volatile var artist: String = ""
    @Volatile var artBitmap: Bitmap? = null
    @Volatile var dominantColor: Int? = null

    /** Current playback progress from 0.0 to 1.0. */
    @Volatile var playbackPosition: Float = 0f

    /** Current animation phase for the wavy indicator. */
    @Volatile var animationPhase: Float = 0f

    fun clearWave() {
        // No longer using visualizer points
    }
}
