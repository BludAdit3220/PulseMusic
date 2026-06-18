/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.playback

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.pulsemusic.music.R
import com.pulsemusic.music.extensions.SilentHandler
import com.pulsemusic.music.widget.MusicWidgetKeys
import com.pulsemusic.music.widget.PlaybackCommandWidget
import com.pulsemusic.music.widget.PlaybackSpotlightWidget
import com.pulsemusic.music.widget.TurntableWidgetRenderer
import com.pulsemusic.music.widget.TurntableWidgetState
import java.io.File

internal class MusicServiceWidgetUpdater(
    private val service: MusicService,
    private val player: Player,
    private val scope: CoroutineScope,
) {
    private var progressJob: Job? = null

    fun update() {
        scope.launch(SilentHandler) {
            pushState()
        }
    }

    fun updateProgressTracking() {
        progressJob?.cancel()
        if (player.isPlaying && player.duration > 0) {
            progressJob = scope.launch(SilentHandler) {
                var lastGlanceUpdate = 0L
                while (isActive && player.isPlaying) {
                    val progress = player.playbackProgress()
                    val now = System.currentTimeMillis()

                    // 1. Update Turntable (RemoteViews) — High frequency for wavy animation
                    TurntableWidgetState.playbackPosition = progress
                    if (player.isPlaying) {
                        TurntableWidgetState.animationPhase = (TurntableWidgetState.animationPhase + 0.3f) % 1000f
                    }
                    TurntableWidgetRenderer.update(service)

                    // 2. Update Glance widgets — Low frequency (1s) to preserve battery
                    if (now - lastGlanceUpdate >= 1000) {
                        updateProgress(progress)
                        lastGlanceUpdate = now
                    }

                    delay(60) // ~16 FPS for fluid "wiggle"
                }
            }
        }
    }

    private suspend fun pushState() {
        val mediaItem = player.currentMediaItem
        val meta = mediaItem?.mediaMetadata
        val artFile = meta?.artworkUri?.let { cacheAlbumArt(it) }
        val dominantColor = artFile?.let { extractDominantColor(it) }
        val snapshot = WidgetSnapshot(
            title = meta?.title?.toString() ?: service.getString(R.string.no_track_playing),
            artist = meta?.artist?.toString().orEmpty(),
            isPlaying = player.isPlaying,
            isAvailable = mediaItem != null,
            playbackPosition = player.playbackProgress(),
            artPath = artFile?.absolutePath,
            dominantColor = dominantColor,
        )

        playbackWidgets.forEach { target ->
            updateWidget(target, snapshot)
        }

        // ── Turntable widget (RemoteViews-based, separate from Glance) ─────
        TurntableWidgetState.isAvailable   = snapshot.isAvailable
        TurntableWidgetState.isPlaying     = snapshot.isPlaying
        TurntableWidgetState.title         = snapshot.title
        TurntableWidgetState.artist        = snapshot.artist
        TurntableWidgetState.dominantColor = snapshot.dominantColor
        TurntableWidgetState.playbackPosition = snapshot.playbackPosition
        // artBitmap: decode from artPath on IO dispatcher
        if (snapshot.artPath != null) {
            val artFile = File(snapshot.artPath)
            if (artFile.exists()) {
                TurntableWidgetState.artBitmap =
                    withContext(Dispatchers.IO) {
                        BitmapFactory.decodeFile(artFile.absolutePath)
                    }
            }
        } else {
            TurntableWidgetState.artBitmap = null
        }
        TurntableWidgetRenderer.update(service)
    }

    private suspend fun updateProgress(progress: Float) {
        TurntableWidgetState.playbackPosition = progress
        TurntableWidgetRenderer.update(service)

        progressWidgets.forEach { target ->
            val ids = GlanceAppWidgetManager(service).getGlanceIds(target.widgetClass)
            ids.forEach { id ->
                updateAppWidgetState(service, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutableWidgetPreferences().apply {
                        this[MusicWidgetKeys.PLAYBACK_POSITION] = progress
                    }
                }
                target.widget.update(service, id)
            }
        }
    }

    private suspend fun updateWidget(
        target: WidgetTarget,
        snapshot: WidgetSnapshot,
    ) {
        TurntableWidgetState.playbackPosition = snapshot.playbackPosition
        TurntableWidgetRenderer.update(service)

        val ids = GlanceAppWidgetManager(service).getGlanceIds(target.widgetClass)
        ids.forEach { id ->
            updateAppWidgetState(service, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutableWidgetPreferences().apply {
                    writeSnapshot(snapshot)
                }
            }
            target.widget.update(service, id)
        }
    }

    private fun Preferences.toMutableWidgetPreferences(): MutablePreferences =
        mutablePreferencesOf().also { mutable ->
            this[MusicWidgetKeys.TRACK_TITLE]?.let { mutable[MusicWidgetKeys.TRACK_TITLE] = it }
            this[MusicWidgetKeys.TRACK_ARTIST]?.let { mutable[MusicWidgetKeys.TRACK_ARTIST] = it }
            this[MusicWidgetKeys.ART_PATH]?.let { mutable[MusicWidgetKeys.ART_PATH] = it }
            this[MusicWidgetKeys.IS_PLAYING]?.let { mutable[MusicWidgetKeys.IS_PLAYING] = it }
            this[MusicWidgetKeys.IS_AVAILABLE]?.let { mutable[MusicWidgetKeys.IS_AVAILABLE] = it }
            this[MusicWidgetKeys.DOMINANT_COLOR]?.let { mutable[MusicWidgetKeys.DOMINANT_COLOR] = it }
            this[MusicWidgetKeys.PLAYBACK_POSITION]?.let { mutable[MusicWidgetKeys.PLAYBACK_POSITION] = it }
        }

    private fun MutablePreferences.writeSnapshot(snapshot: WidgetSnapshot) {
        this[MusicWidgetKeys.TRACK_TITLE] = snapshot.title
        this[MusicWidgetKeys.TRACK_ARTIST] = snapshot.artist
        this[MusicWidgetKeys.IS_PLAYING] = snapshot.isPlaying
        this[MusicWidgetKeys.IS_AVAILABLE] = snapshot.isAvailable
        this[MusicWidgetKeys.PLAYBACK_POSITION] = snapshot.playbackPosition

        val artPath = snapshot.artPath
        if (artPath != null) this[MusicWidgetKeys.ART_PATH] = artPath
        else remove(MusicWidgetKeys.ART_PATH)

        val dominantColor = snapshot.dominantColor
        if (dominantColor != null) this[MusicWidgetKeys.DOMINANT_COLOR] = dominantColor
        else remove(MusicWidgetKeys.DOMINANT_COLOR)
    }

    private suspend fun cacheAlbumArt(uri: Uri): File? = withContext(Dispatchers.IO) {
        val dest = File(service.cacheDir, "widget_art_${Integer.toHexString(uri.toString().hashCode())}.jpg")

        if (uri.scheme == "content" || uri.scheme == "file") {
            return@withContext try {
                service.contentResolver.openInputStream(uri)?.use { src ->
                    dest.outputStream().use { dst -> src.copyTo(dst) }
                }
                if (dest.exists() && dest.length() > 0) dest else null
            } catch (_: Exception) {
                null
            }
        }

        if (uri.scheme == "https" || uri.scheme == "http") {
            return@withContext try {
                val loader = service.applicationContext.imageLoader
                val request = ImageRequest.Builder(service.applicationContext)
                    .data(uri.toString())
                    .size(512, 512)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.image.toBitmap()
                    dest.outputStream().use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, out)
                    }
                    if (dest.exists() && dest.length() > 0) dest else null
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }

        null
    }

    private suspend fun extractDominantColor(file: File): Int? = withContext(Dispatchers.Default) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext null
            val palette = Palette.from(bitmap).generate()
            palette.getDarkVibrantColor(
                palette.getDominantColor(android.graphics.Color.DKGRAY),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun Player.playbackProgress(): Float =
        if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    private data class WidgetSnapshot(
        val title: String,
        val artist: String,
        val isPlaying: Boolean,
        val isAvailable: Boolean,
        val playbackPosition: Float,
        val artPath: String?,
        val dominantColor: Int?,
    )

    private data class WidgetTarget(
        val widgetClass: Class<out GlanceAppWidget>,
        val widget: GlanceAppWidget,
    )

    private companion object {
        val playbackWidgets = listOf(
            WidgetTarget(PlaybackSpotlightWidget::class.java, PlaybackSpotlightWidget()),
            WidgetTarget(PlaybackCommandWidget::class.java, PlaybackCommandWidget()),
        )

        val progressWidgets = listOf(
            WidgetTarget(PlaybackSpotlightWidget::class.java, PlaybackSpotlightWidget()),
            WidgetTarget(PlaybackCommandWidget::class.java, PlaybackCommandWidget()),
        )
    }
}
