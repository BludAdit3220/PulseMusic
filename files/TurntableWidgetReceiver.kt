/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.pulsemusic.music.playback.MusicService

/**
 * Traditional [AppWidgetProvider] receiver for the turntable widget.
 *
 * Uses a [RemoteViews]-based approach (not Glance) so that the radial wave
 * can be drawn on a [android.graphics.Canvas] and pushed as a [android.graphics.Bitmap].
 *
 * Adapted from the Metrolist TurntableWidgetReceiver — action strings are
 * remapped to PulseMusic's existing service action strings, so no new
 * [android.content.IntentFilter] entries are needed in [MusicService].
 */
class TurntableWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Trigger a render using whatever state is already cached.
        // If MusicService is running it will push a fresh update imminently;
        // this path handles cold-start (launcher reboot / widget added) where
        // the service hasn't run yet — we still want something on screen.
        TurntableWidgetRenderer.update(context)

        // Also ask the service to push a full state refresh if it's live.
        if (MusicService.isServiceRunning) {
            context.startService(
                Intent(context, MusicService::class.java).apply {
                    action = ACTION_UPDATE_TURNTABLE_WIDGET
                },
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            // User-initiated control taps — Android allows FGS start from widget PendingIntents
            ACTION_TURNTABLE_PLAY_PAUSE,
            ACTION_TURNTABLE_NEXT,
            ACTION_TURNTABLE_PREVIOUS -> {
                // Map to PulseMusic's existing WIDGET_ action strings so MusicService
                // handles them through the same onStartCommand branch it already has.
                val serviceAction = when (intent.action) {
                    ACTION_TURNTABLE_PLAY_PAUSE -> ACTION_SERVICE_PLAY_PAUSE
                    ACTION_TURNTABLE_NEXT       -> ACTION_SERVICE_SKIP_NEXT
                    ACTION_TURNTABLE_PREVIOUS   -> ACTION_SERVICE_SKIP_PREV
                    else -> return
                }
                try {
                    context.startService(
                        Intent(serviceAction, null, context, MusicService::class.java),
                    )
                } catch (_: Exception) {
                    // Service restricted in background — ignore
                }
            }
        }
    }

    companion object {
        // ── Turntable-specific broadcast actions (sent by widget buttons) ─────
        const val ACTION_TURNTABLE_PLAY_PAUSE = "com.pulsemusic.music.widget.TURNTABLE_PLAY_PAUSE"
        const val ACTION_TURNTABLE_NEXT       = "com.pulsemusic.music.widget.TURNTABLE_NEXT"
        const val ACTION_TURNTABLE_PREVIOUS   = "com.pulsemusic.music.widget.TURNTABLE_PREVIOUS"

        /** Sent to [MusicService] to request a full state push to the widget. */
        const val ACTION_UPDATE_TURNTABLE_WIDGET = "com.pulsemusic.music.widget.UPDATE_TURNTABLE_WIDGET"

        // ── Remapped to PulseMusic's existing service action strings ──────────
        private const val ACTION_SERVICE_PLAY_PAUSE = "com.pulsemusic.music.WIDGET_PLAY_PAUSE"
        private const val ACTION_SERVICE_SKIP_NEXT  = "com.pulsemusic.music.WIDGET_SKIP_NEXT"
        private const val ACTION_SERVICE_SKIP_PREV  = "com.pulsemusic.music.WIDGET_SKIP_PREV"
    }
}
