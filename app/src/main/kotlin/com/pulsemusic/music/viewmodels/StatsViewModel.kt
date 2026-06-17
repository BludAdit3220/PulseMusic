/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import com.pulsemusic.music.innertube.YouTube
import com.pulsemusic.music.models.MediaMetadata
import com.pulsemusic.music.constants.HideVideoSongsKey
import com.pulsemusic.music.constants.LastMonthlyMostPlaylistSyncKey
import com.pulsemusic.music.constants.LastWeeklyMostPlaylistSyncKey
import com.pulsemusic.music.constants.ShowMostStatsPlaylistsKey
import com.pulsemusic.music.constants.StatPeriod
import com.pulsemusic.music.constants.statToPeriod
import com.pulsemusic.music.db.MusicDatabase
import com.pulsemusic.music.db.entities.PlaylistEntity
import com.pulsemusic.music.db.entities.ListeningSummary
import com.pulsemusic.music.ui.screens.OptionStats
import com.pulsemusic.music.utils.dataStore
import com.pulsemusic.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel
@Inject
constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase,
) : ViewModel() {

    private val periodicMostPlaylistSyncMutex = Mutex()
    val selectedOption = MutableStateFlow(OptionStats.CONTINUOUS)
    val indexChips = MutableStateFlow(0)

    private val showMostStatsPlaylists =
        context.dataStore.data
            .map { it[ShowMostStatsPlaylistsKey] ?: true }
            .distinctUntilChanged()

    fun onOptionSelected(option: OptionStats) {
        selectedOption.value = option
        indexChips.value = 0
    }

    fun onChipIndexChanged(index: Int) {
        indexChips.value = index
    }

    private fun toTimestamp(selection: OptionStats, t: Int): Long =
        if (selection == OptionStats.CONTINUOUS || t == 0) {
            LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        } else {
            statToPeriod(selection, t - 1)
        }

    val mostPlayedSongsStats =
        combine(
            selectedOption,
            indexChips,
            context.dataStore.data.map { it[HideVideoSongsKey] ?: false }.distinctUntilChanged()
        ) { first, second, third -> Triple(first, second, third) }
            .flatMapLatest { (selection, t, hideVideoSongs) ->
                database
                    .mostPlayedSongsStats(
                        fromTimeStamp = statToPeriod(selection, t),
                        limit = -1,
                        toTimeStamp = toTimestamp(selection, t),
                    ).map { songs ->
                        if (hideVideoSongs) songs.filter { !it.isVideo } else songs
                    }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mostPlayedSongs =
        combine(
            selectedOption,
            indexChips,
            context.dataStore.data.map { it[HideVideoSongsKey] ?: false }.distinctUntilChanged()
        ) { first, second, third -> Triple(first, second, third) }
            .flatMapLatest { (selection, t, hideVideoSongs) ->
                database
                    .mostPlayedSongs(
                        fromTimeStamp = statToPeriod(selection, t),
                        limit = -1,
                        toTimeStamp = toTimestamp(selection, t),
                    ).map { songs ->
                        if (hideVideoSongs) songs.filter { !it.song.isVideo } else songs
                    }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mostPlayedArtists =
        combine(selectedOption, indexChips) { first, second -> Pair(first, second) }
            .flatMapLatest { (selection, t) ->
                database
                    .mostPlayedArtists(
                        statToPeriod(selection, t),
                        limit = -1,
                        toTimeStamp = toTimestamp(selection, t),
                    ).map { artists -> artists.filter { it.artist.isYouTubeArtist } }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mostPlayedAlbums =
        combine(selectedOption, indexChips) { first, second -> Pair(first, second) }
            .flatMapLatest { (selection, t) ->
                database.mostPlayedAlbums(
                    statToPeriod(selection, t),
                    limit = -1,
                    toTimeStamp = toTimestamp(selection, t),
                )
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val listeningByHour =
        combine(selectedOption, indexChips) { opt, idx -> Pair(opt, idx) }
            .flatMapLatest { (selection, t) ->
                database.listeningByHour(
                    fromTimestamp = statToPeriod(selection, t),
                    toTimestamp = toTimestamp(selection, t),
                )
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val listeningByDayOfWeek =
        combine(selectedOption, indexChips) { opt, idx -> Pair(opt, idx) }
            .flatMapLatest { (selection, t) ->
                database.listeningByDayOfWeek(
                    fromTimestamp = statToPeriod(selection, t),
                    toTimestamp = toTimestamp(selection, t),
                )
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val listeningSummary =
        combine(mostPlayedSongsStats, mostPlayedArtists, mostPlayedAlbums) { songs, artists, albums ->
            ListeningSummary(
                totalPlayCount = songs.sumOf { it.songCountListened },
                totalTimeListened = songs.sumOf { it.timeListened ?: 0L },
                uniqueSongsCount = songs.size,
                uniqueArtistsCount = artists.size,
                uniqueAlbumsCount = albums.size,
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, ListeningSummary(0, 0L, 0, 0, 0))

    val firstEvent =
        database
            .firstEvent()
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Artist filter state
    val selectedArtists = mutableStateListOf<MediaMetadata.Artist>()

    val filteredSongs = combine(
        mostPlayedSongsStats,
        snapshotFlow { selectedArtists.toList() }
    ) { songs, selected ->
        if (selected.isEmpty()) {
            songs
        } else {
            songs.filter { song ->
                song.artists.any { artistEntity -> selected.any { it.id != null && it.id == artistEntity.id } }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredArtists = combine(
        mostPlayedArtists,
        snapshotFlow { selectedArtists.toList() }
    ) { artists, selected ->
        if (selected.isEmpty()) artists
        else artists.filter { dbArtist -> selected.any { it.id != null && it.id == dbArtist.artist.id } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredAlbums = combine(
        mostPlayedAlbums,
        snapshotFlow { selectedArtists.toList() }
    ) { albums, selected ->
        if (selected.isEmpty()) albums
        else albums.filter { album ->
            album.artists.any { artistEntity -> selected.any { it.id == artistEntity.id } }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun transferSongStats(fromSongId: String, toSongId: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                database.transferSongStats(fromSongId, toSongId)
                syncMostPlaylistsIfNeeded(force = true)
                onDone?.invoke()
            } catch (t: Throwable) {
                reportException(t)
            }
        }
    }

    val weeklyMostPlaylist =
        showMostStatsPlaylists.flatMapLatest { isEnabled ->
            if (isEnabled) {
                database.playlist(PlaylistEntity.WEEKLY_MOST_PLAYLIST_ID)
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val monthlyMostPlaylist =
        showMostStatsPlaylists.flatMapLatest { isEnabled ->
            if (isEnabled) {
                database.playlist(PlaylistEntity.MONTHLY_MOST_PLAYLIST_ID)
            } else {
                flowOf(null)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val recapPlaylists =
        database
            .playlistsByNameAsc()
            .map { playlists ->
                playlists.filter { playlist ->
                    playlist.playlist.browseId != null &&
                        playlist.playlist.name.contains("recap", ignoreCase = true)
                }
            }.distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun syncMostPlaylistsIfNeeded(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            periodicMostPlaylistSyncMutex.withLock {
                val now = LocalDateTime.now()
                val nowEpochMillis = System.currentTimeMillis()
                val preferences = context.dataStore.data.first()
                val hideVideoSongs = preferences[HideVideoSongsKey] ?: false
                val shouldShowMostStatsPlaylists = preferences[ShowMostStatsPlaylistsKey] ?: true

                if (!shouldShowMostStatsPlaylists) {
                    clearMostPlaylists()
                    return@withLock
                }

                val weeklyPlaylistExists =
                    database.playlist(PlaylistEntity.WEEKLY_MOST_PLAYLIST_ID).first() != null
                val monthlyPlaylistExists =
                    database.playlist(PlaylistEntity.MONTHLY_MOST_PLAYLIST_ID).first() != null

                val shouldSyncWeekly =
                    force || !weeklyPlaylistExists || isWeeklySyncDue(
                        lastSyncMillis = preferences[LastWeeklyMostPlaylistSyncKey],
                        now = now,
                    )
                val shouldSyncMonthly =
                    force || !monthlyPlaylistExists || isMonthlySyncDue(
                        lastSyncMillis = preferences[LastMonthlyMostPlaylistSyncKey],
                        now = now,
                    )

                if (!shouldSyncWeekly && !shouldSyncMonthly) return@withLock

                if (shouldSyncWeekly) {
                    syncMostPlaylist(
                        playlistId = PlaylistEntity.WEEKLY_MOST_PLAYLIST_ID,
                        playlistName = "Weekly Most Played",
                        fromTimeStamp = StatPeriod.WEEK_1.toLocalDateTime(),
                        hideVideoSongs = hideVideoSongs,
                        now = now,
                    )
                }

                if (shouldSyncMonthly) {
                    syncMostPlaylist(
                        playlistId = PlaylistEntity.MONTHLY_MOST_PLAYLIST_ID,
                        playlistName = "Monthly Most Played",
                        fromTimeStamp = StatPeriod.MONTH_1.toLocalDateTime(),
                        hideVideoSongs = hideVideoSongs,
                        now = now,
                    )
                }

                if (!force) {
                    context.dataStore.edit { settings ->
                        if (shouldSyncWeekly) settings[LastWeeklyMostPlaylistSyncKey] = nowEpochMillis
                        if (shouldSyncMonthly) settings[LastMonthlyMostPlaylistSyncKey] = nowEpochMillis
                    }
                }
            }
        }
    }

    private suspend fun clearMostPlaylists() {
        database.withTransaction {
            clearPlaylist(PlaylistEntity.WEEKLY_MOST_PLAYLIST_ID)
            clearPlaylist(PlaylistEntity.MONTHLY_MOST_PLAYLIST_ID)
            delete(PlaylistEntity(id = PlaylistEntity.WEEKLY_MOST_PLAYLIST_ID, name = ""))
            delete(PlaylistEntity(id = PlaylistEntity.MONTHLY_MOST_PLAYLIST_ID, name = ""))
        }
    }

    private fun isWeeklySyncDue(lastSyncMillis: Long?, now: LocalDateTime): Boolean {
        if (lastSyncMillis == null || lastSyncMillis <= 0L) return true
        val lastSyncAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSyncMillis), ZoneId.systemDefault())
        return !lastSyncAt.plusWeeks(1).isAfter(now)
    }

    private fun isMonthlySyncDue(lastSyncMillis: Long?, now: LocalDateTime): Boolean {
        if (lastSyncMillis == null || lastSyncMillis <= 0L) return true
        val lastSyncAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSyncMillis), ZoneId.systemDefault())
        return !lastSyncAt.plusMonths(1).isAfter(now)
    }

    private suspend fun syncMostPlaylist(
        playlistId: String,
        playlistName: String,
        fromTimeStamp: LocalDateTime,
        hideVideoSongs: Boolean,
        now: LocalDateTime,
    ) {
        val songs =
            database
                .mostPlayedSongs(
                    fromTimeStamp = fromTimeStamp.toInstant(ZoneOffset.UTC).toEpochMilli(),
                    limit = -1,
                    toTimeStamp = now.toInstant(ZoneOffset.UTC).toEpochMilli(),
                ).first()
                .let { mostPlayedSongs ->
                    if (hideVideoSongs) mostPlayedSongs.filter { !it.song.isVideo }
                    else mostPlayedSongs
                }.distinctBy { it.song.id }

        val existingPlaylist = database.playlist(playlistId).first()?.playlist
        val playlistEntity =
            existingPlaylist?.copy(
                name = playlistName,
                isEditable = true,
                bookmarkedAt = existingPlaylist.bookmarkedAt ?: now,
                lastUpdateTime = now,
            ) ?: PlaylistEntity(
                id = playlistId,
                name = playlistName,
                isEditable = true,
                bookmarkedAt = now,
                lastUpdateTime = now,
            )

        if (existingPlaylist == null) {
            database.insert(playlistEntity)
        } else {
            database.update(playlistEntity)
        }

        database.clearPlaylist(playlistId)

        val fullPlaylist = database.playlist(playlistId).first()
        if (fullPlaylist != null) {
            database.addSongsToPlaylist(fullPlaylist, songs.map { it.id to null })
        }
    }

    init {
        viewModelScope.launch {
            mostPlayedArtists.collect { artists ->
                artists
                    .map { it.artist }
                    .filter {
                        it.thumbnailUrl == null || Duration.between(
                            it.lastUpdateTime,
                            LocalDateTime.now()
                        ) > Duration.ofDays(10)
                    }.forEach { artist ->
                        YouTube.artist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
        viewModelScope.launch {
            mostPlayedAlbums.collect { albums ->
                albums
                    .filter { it.album.songCount == 0 }
                    .forEach { album ->
                        YouTube
                            .album(album.id)
                            .onSuccess { albumPage ->
                                database.query {
                                    update(album.album, albumPage, album.artists)
                                }
                            }.onFailure {
                                reportException(it)
                                if (it.message?.contains("NOT_FOUND") == true) {
                                    database.query {
                                        delete(album.album)
                                    }
                                }
                            }
                    }
            }
        }
    }
}
