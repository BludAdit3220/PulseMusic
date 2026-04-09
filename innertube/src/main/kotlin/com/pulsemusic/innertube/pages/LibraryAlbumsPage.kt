package com.pulsemusic.innertube.pages

import com.pulsemusic.innertube.models.Album
import com.pulsemusic.innertube.models.AlbumItem
import com.pulsemusic.innertube.models.Artist
import com.pulsemusic.innertube.models.ArtistItem
import com.pulsemusic.innertube.models.MusicResponsiveListItemRenderer
import com.pulsemusic.innertube.models.MusicTwoRowItemRenderer
import com.pulsemusic.innertube.models.PlaylistItem
import com.pulsemusic.innertube.models.SongItem
import com.pulsemusic.innertube.models.YTItem
import com.pulsemusic.innertube.models.oddElements
import com.pulsemusic.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            return AlbumItem(
                        browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                        playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId ?: return null,
                        title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                        artists = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                        thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
        }
    }
}
