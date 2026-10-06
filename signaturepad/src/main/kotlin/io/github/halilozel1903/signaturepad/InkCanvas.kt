package io.github.halilozel1903.signaturepad

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke as OutlineStyle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.signaturepad.core.PalmRejection
import io.github.halilozel1903.signaturepad.core.PointerDecision
import io.github.halilozel1903.signaturepad.core.Stroke
import io.github.halilozel1903.signaturepad.core.StrokePoint
import io.github.halilozel1903.signaturepad.core.ToolType

/**
 * The drawing layer shared by [SignaturePad] and [SketchPad]: captures pointer input into [state] and draws the
 * strokes. It draws in its own offscreen layer so eraser strokes clear ink, not what is behind the pad.
 */
@Composable
internal fun InkCanvas(
    state: SignaturePadState,
    enabled: Boolean,
    palmRejection: Boolean,
    eraserOutline: Color,
    onStrokeFinished: (Stroke) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnStrokeFinished by rememberUpdatedState(onStrokeFinished)
    val cache = remember { InkCache() }
    val input = if (enabled) {
        Modifier.pointerInput(state, palmRejection) {
            captureInk(state, palmRejection) { currentOnStrokeFinished(it) }
        }
    } else {
        Modifier
    }
    Canvas(
        modifier
            .onSizeChanged { state.onCanvasSize(it) }
            .clipToBounds()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .then(input),
    ) {
        val config = state.penConfig
        drawInk(state.strokes, config, cache)
        state.activeStroke()?.let { drawInk(it, sampleActive(it, config)) }
        state.eraserCursor?.let { center ->
            drawCircle(
                color = eraserOutline,
                radius = state.eraserCursorRadius,
                center = center,
                style = OutlineStyle(width = 1.5.dp.toPx()),
            )
        }
    }
}

/**
 * Follows one drawing pointer at a time. With [palmRejection], fingers are ignored while a stylus is active and a
 * stylus touching down cancels a stroke a finger (most likely the palm) has started.
 */
@OptIn(ExperimentalComposeUiApi::class)
private suspend fun PointerInputScope.captureInk(
    state: SignaturePadState,
    palmRejection: Boolean,
    onStrokeFinished: (Stroke) -> Unit,
) {
    val palm = PalmRejection(enabled = palmRejection)
    var activeId: PointerId? = null
    var activeIsStylus = false
    try {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                for (change in event.changes) {
                    val toolType = change.type.toToolType()
                    if (change.changedToDownIgnoreConsumed()) {
                        val decision = palm.onDown(change.id.value, toolType, change.uptimeMillis)
                        if (decision == PointerDecision.Ignore) {
                            change.consume()
                            continue
                        }
                        if (decision == PointerDecision.AcceptAndCancelFingers && activeId != null && !activeIsStylus) {
                            state.cancelStroke()
                            activeId = null
                        }
                        if (activeId == null) {
                            activeId = change.id
                            activeIsStylus = toolType.isStylus
                            state.startStroke(change.toStrokePoint(toolType), this)
                            change.consume()
                        }
                    } else if (change.id == activeId) {
                        if (change.pressed) {
                            change.historical.forEach { historical ->
                                state.extendStroke(
                                    StrokePoint(
                                        x = historical.position.x,
                                        y = historical.position.y,
                                        pressure = change.pressure,
                                        timeMillis = historical.uptimeMillis,
                                        toolType = toolType,
                                    ),
                                )
                            }
                            state.extendStroke(change.toStrokePoint(toolType))
                        } else {
                            // The pressure at lift is often 0: keep the last pressure so the stroke does not end in a sudden taper.
                            state.extendStroke(change.toStrokePoint(toolType, pressure = state.lastPressure()))
                            state.finishStroke()?.let(onStrokeFinished)
                            activeId = null
                        }
                        change.consume()
                    }
                    if (change.changedToUpIgnoreConsumed()) {
                        palm.onUp(change.id.value, change.uptimeMillis)
                    }
                }
            }
        }
    } finally {
        // The gesture was cancelled (the pad left the composition or its keys changed): drop the half stroke.
        state.cancelStroke()
    }
}

private fun PointerInputChange.toStrokePoint(toolType: ToolType, pressure: Float = this.pressure): StrokePoint =
    StrokePoint(
        x = position.x,
        y = position.y,
        pressure = pressure.coerceIn(0f, 1f),
        timeMillis = uptimeMillis,
        toolType = toolType,
    )

internal fun PointerType.toToolType(): ToolType = when (this) {
    PointerType.Touch -> ToolType.Finger
    PointerType.Stylus -> ToolType.Stylus
    PointerType.Eraser -> ToolType.StylusEraser
    PointerType.Mouse -> ToolType.Mouse
    else -> ToolType.Unknown
}
