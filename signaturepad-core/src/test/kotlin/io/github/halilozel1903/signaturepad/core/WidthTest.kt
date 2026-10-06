package io.github.halilozel1903.signaturepad.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WidthTest {

    @Test
    fun uniformKeepsTheBaseWidth() {
        val widths = StrokeWidths.compute(stroke(0f, 0f, 50f, 0f, 300f, 0f, width = 5f), PenConfig.Uniform)
        assertEquals(listOf(5f, 5f, 5f), widths.toList())
    }

    @Test
    fun slowFingerStrokeIsThickFastOneThin() {
        // Default config without pressure: t = lerp(0.5, slowness, 0.7), width = base * lerp(0.5, 1.5, t).
        val slow = Stroke(points(0f, 0f, 0.1f, 0f, 0.2f, 0f, stepMillis = 100), INK, 10f)
        val fast = Stroke(points(0f, 0f, 100f, 0f, 200f, 0f, stepMillis = 10), INK, 10f)
        StrokeWidths.compute(slow).forEach { assertEquals(13.5f, it, 0.01f) }
        StrokeWidths.compute(fast).forEach { assertEquals(6.5f, it, 0.01f) }
    }

    @Test
    fun pressureMakesStylusStrokesHeavier() {
        fun stylus(pressure: Float) = Stroke(
            List(3) { StrokePoint(it * 5f, 0f, pressure, it * 10L, ToolType.Stylus) },
            INK,
            10f,
        )
        val light = StrokeWidths.compute(stylus(0.1f))
        val heavy = StrokeWidths.compute(stylus(1f))
        assertTrue(heavy[1] > light[1] * 1.8f, "heavy ${heavy[1]} light ${light[1]}")
    }

    @Test
    fun constantFingerPressureIsIgnoredButVaryingPressureIsUsed() {
        assertFalse(StrokeWidths.hasPressure(points(0f, 0f, 1f, 1f)))
        assertTrue(StrokeWidths.hasPressure(points(0f, 0f, toolType = ToolType.Stylus)))
        val varying = listOf(StrokePoint(0f, 0f, 0.2f), StrokePoint(1f, 1f, 0.9f))
        assertTrue(StrokeWidths.hasPressure(varying))
    }

    @Test
    fun smoothingAvoidsJumps() {
        val points = listOf(
            StrokePoint(0f, 0f, timeMillis = 0),
            StrokePoint(1f, 0f, timeMillis = 100),
            StrokePoint(400f, 0f, timeMillis = 110),
        )
        val widths = StrokeWidths.compute(Stroke(points, INK, 10f))
        // Without smoothing the last width would drop straight to 6.5.
        assertTrue(widths[2] > 9f && widths[2] < widths[1], widths.toList().toString())
    }

    @Test
    fun eraserStrokesKeepTheirBaseWidth() {
        val eraser = Stroke(points(0f, 0f, 300f, 0f, 301f, 0f), INK, 20f, StrokeKind.Eraser)
        assertEquals(listOf(20f, 20f, 20f), StrokeWidths.compute(eraser).toList())
    }

    @Test
    fun velocity() {
        val p = points(0f, 0f, 30f, 40f, stepMillis = 10)
        assertEquals(5f, StrokeWidths.velocityAt(p, 1))
        assertEquals(5f, StrokeWidths.velocityAt(p, 0))
        assertEquals(0f, StrokeWidths.velocityAt(p.take(1), 0))
    }
}
