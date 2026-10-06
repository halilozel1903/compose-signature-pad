package io.github.halilozel1903.signaturepad.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.signaturepad.ClearButton
import io.github.halilozel1903.signaturepad.DrawingTool
import io.github.halilozel1903.signaturepad.PenColorPicker
import io.github.halilozel1903.signaturepad.PenWidthPicker
import io.github.halilozel1903.signaturepad.RedoButton
import io.github.halilozel1903.signaturepad.SignaturePadState
import io.github.halilozel1903.signaturepad.SignaturePadToolbar
import io.github.halilozel1903.signaturepad.SketchPad
import io.github.halilozel1903.signaturepad.ToolPicker
import io.github.halilozel1903.signaturepad.UndoButton

/** A damage report: sketch what arrived broken. Tools on the side on tablets, in a toolbar on phones. */
@Composable
fun SketchScreen(state: SignaturePadState, wide: Boolean) {
    if (wide) {
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                BrandHeader("Damage report", "Order PX-48213 · Parcel 2 of 2")
                SectionCard {
                    SectionLabel("Driver note")
                    Text(
                        "Top right corner crushed on arrival. Tape intact, contents not checked.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Tag("Reported 09:41", container = MaterialTheme.colorScheme.tertiaryContainer)
                }
                SectionCard {
                    SectionLabel("Tools")
                    ToolPicker(state)
                    SectionLabel("Width", Modifier.padding(top = 4.dp))
                    PenWidthPicker(state)
                    SectionLabel("Ink", Modifier.padding(top = 4.dp))
                    PenColorPicker(state, swatchSize = 24.dp)
                }
                Button(onClick = {}, enabled = !state.isEmpty, modifier = Modifier.fillMaxWidth()) {
                    Text("Attach to report")
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        toolHint(state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                    )
                    UndoButton(state)
                    RedoButton(state)
                    ClearButton(state)
                }
                SketchPad(state = state, modifier = Modifier.fillMaxSize())
            }
        }
    } else {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            BrandHeader("Damage report", "Order PX-48213 · Parcel 2 of 2")
            SignaturePadToolbar(
                state = state,
                showTools = true,
                showWidths = true,
                showColors = true,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SketchPad(state = state, palmRejection = false, modifier = Modifier.fillMaxSize())
        }
    }
}

private fun toolHint(state: SignaturePadState): String = when (state.tool) {
    DrawingTool.Pen -> "Pen · palm rejection on: rest your hand while you draw"
    DrawingTool.Eraser -> "Eraser · or flip the stylus to its eraser end"
    DrawingTool.StrokeEraser -> "Stroke eraser · touch a stroke to remove it"
}
