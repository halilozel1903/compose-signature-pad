package io.github.halilozel1903.signaturepad.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeometryTest {

    @Test
    fun segmentsStartAndEndAtTheRawPoints() {
        val s = stroke(0f, 0f, 10f, 0f, 20f, 10f, 30f, 10f)
        val segments = StrokeSmoothing.segments(s, PenConfig.Uniform)
        assertEquals(4, segments.size)
        assertEquals(Point(0f, 0f), segments.first().start)
        assertEquals(Point(30f, 10f), segments.last().end)
        assertTrue(segments[1] is QuadSegment)
        assertEquals(Point(10f, 0f), (segments[1] as QuadSegment).control)
        // Joined end to end.
        for (i in 1 until segments.size) assertEquals(segments[i - 1].end, segments[i].start)
    }

    @Test
    fun samplesFollowTheCurve() {
        val segments = StrokeSmoothing.segments(stroke(0f, 0f, 10f, 0f, 10f, 10f), PenConfig.Uniform)
        val samples = StrokeSmoothing.sample(segments, maxStep = 1f)
        assertEquals(StrokeSample(0f, 0f, 4f), samples.first())
        assertEquals(StrokeSample(10f, 10f, 4f), samples.last())
        assertTrue(samples.size > 15)
        for (i in 1 until samples.size) {
            val step = distance(samples[i - 1].x, samples[i - 1].y, samples[i].x, samples[i].y)
            assertTrue(step <= 1.01f, "step $step")
        }
    }

    @Test
    fun dotHasOneSample() {
        val samples = StrokeSmoothing.sample(stroke(3f, 4f), PenConfig.Uniform)
        assertEquals(listOf(StrokeSample(3f, 4f, 4f)), samples)
    }

    @Test
    fun boundsIncludeHalfTheWidth() {
        val s = stroke(10f, 20f, 50f, 60f, width = 4f)
        assertEquals(BoundingBox(10f, 20f, 50f, 60f), s.pointBounds())
        assertEquals(BoundingBox(8f, 18f, 52f, 62f), s.bounds(PenConfig.Uniform))
    }

    @Test
    fun signatureBoundsIgnoreErasersAndEmptyIsNull() {
        val sig = Signature(
            listOf(stroke(10f, 10f, 20f, 10f, width = 2f), stroke(0f, 0f, 100f, 100f, kind = StrokeKind.Eraser)),
            200f,
            200f,
        )
        assertEquals(BoundingBox(9f, 9f, 21f, 11f), sig.bounds(PenConfig.Uniform))
        assertNull(Signature(emptyList(), 10f, 10f).bounds())
        assertTrue(Signature(listOf(stroke(1f, 1f, kind = StrokeKind.Eraser)), 10f, 10f).isEmpty)
        assertFalse(sig.isEmpty)
    }

    @Test
    fun trimCropsToInkWithPadding() {
        val sig = Signature(listOf(stroke(50f, 40f, 90f, 60f, width = 2f)), 400f, 300f)
        val trimmed = sig.trim(padding = 5f, config = PenConfig.Uniform)
        assertEquals(52f, trimmed.width)
        assertEquals(32f, trimmed.height)
        assertEquals(BoundingBox(5f, 5f, 47f, 27f), trimmed.bounds(PenConfig.Uniform))
        val empty = Signature(emptyList(), 100f, 50f)
        assertEquals(empty, empty.trim(8f))
    }

    @Test
    fun fitIntoScalesAndCenters() {
        val sig = Signature(listOf(stroke(10f, 10f, 30f, 10f, width = 2f)), 40f, 20f)
        val fitted = sig.fitInto(110f, 110f, config = PenConfig.Uniform)
        assertEquals(110f, fitted.width)
        assertEquals(10f, fitted.strokes[0].width)
        assertEquals(BoundingBox(0f, 50f, 110f, 60f), fitted.bounds(PenConfig.Uniform))
        val capped = sig.fitInto(110f, 110f, maxScale = 2f, config = PenConfig.Uniform)
        assertEquals(4f, capped.strokes[0].width)
    }

    @Test
    fun hitTestFindsStrokesNearThePoint() {
        val s = stroke(0f, 0f, 100f, 0f, width = 4f)
        assertTrue(StrokeHitTest.hits(s, 50f, 5f, radius = 4f, config = PenConfig.Uniform))
        assertFalse(StrokeHitTest.hits(s, 50f, 10f, radius = 4f, config = PenConfig.Uniform))
        val other = stroke(0f, 50f, 100f, 50f)
        assertEquals(listOf(other), StrokeHitTest.eraseAt(listOf(s, other), 30f, 1f, 3f))
        assertFalse(StrokeHitTest.hits(stroke(0f, 0f, 10f, 0f, kind = StrokeKind.Eraser), 5f, 0f, 5f))
    }

    @Test
    fun distanceToSegment() {
        assertEquals(5f, StrokeHitTest.distanceToSegment(5f, 5f, 0f, 0f, 10f, 0f))
        assertEquals(5f, StrokeHitTest.distanceToSegment(-3f, 4f, 0f, 0f, 10f, 0f))
        assertEquals(5f, StrokeHitTest.distanceToSegment(3f, 4f, 0f, 0f, 0f, 0f))
    }
}
