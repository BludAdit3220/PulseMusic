/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.widget

import android.graphics.*
import kotlin.math.*

/**
 * Replicates the Mini Player's artwork implementation for the Turntable widget.
 * Features a circular artwork and the Material 3 "blobby flower" wavy progress indicator.
 */
internal object TurntableCanvasRenderer {

    private const val PETAL_COUNT = 10
    private const val TRACK_ALPHA = 45 // ~0.18f

    fun render(
        artBitmap: Bitmap?,
        progress: Float,
        isPlaying: Boolean,
        animationPhase: Float,
        color: Int,
        sizePx: Int,
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx / 2f
        
        // Match mini player sizing: 47dp container, 37dp art
        // artRadius = radius * (37/47)
        val artRadius = radius * 0.787f
        
        // 1. Draw Wavy Progress (Outer)
        drawWavyProgress(canvas, cx, cy, radius, artRadius, progress, isPlaying, animationPhase, color)
        
        // 2. Draw Circular Artwork (Inner)
        if (artBitmap != null) {
            drawCircularArt(canvas, artBitmap, cx, cy, artRadius, color)
        } else {
            drawPlaceholder(canvas, cx, cy, artRadius)
        }
        
        return bmp
    }

    private fun drawWavyProgress(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        maxRadius: Float,
        artRadius: Float,
        progress: Float,
        isPlaying: Boolean,
        phase: Float,
        color: Int,
    ) {
        val strokeWidth = maxRadius * 0.08f
        val amplitude = maxRadius * 0.04f
        val baseR = artRadius + (strokeWidth / 2f) + (amplitude / 2f)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            this.color = color
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val trackPaint = Paint(paint).apply {
            this.alpha = TRACK_ALPHA
        }

        // Draw track
        canvas.drawPath(buildWavyPath(cx, cy, baseR, amplitude, 1.0f, phase, 1.0f, isPlaying), trackPaint)
        
        // Draw progress
        if (progress > 0f) {
            canvas.drawPath(buildWavyPath(cx, cy, baseR, amplitude, progress, phase, 1.0f, isPlaying), paint)
        } else if (isPlaying) {
            // Indeterminate-like look if playing but no progress
            canvas.drawPath(buildWavyPath(cx, cy, baseR, amplitude, 0.25f, phase, 1.0f, isPlaying), paint)
        }
    }

    private fun buildWavyPath(
        cx: Float,
        cy: Float,
        baseR: Float,
        amplitude: Float,
        progress: Float,
        phase: Float,
        intensity: Float,
        isPlaying: Boolean
    ): Path {
        val path = Path()
        val segments = 360
        val endSegment = (segments * progress).toInt()
        
        for (i in 0..endSegment) {
            val angleRad = (i.toFloat() / segments) * 2 * PI.toFloat() - (PI.toFloat() / 2f)
            
            // The "flower" math: r = base + amp * cos(n * theta + phase)^2
            // cos^2 creates the blobby petals
            val modulationAngle = angleRad * PETAL_COUNT + phase
            val modulation = cos(modulationAngle.toDouble()).pow(2.0).toFloat()
            
            val currentR = baseR + (modulation * amplitude * (if (isPlaying) intensity else 0.1f))
            
            val x = cx + cos(angleRad.toDouble()).toFloat() * currentR
            val y = cy + sin(angleRad.toDouble()).toFloat() * currentR
            
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        return path
    }

    private fun drawCircularArt(
        canvas: Canvas,
        artBitmap: Bitmap,
        cx: Float,
        cy: Float,
        radius: Float,
        color: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Create shader for circular crop
        val scaled = Bitmap.createScaledBitmap(artBitmap, (radius * 2).toInt(), (radius * 2).toInt(), true)
        val shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val matrix = Matrix()
        matrix.setTranslate(cx - radius, cy - radius)
        shader.setLocalMatrix(matrix)
        paint.shader = shader
        
        canvas.drawCircle(cx, cy, radius, paint)
        
        // Border matching mini player
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            this.color = color
            alpha = 50
        }
        canvas.drawCircle(cx, cy, radius, borderPaint)
        
        scaled.recycle()
    }

    private fun drawPlaceholder(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 40, 36, 48)
        }
        canvas.drawCircle(cx, cy, radius, paint)
        
        // Music note or icon could be added here if needed, matching mini player's fallback
    }
}
