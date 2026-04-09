package com.pulsemusic.innertube.pages

import com.pulsemusic.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
