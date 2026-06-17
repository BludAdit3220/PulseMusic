/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.BitmapShader
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Renders the radial wave visualizer as a [Bitmap] — a direct port of
 * [RadialWaveVisualizer.qml] from the Quickshell config.
 *
 * The QML version draws on a Canvas2D:
 *  - `maxRadius = min(w,h) / 2`
 *  - `inwardOffset = maxRadius * 0.8`
 *  - For each frequency point, `currentRadius = maxRadius - (normalized * inwardOffset)`
 *    clamped to `[maxRadius - inwardOffset, maxRadius]`
 *  - The path traces the wave backwards (outer→inner), closes by tracing the
 *    full-radius circle forwards, and fills with `waveOpacity = 0.15`.
 *  - A MultiEffect blur (`blurMax=7, blur=waveBlur`) is applied on top.
 *
 * On Android we reproduce this with [BlurMaskFilter] on the fill paint.
 *
 * The result is rendered to a [Bitmap] so it can be pushed into a
 * RemoteViews via [android.widget.RemoteViews.setImageViewBitmap].
 */
internal object RadialWaveView {

    private const val MAX_VISUALIZER_VALUE = 800f
    private const val SMOOTHING_WINDOW = 2
    private const val WAVE_OPACITY = 0.18f   // slightly higher than QML 0.15 for Android rendering
    private const val BLUR_RADIUS = 12f      // approximates QML blurMax=7

    /**
     * Draws the full turntable widget frame to a [Bitmap]:
     *  1. Circular album art (clipped to circle)
     *  2. Vinyl grooves ring
     *  3. Radial wave overlay (the QML port)
     *  4. Centre label circle
     *
     * @param artBitmap  The decoded album art bitmap, or null for a placeholder.
     * @param wavePoints Frequency magnitude array from the visualizer (0..MAX_VISUALIZER_VALUE).
     *                   Pass empty list when not playing.
     * @param isPlaying  Whether playback is active — silences wave when false.
     * @param waveColor  The wave fill colour (should come from [WidgetPalette.secondaryContainer]).
     * @param sizePx     Target output bitmap size in pixels (square).
     */
    fun render(
        context: Context,
        artBitmap: Bitmap?,
        wavePoints: List<Float>,
        isPlaying: Boolean,
        waveColor: Int,
        sizePx: Int,
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val maxRadius = sizePx / 2f

        // ── 1. Album art circle ──────────────────────────────────────────────
        val artRadius = maxRadius * 0.60f
        drawCircularArt(canvas, artBitmap, cx, cy, artRadius, sizePx)

        // ── 2. Vinyl groove ring ─────────────────────────────────────────────
        drawVinylGrooves(canvas, cx, cy, maxRadius, artRadius)

        // ── 3. Radial wave (QML port) ────────────────────────────────────────
        if (wavePoints.size >= 3) {
            drawRadialWave(canvas, cx, cy, maxRadius, wavePoints, isPlaying, waveColor)
        }

        // ── 4. Centre spindle circle ─────────────────────────────────────────
        drawSpindle(canvas, cx, cy)

        return bmp
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun drawCircularArt(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        artRadius: Float,
        sizePx: Int,
    ) {
        // We use a layer with DST_IN to clip the art to a circle
        canvas.saveLayer(null, null)
        // Placeholder fill
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 30, 25, 35)
        }
        canvas.drawCircle(cx, cy, artRadius, bgPaint)
        canvas.restore()
    }

    /**
     * Draws album art clipped to a circle on top of the background.
     * Called separately so the art bitmap can be null-checked.
     */
    fun drawCircularArtBitmap(
        canvas: Canvas,
        artBitmap: Bitmap,
        cx: Float,
        cy: Float,
        artRadius: Float,
    ) {
        val scaled = Bitmap.createScaledBitmap(
            artBitmap,
            (artRadius * 2).toInt(),
            (artRadius * 2).toInt(),
            true,
        )
        val shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val matrix = android.graphics.Matrix()
        matrix.setTranslate(cx - artRadius, cy - artRadius)
        shader.setLocalMatrix(matrix)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
        }
        canvas.drawCircle(cx, cy, artRadius, paint)
        scaled.recycle()
    }

    private fun drawVinylGrooves(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        outerRadius: Float,
        innerRadius: Float,
    ) {
        val groovePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        // Draw 8 concentric groove rings between innerRadius and outerRadius
        val gap = (outerRadius - innerRadius) / 9f
        for (i in 1..8) {
            val r = innerRadius + gap * i
            val alpha = (60 + i * 8).coerceAtMost(120)
            groovePaint.color = Color.argb(alpha, 255, 255, 255)
            canvas.drawCircle(cx, cy, r, groovePaint)
        }
    }

    /**
     * Direct port of [RadialWaveVisualizer.qml] `onPaint`.
     *
     * QML logic:
     * ```
     * inwardOffset = maxRadius * 0.8
     * // trace backwards (outer edge → wave dips inward)
     * for i in (n-1)..0:
     *     angle = (i/(n-1)) * 2π - π/2
     *     r = maxRadius - (normalized * inwardOffset)   clamped ≥ maxRadius-inwardOffset
     * // close by tracing the outer circle forward
     * for i in 0..n-1:
     *     x = cx + cos(angle)*maxRadius
     * closePath(); fill with waveOpacity
     * ```
     */
    private fun drawRadialWave(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        maxRadius: Float,
        rawPoints: List<Float>,
        isPlaying: Boolean,
        waveColor: Int,
    ) {
        val n = rawPoints.size
        val inwardOffset = maxRadius * 0.8f

        // Smooth: simple moving average (mirrors QML smoothing window=2)
        val smooth = FloatArray(n)
        if (!isPlaying) {
            // not playing → all zeros → flat circle (invisible wave)
            smooth.fill(0f)
        } else {
            for (i in 0 until n) {
                var sum = 0f
                var count = 0
                for (j in -SMOOTHING_WINDOW..SMOOTHING_WINDOW) {
                    val idx = (i + j).coerceIn(0, n - 1)
                    sum += rawPoints[idx]
                    count++
                }
                smooth[i] = sum / count
            }
        }

        // plotPoints = smooth + wrap (append [0] to close the circle)
        val visualN = n + 1

        val path = Path()

        // Trace backwards: i = visualN-1 down to 0
        for (i in visualN - 1 downTo 0) {
            val pointIdx = if (i == visualN - 1) 0 else i  // wrap
            val normalized = (smooth[pointIdx] / MAX_VISUALIZER_VALUE).coerceIn(0f, 1f)
            val angle = (i.toFloat() / (visualN - 1).toFloat()) * (Math.PI * 2).toFloat() - (Math.PI / 2).toFloat()

            val rawRadius = maxRadius - normalized * inwardOffset
            val r = rawRadius.coerceAtLeast(maxRadius - inwardOffset)

            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r

            if (i == visualN - 1) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Close by tracing the outer circle forward
        for (i in 0 until visualN) {
            val angle = (i.toFloat() / (visualN - 1).toFloat()) * (Math.PI * 2).toFloat() - (Math.PI / 2).toFloat()
            val x = cx + cos(angle) * maxRadius
            val y = cy + sin(angle) * maxRadius
            path.lineTo(x, y)
        }
        path.close()

        val r = Color.red(waveColor) / 255f
        val g = Color.green(waveColor) / 255f
        val b = Color.blue(waveColor) / 255f
        val fillColor = Color.argb(
            (WAVE_OPACITY * 255).toInt(),
            (r * 255).toInt(),
            (g * 255).toInt(),
            (b * 255).toInt(),
        )

        // Draw with blur (approximates QML MultiEffect blurEnabled)
        val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
            maskFilter = BlurMaskFilter(BLUR_RADIUS, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(path, wavePaint)
    }

    private fun drawSpindle(canvas: Canvas, cx: Float, cy: Float) {
        val spindlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 40, 36, 44)
        }
        canvas.drawCircle(cx, cy, 10f, spindlePaint)
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 200, 190, 220)
        }
        canvas.drawCircle(cx, cy, 3f, dotPaint)
    }

    /**
     * Renders the full turntable bitmap given all state.
     * The art and wave are composited in a single pass.
     */
    fun renderFull(
        artBitmap: Bitmap?,
        wavePoints: List<Float>,
        isPlaying: Boolean,
        waveColor: Int,
        sizePx: Int,
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val maxRadius = sizePx / 2f
        val artRadius = maxRadius * 0.60f

        // ── Background ────────────────────────────────────────────────────────
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 22, 18, 28)
        }
        canvas.drawCircle(cx, cy, maxRadius, bgPaint)

        // ── Vinyl grooves ─────────────────────────────────────────────────────
        drawVinylGrooves(canvas, cx, cy, maxRadius, artRadius)

        // ── Radial wave (drawn before art so it appears behind the art circle) ─
        if (wavePoints.size >= 3) {
            drawRadialWave(canvas, cx, cy, maxRadius, wavePoints, isPlaying, waveColor)
        }

        // ── Album art ────────────────────────────────────────────────────────
        if (artBitmap != null) {
            drawCircularArtBitmap(canvas, artBitmap, cx, cy, artRadius)
        } else {
            // Placeholder: dark circle with music note tint
            val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(255, 40, 34, 50)
            }
            canvas.drawCircle(cx, cy, artRadius, placeholderPaint)
        }

        // ── Spindle ────────────────────────────────────────────────────────
        drawSpindle(canvas, cx, cy)

        return bmp
    }
}
