package io.github.halilozel1903.signaturepad.core

/** Finds strokes near a point, for a whole stroke eraser or for selection. */
public object StrokeHitTest {

    /**
     * True when the smoothed line of [stroke] passes within [radius] pixels of ([x], [y]), counting half the stroke
     * width. Eraser strokes never hit.
     */
    public fun hits(stroke: Stroke, x: Float, y: Float, radius: Float, config: PenConfig = PenConfig.Default): Boolean {
        if (stroke.kind != StrokeKind.Pen || stroke.isEmpty) return false
        val box = stroke.bounds(config) ?: return false
        if (!box.inflate(radius).contains(x, y)) return false
        val samples = StrokeSmoothing.sample(stroke, config, maxStep = 2f)
        if (samples.size == 1) {
            val s = samples[0]
            return distance(s.x, s.y, x, y) <= radius + s.width / 2f
        }
        for (i in 1 until samples.size) {
            val a = samples[i - 1]
            val b = samples[i]
            val reach = radius + maxOf(a.width, b.width) / 2f
            if (distanceToSegment(x, y, a.x, a.y, b.x, b.y) <= reach) return true
        }
        return false
    }

    /** The strokes of [strokes] that do not [hits] ([x], [y]) within [radius]. */
    public fun eraseAt(
        strokes: List<Stroke>,
        x: Float,
        y: Float,
        radius: Float,
        config: PenConfig = PenConfig.Default,
    ): List<Stroke> = strokes.filterNot { hits(it, x, y, radius, config) }

    /** The distance from ([px], [py]) to the segment from ([ax], [ay]) to ([bx], [by]). */
    public fun distanceToSegment(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = bx - ax
        val dy = by - ay
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared == 0f) return distance(px, py, ax, ay)
        val t = (((px - ax) * dx + (py - ay) * dy) / lengthSquared).coerceIn(0f, 1f)
        return distance(px, py, ax + t * dx, ay + t * dy)
    }
}
