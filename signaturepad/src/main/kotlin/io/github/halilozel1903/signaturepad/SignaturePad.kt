package io.github.halilozel1903.signaturepad

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.halilozel1903.signaturepad.core.Stroke

/**
 * A signature field: a paper colored pad with a baseline, an "×" mark and a "Sign here" hint until someone signs.
 *
 * Strokes follow the finger or stylus with speed and pressure aware width (see [SignaturePadState.penConfig]).
 * Read the result with [SignaturePadState.toImageBitmap], [SignaturePadState.toPngBytes],
 * [SignaturePadState.toSvg] or [SignaturePadState.encode].
 *
 * @param state the strokes and pen settings, from [rememberSignaturePadState].
 * @param enabled false makes the pad read only.
 * @param hint the text on an empty pad, or null for none.
 * @param showBaseline draws the dashed line to sign on and the "×" mark.
 * @param baselineOffset the distance from the bottom of the pad to the baseline.
 * @param palmRejection ignores fingers (a resting palm) while a stylus is in use. Off by default, so fingers work
 *   on phones; turn it on for tablets with a stylus.
 * @param onStrokeFinished called with each finished stroke.
 */
@Composable
public fun SignaturePad(
    state: SignaturePadState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hint: String? = "Sign here",
    showBaseline: Boolean = true,
    baselineOffset: Dp = 48.dp,
    palmRejection: Boolean = false,
    colors: SignaturePadColors = SignaturePadDefaults.colors(),
    shape: Shape = SignaturePadDefaults.Shape,
    contentDescription: String = "Signature pad",
    onStrokeFinished: (Stroke) -> Unit = {},
) {
    Box(
        modifier
            .clip(shape)
            .background(colors.background)
            .border(1.dp, colors.border, shape)
            .semantics { this.contentDescription = contentDescription },
    ) {
        if (showBaseline) {
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height - baselineOffset.toPx()
                val inset = 24.dp.toPx()
                val stroke = 1.5.dp.toPx()
                drawLine(
                    color = colors.baseline,
                    start = Offset(inset, y),
                    end = Offset(size.width - inset, y),
                    strokeWidth = stroke,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
                )
                // The "×" just above the line, where the signature starts.
                val mark = 7.dp.toPx()
                val cx = inset + mark
                val cy = y - mark - 6.dp.toPx()
                drawLine(colors.baseline, Offset(cx - mark, cy - mark), Offset(cx + mark, cy + mark), stroke * 1.4f, StrokeCap.Round)
                drawLine(colors.baseline, Offset(cx - mark, cy + mark), Offset(cx + mark, cy - mark), stroke * 1.4f, StrokeCap.Round)
            }
        }
        if (hint != null && state.isEmpty) {
            Text(
                text = hint,
                color = colors.hint,
                style = LocalTextStyle.current.copy(fontSize = 18.sp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 56.dp, bottom = baselineOffset + 10.dp),
            )
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
