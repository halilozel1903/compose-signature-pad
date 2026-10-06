package io.github.halilozel1903.signaturepad.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.signaturepad.SignaturePadState
import io.github.halilozel1903.signaturepad.core.PenConfig
import io.github.halilozel1903.signaturepad.core.Signature
import io.github.halilozel1903.signaturepad.core.SvgOptions
import io.github.halilozel1903.signaturepad.toImageBitmap
import io.github.halilozel1903.signaturepad.toPngBytes

/** What the app uploads: the signature as transparent and white PNGs, as SVG and in the compact text form. */
@Composable
fun ExportScreen(state: SignaturePadState) {
    // The signature from the Deliver tab, or the pre-recorded one until someone has signed there.
    val signature = if (!state.isEmpty && state.canvasSize.width > 0) state.toSignature() else SampleStrokes.signature()
    val penConfig = state.penConfig
    val exports = remember(signature, penConfig) { Exports.of(signature, penConfig) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.widthIn(max = 640.dp)) {
            BrandHeader("Export preview", "Emma Lindgren · Order PX-48213")
            SectionCard {
                ExportTitle("PNG · transparent", "${exports.width} x ${exports.height} px · ${kilobytes(exports.transparentPng.size)}")
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                ) {
                    Checkerboard(Modifier.matchParentSize())
                    ExportImage(exports.transparent)
                }
            }
            SectionCard {
                ExportTitle("PNG · white background", kilobytes(exports.whitePng.size))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                ) {
                    ExportImage(exports.white)
                }
            }
            SectionCard {
                ExportTitle("SVG · variable width paths", "${exports.svgPaths} paths · ${kilobytes(exports.svg.length)}")
                Code(exports.svg.lineSequence().take(5).joinToString("\n"), maxLines = 5)
            }
            SectionCard {
                ExportTitle("Compact text", "${"%,d".format(exports.encoded.length)} characters · ${inkSummary(signature)}")
                Code(exports.encoded, maxLines = 3)
            }
        }
    }
}

private class Exports(
    val transparent: ImageBitmap,
    val white: ImageBitmap,
    val transparentPng: ByteArray,
    val whitePng: ByteArray,
    val svg: String,
    val svgPaths: Int,
    val encoded: String,
    val width: Int,
    val height: Int,
) {
    companion object {
        fun of(signature: Signature, penConfig: PenConfig): Exports {
            val padding = 16f
            val transparent = signature.toImageBitmap(trimPadding = padding, penConfig = penConfig)
            val white = signature.toImageBitmap(background = Color.White, trimPadding = padding, penConfig = penConfig)
            val svg = signature.toSvg(SvgOptions(penConfig = penConfig, trimPadding = padding))
            return Exports(
                transparent = transparent,
                white = white,
                transparentPng = signature.toPngBytes(trimPadding = padding, penConfig = penConfig),
                whitePng = signature.toPngBytes(background = Color.White, trimPadding = padding, penConfig = penConfig),
                svg = svg,
                svgPaths = svg.lineSequence().count { it.startsWith("<path") },
                encoded = signature.encode(),
                width = transparent.width,
                height = transparent.height,
            )
        }
    }
}

@Composable
private fun ExportTitle(title: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(title, Modifier.weight(1f))
        Text(detail, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExportImage(bitmap: ImageBitmap) {
    Image(
        bitmap = bitmap,
        contentDescription = "Exported signature",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(bitmap.width.toFloat() / bitmap.height.coerceAtLeast(1)),
    )
}

@Composable
private fun Code(text: String, maxLines: Int) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(10.dp),
    )
}

/** The usual gray and white squares behind transparent images. */
@Composable
private fun Checkerboard(modifier: Modifier) {
    Canvas(modifier.background(Color.White)) {
        val cell = 8.dp.toPx()
        val light = Color(0xFFE6E8EC)
        var row = 0
        var y = 0f
        while (y < size.height) {
            var x = if (row % 2 == 0) 0f else cell
            while (x < size.width) {
                drawRect(light, Offset(x, y), Size(cell, cell))
                x += cell * 2
            }
            y += cell
            row++
        }
    }
}

private fun kilobytes(bytes: Int): String = "%.1f KB".format(bytes / 1024f)
