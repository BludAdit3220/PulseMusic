/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.lyrics

import android.content.Context
import com.pulsemusic.music.constants.EnablePaxsenixAppleMusicLyricsKey
import com.pulsemusic.music.paxsenix.PaxsenixLyrics
import com.pulsemusic.music.utils.dataStore
import com.pulsemusic.music.utils.get

object PaxsenixAppleMusicLyricsProvider : LyricsProvider {
    override val name = "Paxsenix: Apple Music"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnablePaxsenixAppleMusicLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> = PaxsenixLyrics.getAppleMusicLyrics(title, artist, duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        getLyrics(id, title, artist, album, duration).onSuccess(callback)
    }
}
