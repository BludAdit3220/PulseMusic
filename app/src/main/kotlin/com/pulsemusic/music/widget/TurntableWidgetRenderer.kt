/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import androidx.palette.graphics.Palette
import com.pulsemusic.music.MainActivity
import com.pulsemusic.music.R

/**
 * Stateless renderer: takes the current [TurntableWidgetState] snapshot and
 * pushes a [RemoteViews] update to every turntable widget instance.
 *
 * This is called from:
 *  - [TurntableWidgetReceiver.onUpdate] (system-requested refresh)
 *  - [MusicServiceWidgetUpdater] after any playback state change
 *  - The visualizer callback when new wave data arrives
 */
internal object TurntableWidgetRenderer {

    /**
     * Renders and pushes the widget to all registered instances.
     * Safe to call from any thread — [AppWidgetManager.updateAppWidget] is thread-safe.
     */
    fun update(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, TurntableWidgetReceiver::class.java),
        )
        if (ids.isEmpty()) return

        val sizePx = resolveSizePx(context, manager, ids.first())
        val bmp = buildBitmap(sizePx)
        val views = buildRemoteViews(context, bmp)

        for (id in ids) {
            manager.updateAppWidget(id, views)
        }

        // Recycle after push — RemoteViews has already parcelled the bitmap
        bmp.recycle()
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun buildBitmap(sizePx: Int): Bitmap {
        val state = TurntableWidgetState
        val waveColor = resolveWaveColor(state.dominantColor)

        return TurntableCanvasRenderer.render(
            artBitmap = state.artBitmap,
            progress = state.playbackPosition,
            isPlaying = state.isPlaying,
            animationPhase = state.animationPhase,
            color = waveColor,
            sizePx = sizePx,
        )
    }

    private fun buildRemoteViews(context: Context, bitmap: Bitmap): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_turntable)
        val state = TurntableWidgetState

        // ── Album art canvas ─────────────────────────────────────────────────
        views.setImageViewBitmap(R.id.turntable_canvas, bitmap)

        // ── Play/pause icon ──────────────────────────────────────────────────
        views.setImageViewResource(
            R.id.turntable_play_pause,
            if (state.isPlaying) R.drawable.ic_widget_pause_secondary else R.drawable.ic_widget_play_secondary,
        )

        // ── Pending intents ──────────────────────────────────────────────────
        views.setOnClickPendingIntent(
            R.id.turntable_canvas,
            openAppIntent(context),
        )
        views.setOnClickPendingIntent(
            R.id.turntable_play_pause_container,
            widgetActionIntent(context, TurntableWidgetReceiver.ACTION_TURNTABLE_PLAY_PAUSE),
        )
        views.setOnClickPendingIntent(
            R.id.turntable_prev,
            widgetActionIntent(context, TurntableWidgetReceiver.ACTION_TURNTABLE_PREVIOUS),
        )
        views.setOnClickPendingIntent(
            R.id.turntable_next,
            widgetActionIntent(context, TurntableWidgetReceiver.ACTION_TURNTABLE_NEXT),
        )

        return views
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun widgetActionIntent(context: Context, action: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            Intent(action).setClass(context, TurntableWidgetReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /**
     * Derives the wave colour from the album art dominant colour, matching
     * how [rememberWidgetPalette] works in the Glance widgets.
     * Falls back to a neutral purple-grey if no dominant colour is available.
     */
    private fun resolveWaveColor(dominantColor: Int?): Int {
        if (dominantColor == null) return Color.argb(255, 160, 140, 200)
        // Blend dominant toward white slightly (mirrors the secondaryContainer logic)
        val r = ((Color.red(dominantColor) * 0.6f) + (255 * 0.4f)).toInt().coerceIn(0, 255)
        val g = ((Color.green(dominantColor) * 0.6f) + (255 * 0.4f)).toInt().coerceIn(0, 255)
        val b = ((Color.blue(dominantColor) * 0.6f) + (255 * 0.4f)).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    /**
     * Reads the widget's actual allocated size (API 16+) so the rendered
     * bitmap is always sharp rather than upscaled from a fixed resolution.
     */
    private fun resolveSizePx(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
    ): Int {
        val options: Bundle = manager.getAppWidgetOptions(widgetId)
        val minDp = minOf(
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 160),
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 160),
        ).coerceAtLeast(80)
        val density = context.resources.displayMetrics.density
        return (minDp * density).toInt().coerceIn(240, 600)
    }
}
