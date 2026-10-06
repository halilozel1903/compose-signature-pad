package io.github.halilozel1903.signaturepad.core

/** What to do with a new pointer. */
public enum class PointerDecision {
    /** Draw with it. */
    Accept,

    /** Ignore it: a palm or finger while a stylus is in use. */
    Ignore,

    /**
     * Draw with it and cancel strokes that fingers have started: the stylus arrived after the palm touched down,
     * so those strokes are most likely the palm.
     */
    AcceptAndCancelFingers,
}

/**
 * Palm rejection for drawing with a stylus while the hand rests on the screen. Opt in: when [enabled] is false,
 * every pointer is accepted.
 *
 * The rule: finger (and unknown) input is ignored while a stylus is active, which is while a stylus pointer is
 * down and for [graceMillis] after it lifts (between letters of a signature). Mouse input is always accepted.
 * When a stylus touches down while finger strokes are in progress, those strokes are cancelled.
 *
 * Call [onDown] for every new pointer and [onUp] when it lifts or is cancelled. Not thread safe; use it from the
 * input thread.
 */
public class PalmRejection(
    public val enabled: Boolean = true,
    public val graceMillis: Long = DEFAULT_GRACE_MILLIS,
) {
    private val stylusPointers = mutableSetOf<Long>()
    private var lastStylusUpMillis: Long = Long.MIN_VALUE

    init {
        require(graceMillis >= 0) { "graceMillis must not be negative" }
    }

    /** True while a stylus is down or lifted less than [graceMillis] before [nowMillis]. */
    public fun isStylusActive(nowMillis: Long): Boolean =
        stylusPointers.isNotEmpty() ||
            (lastStylusUpMillis != Long.MIN_VALUE && nowMillis - lastStylusUpMillis <= graceMillis)

    /** A pointer [pointerId] of [toolType] touched down at [timeMillis]. */
    public fun onDown(pointerId: Long, toolType: ToolType, timeMillis: Long): PointerDecision {
        if (toolType.isStylus) {
            stylusPointers += pointerId
            return if (enabled) PointerDecision.AcceptAndCancelFingers else PointerDecision.Accept
        }
        if (!enabled || toolType == ToolType.Mouse) return PointerDecision.Accept
        return if (isStylusActive(timeMillis)) PointerDecision.Ignore else PointerDecision.Accept
    }

    /** Pointer [pointerId] lifted or was cancelled at [timeMillis]. */
    public fun onUp(pointerId: Long, timeMillis: Long) {
        if (stylusPointers.remove(pointerId)) lastStylusUpMillis = timeMillis
    }

    /** Forgets every pointer and the last stylus time. */
    public fun reset() {
        stylusPointers.clear()
        lastStylusUpMillis = Long.MIN_VALUE
    }

    public companion object {
        /** Long enough to cover the gap between two words of a signature. */
        public const val DEFAULT_GRACE_MILLIS: Long = 600L
    }
}
