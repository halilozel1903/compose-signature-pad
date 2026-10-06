package io.github.halilozel1903.signaturepad

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.signaturepad.core.Stroke

/** The paper pattern of a [SketchPad]. */
public enum class SketchPaper {
    /** Plain paper. */
    Plain,

    /** A dot grid, like a bullet journal. */
    Dots,

    /** A square grid, like graph paper. */
    Grid,
}

/**
 * A drawing surface with pen, eraser and stroke eraser (pick them with [SignaturePadState.tool] or [ToolPicker]).
 * The eraser end of a stylus erases on its own. Palm rejection is on by default, since sketching usually happens on a
 * tablet with a stylus.
 *
 * @param state the strokes and pen settings, from [rememberSignaturePadState].
 * @param enabled false makes the pad read only.
 * @param paper plain paper, dots or a grid. Not part of exports.
 * @param gridSpacing the distance between dots or grid lines.
 * @param palmRejection ignores fingers (a resting palm) while a stylus is in use.
 * @param onStrokeFinished called with each finished stroke (not for the stroke eraser).
 */
@Composable
public fun SketchPad(
    state: SignaturePadState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    paper: SketchPaper = SketchPaper.Dots,
    gridSpacing: Dp = 24.dp,
    palmRejection: Boolean = true,
    colors: SignaturePadColors = SignaturePadDefaults.colors(),
    shape: Shape = SignaturePadDefaults.Shape,
    contentDescription: String = "Sketch pad",
    onStrokeFinished: (Stroke) -> Unit = {},
) {
    Box(
        modifier
            .clip(shape)
            .background(colors.background)
            .border(1.dp, colors.border, shape)
            .semantics { this.contentDescription = contentDescription },
    ) {
        if (paper != SketchPaper.Plain) {
            Canvas(Modifier.fillMaxSize()) {
                val step = gridSpacing.toPx().coerceAtLeast(4f)
                if (paper == SketchPaper.Dots) {
                    val radius = 1.5.dp.toPx()
                    var y = step
                    while (y < size.height) {
                        var x = step
                        while (x < size.width) {
                            drawCircle(colors.grid, radius, Offset(x, y))
                            x += step
                        }
                        y += step
                    }
                } else {
                    val line = 1.dp.toPx()
                    var x = step
                    while (x < size.width) {
                        drawLine(colors.grid, Offset(x, 0f), Offset(x, size.height), line)
                        x += step
                    }
                    var y = step
                    while (y < size.height) {
                        drawLine(colors.grid, Offset(0f, y), Offset(size.width, y), line)
                        y += step
                    }
                }
            }
        }
        InkCanvas(
            state = state,
            enabled = enabled,
            palmRejection = palmRejection,
            eraserOutline = colors.eraserOutline,
            onStrokeFinished = onStrokeFinished,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
