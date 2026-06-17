/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.innertube.pages

import com.pulsemusic.music.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
