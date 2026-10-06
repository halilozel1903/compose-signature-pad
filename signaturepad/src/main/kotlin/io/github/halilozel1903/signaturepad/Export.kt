package io.github.halilozel1903.signaturepad

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.scale as scaleBy
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.halilozel1903.signaturepad.core.PenConfig
import io.github.halilozel1903.signaturepad.core.Signature
import kotlin.math.ceil

/**
 * Renders the signature to an image exactly as the pads draw it.
 *
 * @param background the background; [Color.Transparent] (the default) keeps the image transparent around the ink.
 * @param trimPadding when not null, crops to the ink plus this many pixels on every side.
 * @param scale multiplies the size, for example 2f for a sharper image in a PDF.
 * @param penConfig the width model; use the pad's to match the screen.
 */
public fun Signature.toImageBitmap(
    background: Color = Color.Transparent,
    trimPadding: Float? = null,
    scale: Float = 1f,
    penConfig: PenConfig = PenConfig.Default,
): ImageBitmap {
    require(scale > 0f) { "Scale must be positive, was $scale" }
    val source = if (trimPadding != null) trim(trimPadding, penConfig) else this
    val width = ceil(source.width * scale).toInt().coerceAtLeast(1)
    val height = ceil(source.height * scale).toInt().coerceAtLeast(1)

    // Ink goes on its own transparent image first, so eraser strokes only clear ink, never the background.
    val ink = ImageBitmap(width, height, ImageBitmapConfig.Argb8888)
    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(ink),
        size = Size(width.toFloat(), height.toFloat()),
    ) {
        scaleBy(scale, pivot = Offset.Zero) {
            drawInk(source.strokes, penConfig)
        }
    }
    if (background.alpha == 0f) return ink

    val result = ImageBitmap(width, height, ImageBitmapConfig.Argb8888)
    val canvas = Canvas(result)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { color = background })
    canvas.drawImage(ink, Offset.Zero, Paint())
    return result
}

/** [toImageBitmap] as an Android [Bitmap]. */
public fun Signature.toBitmap(
    background: Color = Color.Transparent,
    trimPadding: Float? = null,
    scale: Float = 1f,
    penConfig: PenConfig = PenConfig.Default,
): Bitmap = toImageBitmap(background, trimPadding, scale, penConfig).asAndroidBitmap()

/** [toImageBitmap] encoded as PNG bytes, ready to upload or save. */
public fun Signature.toPngBytes(
    background: Color = Color.Transparent,
    trimPadding: Float? = null,
    scale: Float = 1f,
    penConfig: PenConfig = PenConfig.Default,
): ByteArray = toBitmap(background, trimPadding, scale, penConfig).toPngBytes()
