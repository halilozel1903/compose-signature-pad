package io.github.halilozel1903.signaturepad.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Parcelo's indigo and amber theme. */
@Composable
fun SampleTheme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = if (dark) {
        darkColorScheme(
            primary = Color(0xFFBAC3FF),
            onPrimary = Color(0xFF1A2678),
            primaryContainer = Color(0xFF333F90),
            onPrimaryContainer = Color(0xFFDEE0FF),
            secondary = Color(0xFFC4C5DD),
            secondaryContainer = Color(0xFF434659),
            onSecondaryContainer = Color(0xFFE0E1F9),
            tertiary = Color(0xFFFFB95C),
            onTertiary = Color(0xFF462A00),
            tertiaryContainer = Color(0xFF643F00),
            onTertiaryContainer = Color(0xFFFFDDB6),
            background = Color(0xFF121318),
            onBackground = Color(0xFFE4E1E9),
            surface = Color(0xFF121318),
            onSurface = Color(0xFFE4E1E9),
            surfaceVariant = Color(0xFF46464F),
            onSurfaceVariant = Color(0xFFC7C5D0),
            surfaceContainerLowest = Color(0xFF0D0E13),
            surfaceContainerLow = Color(0xFF1B1B21),
            surfaceContainer = Color(0xFF1F1F25),
            surfaceContainerHigh = Color(0xFF292A2F),
            surfaceContainerHighest = Color(0xFF34343A),
            outline = Color(0xFF90909A),
            outlineVariant = Color(0xFF46464F),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF3F4AA8),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDEE0FF),
            onPrimaryContainer = Color(0xFF000C62),
            secondary = Color(0xFF5B5D72),
            secondaryContainer = Color(0xFFE0E1F9),
            onSecondaryContainer = Color(0xFF181A2C),
            tertiary = Color(0xFF8A5100),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFDDB6),
            onTertiaryContainer = Color(0xFF2C1700),
            background = Color(0xFFF4F4FA),
            onBackground = Color(0xFF1B1B21),
            surface = Color(0xFFF4F4FA),
            onSurface = Color(0xFF1B1B21),
            surfaceVariant = Color(0xFFE3E1EC),
            onSurfaceVariant = Color(0xFF46464F),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFFFFFF),
            surfaceContainer = Color(0xFFEDEDF4),
            surfaceContainerHigh = Color(0xFFE8E7EF),
            surfaceContainerHighest = Color(0xFFE2E1E9),
            outline = Color(0xFF777680),
            outlineVariant = Color(0xFFC7C5D0),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
