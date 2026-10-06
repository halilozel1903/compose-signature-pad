package io.github.halilozel1903.signaturepad.core

/** What a stroke does to the ink below it. */
public enum class StrokeKind {
    /** Draws ink in the stroke's color. */
    Pen,

    /** Removes the ink below it (a pixel eraser). Its color is ignored. */
    Eraser,
}

/**
 * One continuous line, from pen down to pen up.
 *
 * @property points the sampled points, in drawing order.
 * @property color the ink color as ARGB, like `0xFF1A237E.toInt()`.
 * @property width the base width in pixels. The drawn width varies around it with speed and pressure, see [PenConfig].
 * @property kind [StrokeKind.Pen] or [StrokeKind.Eraser].
 */
public data class Stroke(
    val points: List<StrokePoint>,
    val color: Int = DEFAULT_COLOR,
    val width: Float = DEFAULT_WIDTH,
    val kind: StrokeKind = StrokeKind.Pen,
) {
    init {
        require(width > 0f) { "Stroke width must be positive, was $width" }
    }

    /** True when the stroke has no points. */
    public val isEmpty: Boolean get() = points.isEmpty()

    /** The tool of the first point, or [ToolType.Unknown] for an empty stroke. */
    public val toolType: ToolType get() = points.firstOrNull()?.toolType ?: ToolType.Unknown

    /** The bounds of the points, without the stroke width. Null for an empty stroke. */
    public fun pointBounds(): BoundingBox? = BoundingBox.of(points)

    /** The bounds including half of each point's drawn width. Null for an empty stroke. */
    public fun bounds(config: PenConfig = PenConfig.Default): BoundingBox? {
        if (points.isEmpty()) return null
        val widths = StrokeWidths.compute(this, config)
        var left = Float.POSITIVE_INFINITY
        var top = Float.POSITIVE_INFINITY
        var right = Float.NEGATIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        points.forEachIndexed { index, point ->
            val half = widths[index] / 2f
            left = minOf(left, point.x - half)
            top = minOf(top, point.y - half)
            right = maxOf(right, point.x + half)
            bottom = maxOf(bottom, point.y + half)
        }
        return BoundingBox(left, top, right, bottom)
    }

    /** This stroke moved by [dx], [dy]. */
    public fun translate(dx: Float, dy: Float): Stroke = copy(points = points.map { it.translate(dx, dy) })

    /** This stroke scaled by [factor] around the origin, width included. */
    public fun scale(factor: Float): Stroke {
        require(factor > 0f) { "Scale factor must be positive, was $factor" }
        return copy(points = points.map { it.copy(x = it.x * factor, y = it.y * factor) }, width = width * factor)
    }

    /** This stroke with fewer points, see [Simplification.simplify]. */
    public fun simplify(tolerance: Float): Stroke = copy(points = Simplification.simplify(points, tolerance))

    public companion object {
        /** Near black ink, `#1A1A2E`. */
        public const val DEFAULT_COLOR: Int = 0xFF1A1A2E.toInt()

        /** The default base width in pixels. */
        public const val DEFAULT_WIDTH: Float = 6f
    }
}
