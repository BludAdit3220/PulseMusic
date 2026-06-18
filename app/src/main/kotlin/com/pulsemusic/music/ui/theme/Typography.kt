/*
 * PulseMusic (2026)
 * © PulseMusic contributors — GPL-3.0
 * Derived from PulseMusic (© Aditya Parasher).
 */

package com.pulsemusic.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.pulsemusic.music.R

// ─── Google Fonts Provider ────────────────────────────────────────────────────
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

// ─── Font Families ────────────────────────────────────────────────────────────

/** DM Serif Display — used for player titles, Wrapped stats, hero text */
val DmSerifDisplay = FontFamily(
    Font(
        googleFont = GoogleFont("DM Serif Display"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("DM Serif Display"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
    ),
)

/** Plus Jakarta Sans — clean warm body typeface for all UI text */
val PlusJakartaSans = FontFamily(
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Light,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.SemiBold,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("Plus Jakarta Sans"),
        fontProvider = googleFontProvider,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Normal,
    ),
)

/** JetBrains Mono — used for lyrics timestamps, playback time displays */
val JetBrainsMono = FontFamily(
    Font(
        googleFont = GoogleFont("JetBrains Mono"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("JetBrains Mono"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = GoogleFont("JetBrains Mono"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold,
        style = FontStyle.Normal,
    ),
)


/** BBH Bartle — decorative display font used in Wrapped screens */
val bbh_bartle = FontFamily(
    Font(R.font.bbh_bartle_regular, weight = FontWeight.Normal),
)

/** Alias for bbhBartle (camelCase import used in WrappedIntro) */
val bbhBartle = bbh_bartle

// ─── PulseMusic Typography ──────────────────────────────────────────────────────
/**
 * PulseMusic typography system:
 * - Display / Headline: DM Serif Display — warm editorial feel for hero moments
 * - Body / UI: Plus Jakarta Sans — readable, slightly warm, modern
 * - Monospace: JetBrains Mono — precise timestamps and technical UI
 */
val PulseMusicTypography = Typography(
    // ── Display ──────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    // ── Headline ─────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    // ── Title ─────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // ── Body ──────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // ── Label ─────────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/** System font fallback — used when user disables custom fonts in settings */
val SystemTypography = Typography()
