package io.github.halilozel1903.signaturepad.core

/** An axis aligned rectangle in pixels. */
public data class BoundingBox(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    public val width: Float get() = right - left
    public val height: Float get() = bottom - top

    /** The union of this box and [other]. */
    public fun union(other: BoundingBox): BoundingBox = BoundingBox(
        left = minOf(left, other.left),
        top = minOf(top, other.top),
        right = maxOf(right, other.right),
        bottom = maxOf(bottom, other.bottom),
    )

    /** This box grown by [amount] on every side. */
    public fun inflate(amount: Float): BoundingBox =
        BoundingBox(left - amount, top - amount, right + amount, bottom + amount)

    /** True when ([x], [y]) is inside or on the edge. */
    public fun contains(x: Float, y: Float): Boolean = x in left..right && y in top..bottom

    public companion object {
        /** The bounds of [points], or null when there are none. */
        public fun of(points: List<StrokePoint>): BoundingBox? {
            if (points.isEmpty()) return null
            var left = Float.POSITIVE_INFINITY
            var top = Float.POSITIVE_INFINITY
            var right = Float.NEGATIVE_INFINITY
            var bottom = Float.NEGATIVE_INFINITY
            for (point in points) {
                left = minOf(left, point.x)
                top = minOf(top, point.y)
                right = maxOf(right, point.x)
                bottom = maxOf(bottom, point.y)
            }
            return BoundingBox(left, top, right, bottom)
        }
    }
}
