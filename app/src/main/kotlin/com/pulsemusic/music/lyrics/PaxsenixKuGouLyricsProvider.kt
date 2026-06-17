/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.lyrics

import android.content.Context
import com.pulsemusic.music.constants.EnablePaxsenixKuGouLyricsKey
import com.pulsemusic.music.paxsenix.PaxsenixLyrics
import com.pulsemusic.music.utils.dataStore
import com.pulsemusic.music.utils.get

object PaxsenixKuGouLyricsProvider : LyricsProvider {
    override val name = "Paxsenix: KuGou"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnablePaxsenixKuGouLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> = PaxsenixLyrics.getKugouLyrics(title, artist, duration)

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
