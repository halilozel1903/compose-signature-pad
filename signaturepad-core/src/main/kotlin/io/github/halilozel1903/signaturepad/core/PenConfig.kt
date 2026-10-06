package io.github.halilozel1903.signaturepad.core

/**
 * How the drawn width follows the pen. Width is `base * lerp(minWidthFactor, maxWidthFactor, t)` where `t` mixes
 * slowness (slow strokes are thicker, like ink from a real pen) and stylus pressure.
 *
 * @property minWidthFactor the width factor for a fast, light stroke.
 * @property maxWidthFactor the width factor for a slow, heavy stroke.
 * @property velocityForMinWidth the speed in pixels per millisecond at and above which the stroke is thinnest.
 * @property velocityWeight how much speed changes the width, 0 (not at all) to 1.
 * @property pressureWeight how much pressure decides the width when the input has pressure, 0 to 1.
 * @property smoothing how much of the previous point's width is kept, 0 to below 1. Avoids sudden jumps.
 */
public data class PenConfig(
    val minWidthFactor: Float = 0.5f,
    val maxWidthFactor: Float = 1.5f,
    val velocityForMinWidth: Float = 4f,
    val velocityWeight: Float = 0.7f,
    val pressureWeight: Float = 0.75f,
    val smoothing: Float = 0.6f,
) {
    init {
        require(minWidthFactor > 0f && maxWidthFactor >= minWidthFactor) {
            "Width factors must satisfy 0 < min <= max, were $minWidthFactor and $maxWidthFactor"
        }
        require(velocityForMinWidth > 0f) { "velocityForMinWidth must be positive" }
        require(velocityWeight in 0f..1f) { "velocityWeight must be in 0..1" }
        require(pressureWeight in 0f..1f) { "pressureWeight must be in 0..1" }
        require(smoothing >= 0f && smoothing < 1f) { "smoothing must be in 0 until 1" }
    }

    public companion object {
        /** Speed and pressure aware ink, like a fountain pen. */
        public val Default: PenConfig = PenConfig()

        /** Every point drawn at exactly the base width, like a marker. */
        public val Uniform: PenConfig = PenConfig(
            minWidthFactor = 1f,
            maxWidthFactor = 1f,
            velocityWeight = 0f,
            pressureWeight = 0f,
            smoothing = 0f,
        )

        /** Strong contrast between light and heavy strokes, like a flexible fountain pen nib. Great for signatures. */
        public val Fountain: PenConfig = PenConfig(
            minWidthFactor = 0.3f,
            maxWidthFactor = 1.9f,
            velocityWeight = 0.6f,
            pressureWeight = 0.85f,
            smoothing = 0.5f,
        )

        /** Pressure only, ignoring speed: for a stylus that should feel like a pencil. */
        public val PressureOnly: PenConfig = PenConfig(
            minWidthFactor = 0.35f,
            maxWidthFactor = 1.6f,
            velocityWeight = 0f,
            pressureWeight = 1f,
            smoothing = 0.4f,
        )
    }
}

/** Per point widths of a stroke. */
public object StrokeWidths {

    /**
     * The drawn width at each point of [stroke], in pixels, with the same size as `stroke.points`.
     *
     * Pressure is used when the stroke comes from a stylus or its pressure varies; fingers that report a constant
     * pressure are drawn from speed alone. Eraser strokes always have their base width.
     */
    public fun compute(stroke: Stroke, config: PenConfig = PenConfig.Default): FloatArray {
        if (stroke.kind == StrokeKind.Eraser) return FloatArray(stroke.points.size) { stroke.width }
        val points = stroke.points
        val widths = FloatArray(points.size)
        if (points.isEmpty()) return widths
        val usePressure = hasPressure(points)
        var previous = 0f
        for (i in points.indices) {
            val velocity = velocityAt(points, i)
            val slowness = 1f - (velocity / config.velocityForMinWidth).coerceIn(0f, 1f)
            val fromVelocity = lerp(0.5f, slowness, config.velocityWeight)
            val t = if (usePressure) {
                lerp(fromVelocity, points[i].pressure.coerceIn(0f, 1f), config.pressureWeight)
            } else {
                fromVelocity
            }
            val raw = stroke.width * lerp(config.minWidthFactor, config.maxWidthFactor, t)
            val width = if (i == 0) raw else previous * config.smoothing + raw * (1f - config.smoothing)
            widths[i] = width
            previous = width
        }
        return widths
    }

    /** True when [points] carry real pressure: they come from a stylus or their pressure varies. */
    public fun hasPressure(points: List<StrokePoint>): Boolean {
        if (points.any { it.toolType.isStylus }) return true
        if (points.isEmpty()) return false
        var min = points[0].pressure
        var max = points[0].pressure
        for (point in points) {
            min = minOf(min, point.pressure)
            max = maxOf(max, point.pressure)
        }
        return max - min > PRESSURE_VARIATION
    }

    /** Speed in pixels per millisecond arriving at point [index]; the first point uses the speed to the second. */
    public fun velocityAt(points: List<StrokePoint>, index: Int): Float {
        if (points.size < 2) return 0f
        val (from, to) = if (index == 0) points[0] to points[1] else points[index - 1] to points[index]
        val elapsed = (to.timeMillis - from.timeMillis).coerceAtLeast(1L)
        return from.distanceTo(to) / elapsed
    }

    private const val PRESSURE_VARIATION = 0.05f
}

internal fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
