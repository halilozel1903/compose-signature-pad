package io.github.halilozel1903.signaturepad.core

/**
 * A drawing: its strokes and the size of the canvas they were drawn on.
 *
 * Coordinates are pixels with the origin at the top left of the canvas. Eraser strokes remove the ink of the
 * strokes before them.
 */
public data class Signature(
    val strokes: List<Stroke>,
    val width: Float,
    val height: Float,
) {
    init {
        require(width >= 0f && height >= 0f) { "Canvas size must not be negative, was ${width}x$height" }
    }

    /** True when there is no ink: no pen stroke with points. */
    public val isEmpty: Boolean get() = strokes.none { it.kind == StrokeKind.Pen && it.points.isNotEmpty() }

    /** The number of points in all strokes. */
    public val pointCount: Int get() = strokes.sumOf { it.points.size }

    /**
     * The bounds of the ink, stroke widths included, or null when [isEmpty]. Eraser strokes are ignored: they
     * can only remove ink.
     */
    public fun bounds(config: PenConfig = PenConfig.Default): BoundingBox? = strokes
        .filter { it.kind == StrokeKind.Pen }
        .mapNotNull { it.bounds(config) }
        .reduceOrNull { acc, box -> acc.union(box) }

    /** Every stroke moved by [dx], [dy]. The canvas size stays the same. */
    public fun translate(dx: Float, dy: Float): Signature = copy(strokes = strokes.map { it.translate(dx, dy) })

    /** Every stroke and the canvas scaled by [factor]. */
    public fun scale(factor: Float): Signature =
        Signature(strokes.map { it.scale(factor) }, width * factor, height * factor)

    /**
     * Crops the canvas to the ink plus [padding] on every side and moves the strokes to match. An empty
     * signature is returned unchanged.
     */
    public fun trim(padding: Float = 0f, config: PenConfig = PenConfig.Default): Signature {
        require(padding >= 0f) { "Padding must not be negative, was $padding" }
        val box = bounds(config) ?: return this
        return Signature(
            strokes = strokes.map { it.translate(padding - box.left, padding - box.top) },
            width = box.width + 2 * padding,
            height = box.height + 2 * padding,
        )
    }

    /**
     * Scales the ink uniformly to fit a [targetWidth] x [targetHeight] canvas with [padding] on every side,
     * centered, and returns it on that canvas. Use it to show a signature recorded on one screen on another.
     * Ink is never scaled up by more than [maxScale].
     */
    public fun fitInto(
        targetWidth: Float,
        targetHeight: Float,
        padding: Float = 0f,
        maxScale: Float = Float.POSITIVE_INFINITY,
        config: PenConfig = PenConfig.Default,
    ): Signature {
        require(targetWidth > 0f && targetHeight > 0f) { "Target size must be positive" }
        val box = bounds(config) ?: return Signature(strokes, targetWidth, targetHeight)
        val availableWidth = (targetWidth - 2 * padding).coerceAtLeast(1f)
        val availableHeight = (targetHeight - 2 * padding).coerceAtLeast(1f)
        val factor = minOf(
            availableWidth / box.width.coerceAtLeast(1e-3f),
            availableHeight / box.height.coerceAtLeast(1e-3f),
            maxScale,
        )
        val scaledLeft = box.left * factor
        val scaledTop = box.top * factor
        val dx = (targetWidth - box.width * factor) / 2f - scaledLeft
        val dy = (targetHeight - box.height * factor) / 2f - scaledTop
        return Signature(strokes.map { it.scale(factor).translate(dx, dy) }, targetWidth, targetHeight)
    }

    /** Every stroke simplified with [Simplification.simplify]. */
    public fun simplify(tolerance: Float): Signature = copy(strokes = strokes.map { it.simplify(tolerance) })

    /** The compact text form, see [SignatureCodec]. */
    public fun encode(): String = SignatureCodec.encode(this)

    /** An SVG document, see [SvgExporter]. */
    public fun toSvg(options: SvgOptions = SvgOptions()): String = SvgExporter.export(this, options)

    public companion object {
        /** An empty signature on an empty canvas. */
        public val Empty: Signature = Signature(emptyList(), 0f, 0f)

        /** Reads the text form written by [encode]. */
        public fun decode(text: String): Signature = SignatureCodec.decode(text)
    }
}
