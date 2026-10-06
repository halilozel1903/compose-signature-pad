package io.github.halilozel1903.signaturepad.core

import kotlin.math.ceil

/** A piece of a smoothed stroke, with the drawn width at both ends. */
public sealed interface PathSegment {
    public val start: Point
    public val end: Point
    public val startWidth: Float
    public val endWidth: Float

    /** The position at [t], 0 (start) to 1 (end). */
    public fun pointAt(t: Float): Point

    /** An upper bound of the length (the control polygon), used to choose how finely to sample. */
    public val approximateLength: Float
}

/** A straight piece, used at the ends of a stroke. */
public data class LineSegment(
    override val start: Point,
    override val end: Point,
    override val startWidth: Float,
    override val endWidth: Float,
) : PathSegment {
    override fun pointAt(t: Float): Point = Point(lerp(start.x, end.x, t), lerp(start.y, end.y, t))
    override val approximateLength: Float get() = start.distanceTo(end)
}

/** A quadratic Bezier curve from [start] to [end], pulled towards [control]. */
public data class QuadSegment(
    override val start: Point,
    val control: Point,
    override val end: Point,
    override val startWidth: Float,
    override val endWidth: Float,
) : PathSegment {
    override fun pointAt(t: Float): Point {
        val u = 1f - t
        return Point(
            u * u * start.x + 2f * u * t * control.x + t * t * end.x,
            u * u * start.y + 2f * u * t * control.y + t * t * end.y,
        )
    }

    override val approximateLength: Float get() = start.distanceTo(control) + control.distanceTo(end)
}

/** A point on the smoothed curve with the drawn width there. */
public data class StrokeSample(val x: Float, val y: Float, val width: Float)

/**
 * Turns raw points into a smooth curve with quadratic Bezier midpoints: each input point becomes the control point
 * of a curve between the midpoints of its neighboring segments, so the curve is continuous in direction and passes
 * through the first and last points.
 *
 * For points p0..pn the path is `M p0 L mid(p0,p1) Q p1 mid(p1,p2) ... Q p(n-1) mid(p(n-1),pn) L pn`.
 */
public object StrokeSmoothing {

    /** The smoothed segments of [stroke] with widths from [config]. */
    public fun segments(stroke: Stroke, config: PenConfig = PenConfig.Default): List<PathSegment> =
        segments(stroke.points, StrokeWidths.compute(stroke, config))

    /**
     * The smoothed segments of [points] with [widths] at each point (same size). One point gives a zero length
     * line (a dot), two points a straight line.
     */
    public fun segments(points: List<StrokePoint>, widths: FloatArray): List<PathSegment> {
        require(widths.size == points.size) { "Need one width per point, got ${widths.size} for ${points.size}" }
        val n = points.size
        if (n == 0) return emptyList()
        val p = points.map { it.toPoint() }
        if (n == 1) return listOf(LineSegment(p[0], p[0], widths[0], widths[0]))
        if (n == 2) return listOf(LineSegment(p[0], p[1], widths[0], widths[1]))
        val result = ArrayList<PathSegment>(n)
        var start = p[0].midpoint(p[1])
        var startWidth = (widths[0] + widths[1]) / 2f
        result += LineSegment(p[0], start, widths[0], startWidth)
        for (i in 1 until n - 1) {
            val end = p[i].midpoint(p[i + 1])
            val endWidth = (widths[i] + widths[i + 1]) / 2f
            result += QuadSegment(start, p[i], end, startWidth, endWidth)
            start = end
            startWidth = endWidth
        }
        result += LineSegment(start, p[n - 1], startWidth, widths[n - 1])
        return result
    }

    /**
     * Samples [segments] about every [maxStep] pixels, interpolating the width. Consecutive samples can be drawn as
     * round capped lines to render a variable width stroke. A dot gives a single sample.
     */
    public fun sample(segments: List<PathSegment>, maxStep: Float = 2f): List<StrokeSample> {
        require(maxStep > 0f) { "maxStep must be positive" }
        if (segments.isEmpty()) return emptyList()
        val first = segments.first()
        val samples = ArrayList<StrokeSample>()
        samples += StrokeSample(first.start.x, first.start.y, first.startWidth)
        for (segment in segments) {
            val steps = ceil(segment.approximateLength / maxStep).toInt()
            if (steps == 0) continue
            for (step in 1..steps) {
                val t = step.toFloat() / steps
                val point = segment.pointAt(t)
                samples += StrokeSample(point.x, point.y, lerp(segment.startWidth, segment.endWidth, t))
            }
        }
        return samples
    }

    /** Shortcut for [sample] of [segments] of [stroke]. */
    public fun sample(stroke: Stroke, config: PenConfig = PenConfig.Default, maxStep: Float = 2f): List<StrokeSample> =
        sample(segments(stroke, config), maxStep)
}
