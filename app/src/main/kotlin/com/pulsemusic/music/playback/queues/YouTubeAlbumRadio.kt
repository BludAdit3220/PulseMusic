/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.playback.queues

import androidx.media3.common.MediaItem
import com.pulsemusic.music.innertube.YouTube
import com.pulsemusic.music.innertube.models.WatchEndpoint
import com.pulsemusic.music.extensions.toMediaItem
import com.pulsemusic.music.models.MediaMetadata
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeAlbumRadio(
    internal val playlistId: String,
) : Queue {
    override val preloadItem: MediaMetadata? = null

    private val endpoint: WatchEndpoint
        get() = WatchEndpoint(
            playlistId = playlistId,
            params = "wAEB"
        )

    internal var albumSongCount = 0
    internal var continuation: String? = null
    internal var firstTimeLoaded: Boolean = false

    internal constructor(
        playlistId: String,
        albumSongCount: Int,
        continuation: String?,
        firstTimeLoaded: Boolean,
    ) : this(playlistId) {
        this.albumSongCount = albumSongCount
        this.continuation = continuation
        this.firstTimeLoaded = firstTimeLoaded
    }

    override suspend fun getInitialStatus(): Queue.Status = withContext(IO) {
        val albumSongs = YouTube.albumSongs(playlistId).getOrThrow()
        albumSongCount = albumSongs.size
        Queue.Status(
            title = albumSongs.first().album?.name.orEmpty(),
            items = albumSongs.map { it.toMediaItem() },
            mediaItemIndex = 0
        )
    }

    override fun hasNextPage(): Boolean = !firstTimeLoaded || continuation != null

    override suspend fun nextPage(): List<MediaItem> = withContext(IO) {
        val nextResult = YouTube.next(endpoint, continuation).getOrThrow()
        continuation = nextResult.continuation
        if (!firstTimeLoaded) {
            firstTimeLoaded = true
            nextResult.items.subList(albumSongCount, nextResult.items.size).map { it.toMediaItem() }
        } else {
            nextResult.items.map { it.toMediaItem() }
        }
    }
}
