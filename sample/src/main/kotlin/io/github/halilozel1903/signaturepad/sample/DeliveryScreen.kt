package io.github.halilozel1903.signaturepad.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.signaturepad.ClearButton
import io.github.halilozel1903.signaturepad.RedoButton
import io.github.halilozel1903.signaturepad.SignaturePad
import io.github.halilozel1903.signaturepad.SignaturePadState
import io.github.halilozel1903.signaturepad.UndoButton

/** The recipient signs for the parcels. */
@Composable
fun DeliveryScreen(state: SignaturePadState, wide: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.widthIn(max = 640.dp),
        ) {
            BrandHeader("Delivery confirmation", "Stop 7 of 12 · Order PX-48213")
            SectionCard {
                SectionLabel("Recipient")
                Text("Emma Lindgren", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Storgatan 14, 753 20 Uppsala",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag("2 parcels")
                    Tag("4.2 kg")
                    Tag("Signature required", container = MaterialTheme.colorScheme.tertiaryContainer)
                }
                Spacer(Modifier.height(2.dp))
                CheckRow("Parcels handed over intact")
                CheckRow("Photo ID checked")
            }
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel("Recipient signature", Modifier.weight(1f))
                    UndoButton(state)
                    RedoButton(state)
                    ClearButton(state)
                }
                SignaturePad(
                    state = state,
                    palmRejection = wide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (wide) 260.dp else 200.dp),
                )
                Text(
                    if (state.isEmpty) "Sign with a finger or a stylus" else inkSummary(state.toSignature()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {},
                enabled = !state.isEmpty,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Confirm delivery", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
