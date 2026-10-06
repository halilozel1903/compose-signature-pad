package io.github.halilozel1903.signaturepad.sample

import io.github.halilozel1903.signaturepad.core.Signature
import io.github.halilozel1903.signaturepad.core.Stroke
import io.github.halilozel1903.signaturepad.core.StrokePoint
import io.github.halilozel1903.signaturepad.core.ToolType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Pre-recorded, deterministic ink for the screenshot scenes: there is no stylus on CI, and adb cannot draw with
 * pressure. Hand-tuned control points are densified with a Catmull-Rom spline, then given the pressure and timing a
 * stylus would report (light at the start and end of a stroke, heavier on downstrokes, slower in tight turns).
 */
object SampleStrokes {
    private const val NAVY = 0xFF1B2A6B.toInt()
    private const val CHARCOAL = 0xFF1E1E2E.toInt()
    private const val RED = 0xFFD32F2F.toInt()
    private const val BLUE = 0xFF1E4FD8.toInt()
    private const val ORANGE = 0xFFEF6C00.toInt()
    private const val GRAY = 0xFF8C93A3.toInt()

    /** "Emma Lindgren" in a quick cursive hand, with an underline flourish, on a 1000 x 400 canvas. */
    fun signature(): Signature {
        val clock = Clock()
        val strokes = listOf(
            // Capital E: top bowl, small inner loop, bottom bowl, running into the m.
            ink(
                listOf(
                    128f to 138f, 112f to 116f, 84f to 112f, 64f to 130f, 70f to 158f, 98f to 176f, 118f to 182f,
                    104f to 188f, 70f to 196f, 46f to 224f, 50f to 262f, 84f to 282f, 128f to 278f, 160f to 258f,
                    // m m a
                    176f to 222f, 184f to 212f, 188f to 232f, 186f to 262f, 194f to 236f, 208f to 214f, 216f to 226f,
                    214f to 262f, 222f to 236f, 238f to 214f, 246f to 226f, 244f to 262f, 254f to 234f, 270f to 214f,
                    278f to 226f, 276f to 262f, 288f to 232f, 312f to 226f, 296f to 226f, 280f to 244f, 286f to 264f,
                    306f to 256f, 318f to 232f, 318f to 258f, 330f to 266f, 350f to 252f,
                ),
                NAVY, 6f, clock,
            ),
            // Capital L with its top loop and bottom curl, running into "indgren".
            ink(
                listOf(
                    392f to 262f, 420f to 236f, 452f to 186f, 476f to 128f, 484f to 92f, 470f to 80f, 452f to 96f,
                    444f to 140f, 436f to 200f, 424f to 252f, 404f to 282f, 384f to 284f, 382f to 268f, 404f to 258f,
                    444f to 262f, 472f to 256f,
                    // i n
                    488f to 232f, 494f to 224f, 492f to 262f, 500f to 238f, 514f to 222f, 522f to 234f, 518f to 262f,
                    530f to 238f, 548f to 226f, 552f to 240f, 548f to 262f,
                    // d
                    566f to 238f, 586f to 226f, 574f to 228f, 560f to 246f, 566f to 264f, 584f to 254f, 600f to 214f,
                    616f to 140f, 612f to 128f, 602f to 170f, 600f to 250f, 612f to 264f, 628f to 252f,
                    // g with its descender loop
                    644f to 230f, 660f to 226f, 648f to 226f, 636f to 244f, 642f to 262f, 658f to 252f, 666f to 232f,
                    664f to 290f, 652f to 334f, 626f to 348f, 614f to 326f, 640f to 292f, 676f to 262f,
                    // r e n
                    686f to 240f, 698f to 228f, 700f to 244f, 712f to 232f, 726f to 232f, 742f to 248f, 764f to 236f,
                    758f to 222f, 742f to 230f, 744f to 258f, 764f to 262f, 782f to 248f, 796f to 234f, 800f to 262f,
                    810f to 240f, 826f to 226f, 836f to 242f, 834f to 262f, 852f to 258f, 884f to 236f,
                ),
                NAVY, 6f, clock,
            ),
            // The dot over the i, a quick tap.
            ink(listOf(492f to 184f, 498f to 179f), NAVY, 6f, clock, samplesPerSegment = 2),
            // Underline flourish, fast and light.
            ink(
                listOf(360f to 330f, 420f to 316f, 520f to 306f, 640f to 302f, 760f to 300f, 860f to 296f, 920f to 288f),
                NAVY, 5f, clock, speed = 2.2f, samplesPerSegment = 8,
            ),
        )
        return Signature(strokes, 1000f, 400f)
    }

    /** A damage report sketch: a parcel with a crushed corner circled in red, an arrow and "this side up" marks. */
    fun sketch(): Signature {
        val clock = Clock()
        val strokes = buildList<Stroke> {
            // The box: front, top and right faces.
            add(line(listOf(300f to 262f, 296f to 500f, 600f to 504f, 604f to 258f, 300f to 262f), CHARCOAL, 6f, clock, seed = 1))
            add(line(listOf(300f to 262f, 404f to 176f, 706f to 172f, 604f to 258f), CHARCOAL, 6f, clock, seed = 2))
            add(line(listOf(706f to 172f, 704f to 420f, 600f to 504f), CHARCOAL, 6f, clock, seed = 3))
            // Tape across the top.
            add(line(listOf(446f to 260f, 552f to 174f), CHARCOAL, 4f, clock, seed = 4))
            add(line(listOf(476f to 260f, 580f to 174f), CHARCOAL, 4f, clock, seed = 5))
            // Light shading on the right face.
            for (i in 0 until 6) {
                val x = 618f + i * 14f
                add(line(listOf(x to 300f + i * 6f, x + 10f to 470f - i * 18f), GRAY, 2.5f, clock, seed = 20 + i, amplitude = 0.6f))
            }
            // The crushed corner.
            add(line(listOf(640f to 214f, 656f to 232f, 668f to 212f, 684f to 236f, 694f to 214f), CHARCOAL, 4f, clock, seed = 6, amplitude = 0.4f))
            // Red circle around it, overshooting like a quick hand-drawn loop.
            add(ellipse(668f, 222f, 80f, 60f, RED, 5f, clock))
            // Blue arrow pointing at the damage.
            add(line(listOf(880f to 92f, 830f to 120f, 764f to 172f), BLUE, 5f, clock, seed = 7))
            add(line(listOf(792f to 170f, 764f to 172f, 772f to 146f), BLUE, 5f, clock, seed = 8, amplitude = 0.3f))
            // "This side up" arrows on the front.
            for (x in listOf(360f, 420f)) {
                add(line(listOf(x to 440f, x to 350f), ORANGE, 5f, clock, seed = x.toInt(), amplitude = 0.5f))
                add(line(listOf(x - 18f to 372f, x to 348f, x + 18f to 372f), ORANGE, 5f, clock, seed = x.toInt() + 1, amplitude = 0.3f))
            }
            add(line(listOf(340f to 462f, 440f to 462f), ORANGE, 5f, clock, seed = 9, amplitude = 0.5f))
            // Ground line.
            add(line(listOf(250f to 520f, 640f to 524f, 760f to 470f), CHARCOAL, 3f, clock, seed = 10, amplitude = 0.8f))
        }
        return Signature(strokes, 1000f, 600f)
    }

    /** A running clock so strokes follow each other in time like real input. */
    private class Clock {
        var millis = 1_000L
    }

    /** A cursive stroke through [controls], smoothed with Catmull-Rom. */
    private fun ink(
        controls: List<Pair<Float, Float>>,
        color: Int,
        width: Float,
        clock: Clock,
        speed: Float = 0.9f,
        samplesPerSegment: Int = 6,
    ): Stroke = toStroke(catmullRom(controls, samplesPerSegment), color, width, clock, speed)

    /** A hand-drawn straight stroke: [corners] joined by lines with a slight, smooth wobble. */
    private fun line(
        corners: List<Pair<Float, Float>>,
        color: Int,
        width: Float,
        clock: Clock,
        seed: Int,
        amplitude: Float = 1.4f,
    ): Stroke {
        val points = ArrayList<Pair<Float, Float>>()
        for (i in 0 until corners.size - 1) {
            val (ax, ay) = corners[i]
            val (bx, by) = corners[i + 1]
            val length = hypot(bx - ax, by - ay)
            val steps = max(1, (length / 10f).toInt())
            val nx = -(by - ay) / length
            val ny = (bx - ax) / length
            for (k in 0 until steps) {
                val t = k.toFloat() / steps
                val wobble = amplitude * sin((points.size + seed * 7) * 0.3f) * sin(PI.toFloat() * t)
                points += (ax + (bx - ax) * t + nx * wobble) to (ay + (by - ay) * t + ny * wobble)
            }
        }
        points += corners.last()
        return toStroke(points, color, width, clock, speed = 1.1f)
    }

    private fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, color: Int, width: Float, clock: Clock): Stroke {
        val count = 56
        val start = -2.4f
        val sweep = 7.0f
        val points = (0..count).map { i ->
            val t = i.toFloat() / count
            val angle = start + sweep * t
            val grow = 1f + 0.05f * t
            (cx + rx * cos(angle) * grow) to (cy + ry * sin(angle) * grow)
        }
        return toStroke(points, color, width, clock, speed = 1.4f)
    }

    private fun catmullRom(controls: List<Pair<Float, Float>>, perSegment: Int): List<Pair<Float, Float>> {
        if (controls.size < 3) return controls
        val p = listOf(controls.first()) + controls + listOf(controls.last())
        val out = ArrayList<Pair<Float, Float>>()
        for (i in 1 until p.size - 2) {
            val (x0, y0) = p[i - 1]
            val (x1, y1) = p[i]
            val (x2, y2) = p[i + 1]
            val (x3, y3) = p[i + 2]
            for (k in 0 until perSegment) {
                val t = k.toFloat() / perSegment
                val t2 = t * t
                val t3 = t2 * t
                fun spline(a: Float, b: Float, c: Float, d: Float) =
                    0.5f * (2 * b + (-a + c) * t + (2 * a - 5 * b + 4 * c - d) * t2 + (-a + 3 * b - 3 * c + d) * t3)
                out += spline(x0, x1, x2, x3) to spline(y0, y1, y2, y3)
            }
        }
        out += controls.last()
        return out
    }

    /**
     * Pressure rises over the first points, falls over the last ones and is higher on downstrokes; the pen slows
     * down in tight turns, which makes the ink heavier there.
     */
    private fun toStroke(
        xy: List<Pair<Float, Float>>,
        color: Int,
        width: Float,
        clock: Clock,
        speed: Float,
    ): Stroke {
        val n = xy.size
        val points = ArrayList<StrokePoint>(n)
        for (i in 0 until n) {
            val (x, y) = xy[i]
            if (i > 0) {
                val (px, py) = xy[i - 1]
                val distance = hypot(x - px, y - py)
                val turn = if (i < n - 1) turnAngle(xy[i - 1], xy[i], xy[i + 1]) else 0f
                val localSpeed = speed * (0.45f + 0.55f * (1f - turn / PI.toFloat()))
                clock.millis += max(4L, (distance / localSpeed).toLong())
            }
            // Downstrokes press harder than upstrokes, like with a fountain pen, plus a slow natural variation.
            val (nextX, nextY) = xy[min(i + 1, n - 1)]
            val (prevX, prevY) = xy[max(i - 1, 0)]
            val run = hypot(nextX - prevX, nextY - prevY)
            val downward = if (run > 0f) (nextY - prevY) / run else 0f
            val wave = 0.56f + 0.3f * downward + 0.08f * sin(i * 0.37f)
            val taper = min(1f, (i + 1) / 5f) * min(1f, (n - i) / 7f)
            val pressure = (wave * taper).coerceIn(0.12f, 1f)
            points += StrokePoint(x, y, pressure, clock.millis, ToolType.Stylus)
        }
        clock.millis += 180L
        return Stroke(points, color, width)
    }

    private fun turnAngle(a: Pair<Float, Float>, b: Pair<Float, Float>, c: Pair<Float, Float>): Float {
        val first = atan2(b.second - a.second, b.first - a.first)
        val second = atan2(c.second - b.second, c.first - b.first)
        var delta = abs(second - first)
        if (delta > PI.toFloat()) delta = 2 * PI.toFloat() - delta
        return delta
    }
}
