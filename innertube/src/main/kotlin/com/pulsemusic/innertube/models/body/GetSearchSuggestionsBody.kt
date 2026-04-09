package com.pulsemusic.innertube.models.body

import com.pulsemusic.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetSearchSuggestionsBody(
    val context: Context,
    val input: String,
)
