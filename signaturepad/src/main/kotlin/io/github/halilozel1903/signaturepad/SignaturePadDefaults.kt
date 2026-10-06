package io.github.halilozel1903.signaturepad

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Colors of a pad. The pad is paper: white with dark ink in light and dark themes alike, so what people see is what
 * gets exported.
 */
@Immutable
public data class SignaturePadColors(
    val background: Color = Color.White,
    val border: Color = Color(0xFFD5D8DE),
    val baseline: Color = Color(0xFFADB2BC),
    val hint: Color = Color(0xFF8A909C),
    val eraserOutline: Color = Color(0xFF6B7280),
    val grid: Color = Color(0xFFE3E6EB),
)

/** Defaults for [SignaturePad], [SketchPad] and the toolbar. */
public object SignaturePadDefaults {
    /** Blue black ink, `#1A1A2E`. */
    public val InkColor: Color = Color(0xFF1A1A2E)

    /** The default base pen width. */
    public val PenWidth: Dp = 3.dp

    /** The default eraser width. */
    public val EraserWidth: Dp = 24.dp

    /** The default pad shape. */
    public val Shape: Shape = RoundedCornerShape(16.dp)

    /** Ink colors for [PenColorPicker]: blue black, royal blue, red, green and orange. */
    public val InkColors: List<Color> = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF1E4FD8),
        Color(0xFFD32F2F),
        Color(0xFF2E7D32),
        Color(0xFFEF6C00),
    )

    /** Pen widths for [PenWidthPicker]. */
    public val PenWidths: List<Dp> = listOf(2.dp, 3.dp, 5.dp, 8.dp)

    /** The default colors. */
    public fun colors(
        background: Color = Color.White,
        border: Color = Color(0xFFD5D8DE),
        baseline: Color = Color(0xFFADB2BC),
        hint: Color = Color(0xFF8A909C),
        eraserOutline: Color = Color(0xFF6B7280),
        grid: Color = Color(0xFFE3E6EB),
    ): SignaturePadColors = SignaturePadColors(background, border, baseline, hint, eraserOutline, grid)
}
