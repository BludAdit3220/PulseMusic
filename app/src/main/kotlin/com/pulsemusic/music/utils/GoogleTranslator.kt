/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import org.json.JSONArray
import timber.log.Timber

object GoogleTranslator {
    private val client = HttpClient(OkHttp)
    private const val BASE_URL = "https://translate.googleapis.com/translate_a/single"

    /**
     * Translates text to the target language using Google Translate's free endpoint.
     * Supports regional languages like Bhojpuri (bho), Maithili (mai), etc.
     */
    suspend fun translate(text: String, targetLangCode: String): String {
        if (text.isBlank()) return ""
        
        val lang = normalizeLanguageCode(targetLangCode)
        
        return try {
            val response = client.get(BASE_URL) {
                parameter("client", "gtx")
                parameter("sl", "auto")
                parameter("tl", lang)
                parameter("dt", "t")
                parameter("q", text)
            }
            
            val raw = response.bodyAsText()
            parseTranslationResponse(raw)
        } catch (e: Exception) {
            Timber.tag("GoogleTranslator").e(e, "Translation failed for lang: $lang")
            text // Fallback to original text
        }
    }

    private fun parseTranslationResponse(raw: String): String {
        return try {
            val json = JSONArray(raw)
            val parts = json.optJSONArray(0) ?: return ""
            val result = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.optJSONArray(i)
                val translatedText = part?.optString(0)
                if (translatedText != null) {
                    result.append(translatedText)
                }
            }
            result.toString()
        } catch (e: Exception) {
            Timber.tag("GoogleTranslator").e(e, "Failed to parse response: $raw")
            ""
        }
    }

    /**
     * Maps common names or uppercase codes to ISO codes if necessary.
     */
    private fun normalizeLanguageCode(code: String): String {
        val normalized = code.lowercase().trim()
        return when (normalized) {
            "english" -> "en"
            "japanese" -> "ja"
            "spanish" -> "es"
            "chinese" -> "zh"
            "chinese_simplified" -> "zh-CN"
            "chinese_traditional" -> "zh-TW"
            "korean" -> "ko"
            "french" -> "fr"
            "german" -> "de"
            "russian" -> "ru"
            "italian" -> "it"
            "portuguese" -> "pt"
            "hindi" -> "hi"
            "bhojpuri" -> "bho"
            "maithili" -> "mai"
            "magahi" -> "mag"
            "bajjika" -> "mai" // Fallback to Maithili for Bajjika if not directly supported
            else -> if (normalized.length <= 3) normalized else "en"
        }
    }
}
