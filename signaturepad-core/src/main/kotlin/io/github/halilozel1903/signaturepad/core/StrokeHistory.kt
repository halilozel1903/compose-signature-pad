package io.github.halilozel1903.signaturepad.core

/**
 * Immutable undo and redo history of a list of strokes. Every change returns a new history; adding a stroke, erasing
 * or clearing after an undo drops the redo steps. Clearing is a step of its own, so it can be undone.
 *
 * @property strokes the current strokes.
 * @property maxSteps how many undo steps are kept; older ones are forgotten.
 */
public class StrokeHistory private constructor(
    public val strokes: List<Stroke>,
    private val undoStack: List<List<Stroke>>,
    private val redoStack: List<List<Stroke>>,
    public val maxSteps: Int,
) {
    /** A history starting at [strokes] with nothing to undo. */
    public constructor(strokes: List<Stroke> = emptyList(), maxSteps: Int = DEFAULT_MAX_STEPS) :
        this(strokes, emptyList(), emptyList(), maxSteps)

    init {
        require(maxSteps >= 1) { "maxSteps must be at least 1, was $maxSteps" }
    }

    public val canUndo: Boolean get() = undoStack.isNotEmpty()
    public val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** The number of steps that can be undone. */
    public val undoCount: Int get() = undoStack.size

    /** The number of steps that can be redone. */
    public val redoCount: Int get() = redoStack.size

    /** Adds [stroke] on top. Empty strokes are ignored. */
    public fun add(stroke: Stroke): StrokeHistory = if (stroke.isEmpty) this else replaceAll(strokes + stroke)

    /** Replaces every stroke with [newStrokes] as one undoable step, for example after erasing whole strokes. */
    public fun replaceAll(newStrokes: List<Stroke>): StrokeHistory {
        if (newStrokes == strokes) return this
        return StrokeHistory(newStrokes, (undoStack + listOf(strokes)).takeLast(maxSteps), emptyList(), maxSteps)
    }

    /** Removes every stroke as one undoable step. Does nothing when already empty. */
    public fun clear(): StrokeHistory = if (strokes.isEmpty()) this else replaceAll(emptyList())

    /** Goes one step back, or returns this when there is nothing to undo. */
    public fun undo(): StrokeHistory {
        if (undoStack.isEmpty()) return this
        return StrokeHistory(undoStack.last(), undoStack.dropLast(1), redoStack + listOf(strokes), maxSteps)
    }

    /** Goes one step forward again, or returns this when there is nothing to redo. */
    public fun redo(): StrokeHistory {
        if (redoStack.isEmpty()) return this
        return StrokeHistory(redoStack.last(), undoStack + listOf(strokes), redoStack.dropLast(1), maxSteps)
    }

    /** The same strokes with the undo and redo steps forgotten. */
    public fun forgetSteps(): StrokeHistory = StrokeHistory(strokes, maxSteps)

    override fun equals(other: Any?): Boolean = other is StrokeHistory &&
        strokes == other.strokes && undoStack == other.undoStack && redoStack == other.redoStack &&
        maxSteps == other.maxSteps

    override fun hashCode(): Int = ((strokes.hashCode() * 31 + undoStack.hashCode()) * 31 + redoStack.hashCode()) * 31 + maxSteps

    override fun toString(): String = "StrokeHistory(strokes=${strokes.size}, undo=$undoCount, redo=$redoCount)"

    public companion object {
        public const val DEFAULT_MAX_STEPS: Int = 100
    }
}
