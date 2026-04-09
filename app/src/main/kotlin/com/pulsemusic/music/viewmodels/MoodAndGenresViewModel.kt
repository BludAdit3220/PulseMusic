/**
 * PulseMusic Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.pulsemusic.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsemusic.innertube.YouTube
import com.pulsemusic.innertube.pages.MoodAndGenres
import com.pulsemusic.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodAndGenresViewModel
@Inject
constructor() : ViewModel() {
    val moodAndGenres = MutableStateFlow<List<MoodAndGenres>?>(null)

    init {
        viewModelScope.launch {
            YouTube
                .moodAndGenres()
                .onSuccess {
                    moodAndGenres.value = it
                }.onFailure {
                    reportException(it)
                }
        }
    }
}
