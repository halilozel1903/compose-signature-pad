package io.github.halilozel1903.signaturepad

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import io.github.halilozel1903.signaturepad.core.PenConfig
import io.github.halilozel1903.signaturepad.core.Stroke
import io.github.halilozel1903.signaturepad.core.StrokeKind
import io.github.halilozel1903.signaturepad.core.StrokeSample
import io.github.halilozel1903.signaturepad.core.StrokeSmoothing
import java.util.IdentityHashMap

/** Distance between samples along a stroke, in pixels. */
private const val SAMPLE_STEP = 1.5f

/**
 * Draws [stroke] as round capped lines between its smoothed samples, so the width can change along the stroke.
 * Eraser strokes clear the pixels below them; translucent ink is drawn through a layer so overlapping caps do not
 * show as darker dots.
 */
internal fun DrawScope.drawInk(stroke: Stroke, samples: List<StrokeSample>) {
    if (samples.isEmpty()) return
    val eraser = stroke.kind == StrokeKind.Eraser
    val color = if (eraser) Color.Black else Color(stroke.color)
    val blendMode = if (eraser) BlendMode.Clear else BlendMode.SrcOver
    val translucent = !eraser && color.alpha < 1f
    if (translucent) {
        val layerPaint = Paint()
        layerPaint.alpha = color.alpha
        drawIntoCanvas { canvas -> canvas.saveLayer(Rect(Offset.Zero, size), layerPaint) }
    }
    val opaque = if (translucent) color.copy(alpha = 1f) else color
    if (samples.size == 1) {
        val dot = samples[0]
        drawCircle(opaque, radius = dot.width / 2f, center = Offset(dot.x, dot.y), blendMode = blendMode)
    } else {
        for (i in 1 until samples.size) {
            val a = samples[i - 1]
            val b = samples[i]
            drawLine(
                color = opaque,
                start = Offset(a.x, a.y),
                end = Offset(b.x, b.y),
                strokeWidth = (a.width + b.width) / 2f,
                cap = StrokeCap.Round,
                blendMode = blendMode,
            )
        }
    }
    if (translucent) drawIntoCanvas { it.restore() }
}

/** Draws every stroke of [strokes] in order. */
internal fun DrawScope.drawInk(strokes: List<Stroke>, config: PenConfig, cache: InkCache? = null) {
    cache?.retainOnly(strokes, config)
    for (stroke in strokes) {
        val samples = cache?.samples(stroke, config) ?: StrokeSmoothing.sample(stroke, config, SAMPLE_STEP)
        drawInk(stroke, samples)
    }
}

/** Samples of a stroke being drawn. */
internal fun sampleActive(stroke: Stroke, config: PenConfig): List<StrokeSample> =
    StrokeSmoothing.sample(stroke, config, SAMPLE_STEP)

/**
 * Smoothed samples of finished strokes, keyed by identity: strokes are immutable and the history keeps the same
 * instances, so a stroke is smoothed once instead of every frame.
 */
internal class InkCache {
    private val samples = IdentityHashMap<Stroke, List<StrokeSample>>()
    private var config: PenConfig? = null

    fun samples(stroke: Stroke, config: PenConfig): List<StrokeSample> =
        samples.getOrPut(stroke) { StrokeSmoothing.sample(stroke, config, SAMPLE_STEP) }

    fun retainOnly(strokes: List<Stroke>, config: PenConfig) {
        if (config != this.config) {
            samples.clear()
            this.config = config
        }
        if (samples.size > strokes.size + 32) {
            val keep = IdentityHashMap<Stroke, Boolean>()
            strokes.forEach { keep[it] = true }
            samples.keys.retainAll { keep.containsKey(it) }
        }
    }
}
