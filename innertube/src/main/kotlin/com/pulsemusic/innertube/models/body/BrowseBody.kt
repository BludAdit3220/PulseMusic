package com.pulsemusic.innertube.models.body

import com.pulsemusic.innertube.models.Context
import com.pulsemusic.innertube.models.Continuation
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
    val continuation: String?
)
