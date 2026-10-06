package io.github.halilozel1903.signaturepad.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HistoryAndPalmTest {

    private val a = stroke(0f, 0f, 1f, 1f)
    private val b = stroke(2f, 2f, 3f, 3f)

    @Test
    fun undoAndRedo() {
        val history = StrokeHistory().add(a).add(b)
        assertEquals(listOf(a, b), history.strokes)
        val undone = history.undo()
        assertEquals(listOf(a), undone.strokes)
        assertTrue(undone.canRedo)
        assertEquals(listOf(a, b), undone.redo().strokes)
        assertEquals(emptyList<Stroke>(), undone.undo().strokes)
        assertFalse(undone.undo().canUndo)
    }

    @Test
    fun newStrokeDropsRedo() {
        val history = StrokeHistory().add(a).add(b).undo().add(b.translate(5f, 5f))
        assertFalse(history.canRedo)
        assertEquals(2, history.strokes.size)
    }

    @Test
    fun clearCanBeUndone() {
        val cleared = StrokeHistory().add(a).add(b).clear()
        assertTrue(cleared.strokes.isEmpty())
        assertEquals(listOf(a, b), cleared.undo().strokes)
        assertSame(cleared, cleared.clear())
    }

    @Test
    fun emptyStrokesAndNoOpsAddNoSteps() {
        val history = StrokeHistory().add(a)
        assertSame(history, history.add(Stroke(emptyList())))
        assertSame(history, history.replaceAll(listOf(a)))
        val empty = StrokeHistory()
        assertSame(empty, empty.undo())
        assertSame(empty, empty.redo())
    }

    @Test
    fun keepsAtMostMaxSteps() {
        var history = StrokeHistory(maxSteps = 3)
        repeat(10) { history = history.add(stroke(it.toFloat(), 0f)) }
        assertEquals(3, history.undoCount)
        repeat(5) { history = history.undo() }
        assertEquals(7, history.strokes.size)
        assertEquals(StrokeHistory(history.strokes, 3), history.forgetSteps())
    }

    @Test
    fun palmRejectionIsOptIn() {
        val off = PalmRejection(enabled = false)
        assertEquals(PointerDecision.Accept, off.onDown(1, ToolType.Stylus, 0))
        assertEquals(PointerDecision.Accept, off.onDown(2, ToolType.Finger, 5))
    }

    @Test
    fun fingersAreIgnoredWhileAStylusIsActive() {
        val palm = PalmRejection(graceMillis = 500)
        assertEquals(PointerDecision.Accept, palm.onDown(1, ToolType.Finger, 0))
        palm.onUp(1, 10)
        assertEquals(PointerDecision.AcceptAndCancelFingers, palm.onDown(2, ToolType.Stylus, 100))
        assertEquals(PointerDecision.Ignore, palm.onDown(3, ToolType.Finger, 120))
        assertEquals(PointerDecision.Accept, palm.onDown(4, ToolType.Mouse, 130))
        palm.onUp(2, 1000)
        assertTrue(palm.isStylusActive(1400))
        assertEquals(PointerDecision.Ignore, palm.onDown(5, ToolType.Finger, 1400))
        assertFalse(palm.isStylusActive(1501))
        assertEquals(PointerDecision.Accept, palm.onDown(6, ToolType.Finger, 1501))
        assertEquals(PointerDecision.AcceptAndCancelFingers, palm.onDown(7, ToolType.StylusEraser, 1600))
        palm.reset()
        assertEquals(PointerDecision.Accept, palm.onDown(8, ToolType.Unknown, 1601))
    }
}
