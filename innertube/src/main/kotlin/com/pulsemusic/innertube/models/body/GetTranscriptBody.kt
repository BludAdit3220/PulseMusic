package com.pulsemusic.innertube.models.body

import com.pulsemusic.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)
