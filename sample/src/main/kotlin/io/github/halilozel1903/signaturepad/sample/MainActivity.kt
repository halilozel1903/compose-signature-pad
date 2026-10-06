package io.github.halilozel1903.signaturepad.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * Parcelo, a fictional courier app built with compose-signature-pad: recipients sign for deliveries, drivers sketch
 * damage reports, and the export tab shows what the app uploads. `scripts/screenshots.sh` starts it with
 * `--es scene <scene>` to open a fixed screen with pre-recorded ink:
 *
 * - `signature`: the delivery form, signed (phones)
 * - `sketch`: the damage report sketch with its tools (tablets in landscape)
 * - `export`: the signature exported as PNG, SVG and the compact text form (phones)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            SampleTheme(dark = isSystemInDarkTheme()) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    SampleApp(scene = scene)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}
