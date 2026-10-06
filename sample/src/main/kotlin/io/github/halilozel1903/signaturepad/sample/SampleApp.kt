package io.github.halilozel1903.signaturepad.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.signaturepad.SignaturePadState
import io.github.halilozel1903.signaturepad.core.PenConfig
import io.github.halilozel1903.signaturepad.rememberSignaturePadState

enum class Tab(val label: String, val icon: ImageVector) {
    Deliver("Deliver", SampleIcons.Delivery),
    Sketch("Damage", SampleIcons.Brush),
    Export("Export", SampleIcons.Share),
}

/**
 * Parcelo: a delivery confirmation with a signature, a damage report sketch and an export preview. A bottom bar on
 * phones, a navigation rail on wide windows.
 */
@Composable
fun SampleApp(scene: Scene?) {
    var tab by rememberSaveable {
        mutableStateOf(
            when (scene) {
                Scene.Sketch -> Tab.Sketch
                Scene.Export -> Tab.Export
                else -> Tab.Deliver
            },
        )
    }
    // Fountain: strong contrast between light upstrokes and heavy downstrokes.
    val signatureState = rememberSignaturePadState(
        penColor = SampleColors.Navy,
        penWidth = 2.5.dp,
        penConfig = PenConfig.Fountain,
    )
    val sketchState = rememberSignaturePadState(penColor = SampleColors.Charcoal, penWidth = 3.dp)

    // Screenshot scenes start with pre-recorded ink: CI has no stylus. Loaded once, so rotating keeps your edits.
    var loaded by rememberSaveable { mutableStateOf(false) }
    val padding = with(LocalDensity.current) { 20.dp.toPx() }
    LaunchedEffect(scene) {
        if (scene != null && !loaded) {
            signatureState.load(SampleStrokes.signature(), padding = padding)
            sketchState.load(SampleStrokes.sketch(), padding = padding)
            loaded = true
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        if (wide) {
            Row(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                NavigationRail(windowInsets = WindowInsets(0, 0, 0, 0)) {
                    Tab.entries.forEach { item ->
                        NavigationRailItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                        )
                    }
                }
                Box(Modifier.weight(1f).fillMaxSize()) {
                    TabContent(tab, signatureState, sketchState, wide = true)
                }
            }
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar {
                        Tab.entries.forEach { item ->
                            NavigationBarItem(
                                selected = tab == item,
                                onClick = { tab = item },
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(item.label) },
                            )
                        }
                    }
                },
            ) { innerPadding ->
                Box(Modifier.fillMaxSize().padding(innerPadding)) {
                    TabContent(tab, signatureState, sketchState, wide = false)
                }
            }
        }
    }
}

@Composable
private fun TabContent(tab: Tab, signatureState: SignaturePadState, sketchState: SignaturePadState, wide: Boolean) {
    when (tab) {
        Tab.Deliver -> DeliveryScreen(signatureState, wide)
        Tab.Sketch -> SketchScreen(sketchState, wide)
        Tab.Export -> ExportScreen(signatureState)
    }
}
