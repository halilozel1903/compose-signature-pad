package io.github.halilozel1903.signaturepad.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SimplificationTest {

    @Test
    fun dropsPointsCloseToTheLine() {
        val p = points(0f, 0f, 5f, 1f, 10f, 0f)
        assertEquals(listOf(p[0], p[2]), Simplification.simplify(p, 2f))
        assertEquals(p, Simplification.simplify(p, 0.5f))
    }

    @Test
    fun keepsCorners() {
        val p = points(0f, 0f, 1f, 0.1f, 2f, -0.1f, 3f, 0f, 3.1f, 1f, 2.9f, 2f, 3f, 3f)
        val simplified = Simplification.simplify(p, 0.5f)
        assertEquals(listOf(p[0], p[3], p[6]), simplified)
    }

    @Test
    fun zeroToleranceDropsOnlyCollinearPoints() {
        val p = points(0f, 0f, 1f, 1f, 2f, 2f, 3f, 5f)
        assertEquals(listOf(p[0], p[2], p[3]), Simplification.simplify(p, 0f))
    }

    @Test
    fun shortListsAreReturnedAsIs() {
        val p = points(0f, 0f, 4f, 4f)
        assertSame(p, Simplification.simplify(p, 10f))
    }

    @Test
    fun handlesLongStrokesWithoutRecursion() {
        val p = List(5_000) { StrokePoint(it.toFloat(), if (it % 2 == 0) 0f else 3f) }
        assertEquals(5_000, Simplification.simplify(p, 1f).size)
        assertEquals(2, Simplification.simplify(p, 5f).size)
    }

    @Test
    fun perpendicularDistance() {
        val a = StrokePoint(0f, 0f)
        val b = StrokePoint(10f, 0f)
        assertEquals(3f, Simplification.perpendicularDistance(StrokePoint(4f, 3f), a, b))
        assertEquals(5f, Simplification.perpendicularDistance(StrokePoint(3f, 4f), a, a))
    }
}
