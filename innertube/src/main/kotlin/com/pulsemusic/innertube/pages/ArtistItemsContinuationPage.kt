package com.pulsemusic.innertube.pages

import com.pulsemusic.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
