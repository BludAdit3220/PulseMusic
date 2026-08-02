/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pulsemusic.music.MainActivity
import com.pulsemusic.music.R
import com.pulsemusic.music.musicrecognition.ACTION_MUSIC_RECOGNITION

/**
 * A one-tap "Shazam-style" home screen widget. Tapping it anywhere launches
 * PulseMusic straight into the music recognition screen and immediately starts
 * listening, via the same [ACTION_MUSIC_RECOGNITION] intent used by the quick
 * settings tile.
 */
class MusicRecognitionWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    GlanceTheme.colors
                } else {
                    PulseMusicWidgetColors.providers
                },
            ) {
                MusicRecognitionContent(context)
            }
        }
    }

    companion object {
        val SongTitleKey = stringPreferencesKey("song_title")
        val ArtistNameKey = stringPreferencesKey("artist_name")
    }
}

@Composable
private fun MusicRecognitionContent(context: Context) {
    val size = LocalSize.current
    val isCompact = size.width < 150.dp
    val prefs = currentState<Preferences>()
    val songTitle = prefs[MusicRecognitionWidget.SongTitleKey]
    val artistName = prefs[MusicRecognitionWidget.ArtistNameKey]
    
    val action = openMusicRecognitionAction(context)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(24.dp)
            .clickable(action),
        contentAlignment = Alignment.Center,
    ) {
        if (songTitle != null && artistName != null) {
            RecognizedSongContent(songTitle, artistName, isCompact)
        } else {
            DefaultRecognitionContent(context, isCompact)
        }
    }
}

@Composable
private fun RecognizedSongContent(
    title: String,
    artist: String,
    isCompact: Boolean
) {
    if (isCompact) {
        Column(
            modifier = GlanceModifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecognitionMicBadge()
            Spacer(modifier = GlanceModifier.size(4.dp))
            Text(
                text = title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                maxLines = 1,
            )
        }
    } else {
        Row(
            modifier = GlanceModifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecognitionMicBadge()
            Spacer(modifier = GlanceModifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = artist,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun DefaultRecognitionContent(context: Context, isCompact: Boolean) {
    if (isCompact) {
        Column(
            modifier = GlanceModifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecognitionMicBadge()
            Spacer(modifier = GlanceModifier.size(6.dp))
            Text(
                text = context.getString(R.string.music_recognition),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                maxLines = 2,
            )
        }
    } else {
        Row(
            modifier = GlanceModifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecognitionMicBadge()
            Spacer(modifier = GlanceModifier.width(12.dp))
            Column {
                Text(
                    text = context.getString(R.string.music_recognition),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = context.getString(R.string.widget_tap_to_identify_song),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun RecognitionMicBadge() {
    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.mic),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
            modifier = GlanceModifier.size(20.dp),
        )
    }
}

private fun openMusicRecognitionAction(context: Context): Action =
    actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_MUSIC_RECOGNITION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
    )
