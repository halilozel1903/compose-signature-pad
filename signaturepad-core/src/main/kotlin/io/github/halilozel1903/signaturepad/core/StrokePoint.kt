package io.github.halilozel1903.signaturepad.core

/** The input device that produced a point. */
public enum class ToolType {
    /** A finger on a touch screen. */
    Finger,

    /** The tip of an active stylus (S Pen, USI pen, Pixel Tablet pen). */
    Stylus,

    /** The eraser end of a stylus. Pads erase with it, whatever tool is selected. */
    StylusEraser,

    /** A mouse or touchpad. */
    Mouse,

    /** Anything else. Treated like a finger. */
    Unknown,
    ;

    /** True for both ends of a stylus. */
    public val isStylus: Boolean get() = this == Stylus || this == StylusEraser
}

/**
 * One sampled input point.
 *
 * @property x horizontal position in pixels.
 * @property y vertical position in pixels.
 * @property pressure normalized pressure, 0 to 1. Fingers and mice usually report 1.
 * @property timeMillis when the point was sampled, in milliseconds (any monotonic clock).
 * @property toolType the device that produced the point.
 */
public data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val timeMillis: Long = 0L,
    val toolType: ToolType = ToolType.Finger,
) {
    /** The distance to [other] in pixels. */
    public fun distanceTo(other: StrokePoint): Float = distance(x, y, other.x, other.y)

    /** This point moved by [dx], [dy]. */
    public fun translate(dx: Float, dy: Float): StrokePoint = copy(x = x + dx, y = y + dy)
}

/** A plain 2D point, used for curve geometry. */
public data class Point(val x: Float, val y: Float) {
    /** The point halfway between this and [other]. */
    public fun midpoint(other: Point): Point = Point((x + other.x) / 2f, (y + other.y) / 2f)

    /** The distance to [other]. */
    public fun distanceTo(other: Point): Float = distance(x, y, other.x, other.y)
}

internal fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = (x2 - x1).toDouble()
    val dy = (y2 - y1).toDouble()
    return kotlin.math.sqrt(dx * dx + dy * dy).toFloat()
}

internal fun StrokePoint.toPoint(): Point = Point(x, y)
