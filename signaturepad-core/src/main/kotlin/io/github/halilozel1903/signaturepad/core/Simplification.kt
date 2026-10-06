package io.github.halilozel1903.signaturepad.core

import kotlin.math.abs
import kotlin.math.sqrt

/** Point reduction with the Ramer-Douglas-Peucker algorithm. */
public object Simplification {

    /**
     * Removes points that lie within [tolerance] pixels of the line through their neighbors, keeping the first and
     * last points. A tolerance of 0 keeps every point except exact duplicates on a straight line. Pressure, time and
     * tool of the kept points are unchanged.
     */
    public fun simplify(points: List<StrokePoint>, tolerance: Float): List<StrokePoint> {
        require(tolerance >= 0f) { "Tolerance must not be negative, was $tolerance" }
        if (points.size < 3) return points
        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.size - 1] = true
        // An explicit stack instead of recursion: long strokes would otherwise risk a deep call stack.
        val stack = ArrayDeque<IntArray>()
        stack.addLast(intArrayOf(0, points.size - 1))
        while (stack.isNotEmpty()) {
            val (first, last) = stack.removeLast()
            var maxDistance = -1f
            var index = -1
            for (i in first + 1 until last) {
                val d = perpendicularDistance(points[i], points[first], points[last])
                if (d > maxDistance) {
                    maxDistance = d
                    index = i
                }
            }
            if (index != -1 && maxDistance > tolerance) {
                keep[index] = true
                stack.addLast(intArrayOf(first, index))
                stack.addLast(intArrayOf(index, last))
            }
        }
        return points.filterIndexed { i, _ -> keep[i] }
    }

    /** The distance from [point] to the line through [lineStart] and [lineEnd] (to [lineStart] when they coincide). */
    public fun perpendicularDistance(point: StrokePoint, lineStart: StrokePoint, lineEnd: StrokePoint): Float {
        val dx = (lineEnd.x - lineStart.x).toDouble()
        val dy = (lineEnd.y - lineStart.y).toDouble()
        val length = sqrt(dx * dx + dy * dy)
        if (length == 0.0) return point.distanceTo(lineStart)
        val cross = dx * (lineStart.y - point.y) - dy * (lineStart.x - point.x)
        return (abs(cross) / length).toFloat()
    }
}
