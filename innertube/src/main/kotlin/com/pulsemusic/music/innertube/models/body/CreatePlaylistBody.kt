/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.innertube.models.body

import com.pulsemusic.music.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class CreatePlaylistBody(
    val context: Context,
    val title: String,
    val privacyStatus: String = PrivacyStatus.PRIVATE,
    val videoIds: List<String>? = null
) {
    object PrivacyStatus {
        const val PRIVATE = "PRIVATE"
        const val PUBLIC = "PUBLIC"
        const val UNLISTED = "UNLISTED"
    }
}
