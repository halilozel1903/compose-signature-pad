package io.github.halilozel1903.signaturepad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Undoes the last stroke, erase or clear. Disabled when there is nothing to undo. */
@Composable
public fun UndoButton(state: SignaturePadState, modifier: Modifier = Modifier) {
    IconButton(onClick = { state.undo() }, enabled = state.canUndo, modifier = modifier) {
        Icon(SignaturePadIcons.Undo, contentDescription = "Undo")
    }
}

/** Redoes the last undone step. Disabled when there is nothing to redo. */
@Composable
public fun RedoButton(state: SignaturePadState, modifier: Modifier = Modifier) {
    IconButton(onClick = { state.redo() }, enabled = state.canRedo, modifier = modifier) {
        Icon(SignaturePadIcons.Redo, contentDescription = "Redo")
    }
}

/** Clears the pad (undoable). Disabled when the pad is empty. */
@Composable
public fun ClearButton(state: SignaturePadState, modifier: Modifier = Modifier) {
    IconButton(onClick = { state.clear() }, enabled = state.strokes.isNotEmpty(), modifier = modifier) {
        Icon(SignaturePadIcons.Clear, contentDescription = "Clear")
    }
}

/** Swatches that set [SignaturePadState.penColor]. Picking a color also switches back to the pen. */
@Composable
public fun PenColorPicker(
    state: SignaturePadState,
    modifier: Modifier = Modifier,
    colors: List<Color> = SignaturePadDefaults.InkColors,
    swatchSize: Dp = 28.dp,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colors.forEach { color ->
            val selected = state.tool == DrawingTool.Pen && state.penColor == color
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(swatchSize + 12.dp)
                    .clip(CircleShape)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = {
                            state.penColor = color
                            state.tool = DrawingTool.Pen
                        },
                    )
                    .semantics { contentDescription = "Ink ${colorName(color)}" },
            ) {
                Box(
                    Modifier
                        .size(swatchSize + 8.dp)
                        .border(
                            width = 2.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape,
                        ),
                )
                Box(
                    Modifier
                        .size(swatchSize)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                )
                if (selected) {
                    Box(
                        Modifier
                            .size(swatchSize / 3)
                            .clip(CircleShape)
                            .background(if (color.luminance() > 0.5f) Color.Black else Color.White),
                    )
                }
            }
        }
    }
}

/** Dots of each width that set [SignaturePadState.penWidth]. */
@Composable
public fun PenWidthPicker(
    state: SignaturePadState,
    modifier: Modifier = Modifier,
    widths: List<Dp> = SignaturePadDefaults.PenWidths,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        widths.forEach { width ->
            val selected = state.penWidth == width
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { state.penWidth = width })
                    .semantics { contentDescription = "Pen width ${width.value.toInt()}" },
            ) {
                Box(
                    Modifier
                        .size((width * 2f).coerceIn(4.dp, 24.dp))
                        .clip(CircleShape)
                        .background(
                            if (state.tool == DrawingTool.Pen) state.penColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
            }
        }
    }
}

/** Toggles between [DrawingTool.Pen], [DrawingTool.Eraser] and, optionally, [DrawingTool.StrokeEraser]. */
@Composable
public fun ToolPicker(
    state: SignaturePadState,
    modifier: Modifier = Modifier,
    showStrokeEraser: Boolean = true,
) {
    Row(modifier = modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ToolToggle(state, DrawingTool.Pen, "Pen")
        ToolToggle(state, DrawingTool.Eraser, "Eraser")
        if (showStrokeEraser) ToolToggle(state, DrawingTool.StrokeEraser, "Stroke eraser")
    }
}

@Composable
private fun ToolToggle(state: SignaturePadState, tool: DrawingTool, label: String) {
    FilledTonalIconToggleButton(
        checked = state.tool == tool,
        onCheckedChange = { state.tool = tool },
    ) {
        val icon = when (tool) {
            DrawingTool.Pen -> SignaturePadIcons.Pen
            DrawingTool.Eraser -> SignaturePadIcons.Eraser
            DrawingTool.StrokeEraser -> SignaturePadIcons.StrokeEraser
        }
        Icon(icon, contentDescription = label)
    }
}

/**
 * A ready made toolbar: undo, redo and clear, then optionally pen widths, ink colors and tools. Scrolls sideways
 * when it does not fit.
 */
@Composable
public fun SignaturePadToolbar(
    state: SignaturePadState,
    modifier: Modifier = Modifier,
    showWidths: Boolean = false,
    showColors: Boolean = false,
    showTools: Boolean = false,
    widths: List<Dp> = SignaturePadDefaults.PenWidths,
    colors: List<Color> = SignaturePadDefaults.InkColors,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UndoButton(state)
        RedoButton(state)
        ClearButton(state)
        if (showTools) {
            ToolbarDivider()
            ToolPicker(state)
        }
        if (showWidths) {
            ToolbarDivider()
            PenWidthPicker(state, widths = widths)
        }
        if (showColors) {
            ToolbarDivider()
            PenColorPicker(state, colors = colors)
        }
    }
}

@Composable
private fun ToolbarDivider() {
    Spacer(Modifier.width(4.dp))
    VerticalDivider(Modifier.height(28.dp).padding(horizontal = 4.dp))
    Spacer(Modifier.width(4.dp))
}

private fun colorName(color: Color): String {
    val rgb = color.toArgb() and 0xFFFFFF
    return "#" + rgb.toString(16).uppercase().padStart(6, '0')
}
