package com.pulsemusic.music.ui.screens.wrapped

import android.content.Context
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.pulsemusic.music.R

object WrappedArtworkGenerator {
    fun generate(context: Context, year: Int = WrappedConstants.YEAR): Bitmap {
        val size = 1024
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background Gradient (PulseMusic Brand Colors)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                intArrayOf(0xFF8B6914.toInt(), 0xFF1A1410.toInt(), 0xFF0F0C0A.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        // Decorative geometric shapes (subtle)
        val shapePaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 15
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawCircle(size * 0.8f, size * 0.2f, size * 0.3f, shapePaint)
        canvas.drawCircle(size * 0.2f, size * 0.9f, size * 0.4f, shapePaint)

        // PulseMusic Logo
        val logoSize = 256
        val rawLogo = ResourcesCompat.getDrawable(context.resources, R.drawable.app_logo, null)
        val logoBitmap = rawLogo?.toBitmap(logoSize, logoSize)
        if (logoBitmap != null) {
            val logoX = (size - logoSize) / 2f
            val logoY = size * 0.25f
            canvas.drawBitmap(logoBitmap, logoX, logoY, null)
        }

        // Text: Wrapped 2025
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 100f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PULSEMUSIC", size / 2f, size * 0.65f, textPaint)

        val yearPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF8B6914.toInt()
            textSize = 140f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(year.toString(), size / 2f, size * 0.8f, yearPaint)

        val subtitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.LTGRAY
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.1f
        }
        canvas.drawText("YOUR YEAR IN MUSIC", size / 2f, size * 0.88f, subtitlePaint)

        return bitmap
    }
}
