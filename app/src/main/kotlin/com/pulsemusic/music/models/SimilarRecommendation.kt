/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.models

import com.pulsemusic.music.innertube.models.YTItem
import com.pulsemusic.music.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
