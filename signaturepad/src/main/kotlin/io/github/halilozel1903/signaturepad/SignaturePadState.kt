package io.github.halilozel1903.signaturepad

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.signaturepad.core.PenConfig
import io.github.halilozel1903.signaturepad.core.Signature
import io.github.halilozel1903.signaturepad.core.SignatureCodec
import io.github.halilozel1903.signaturepad.core.Stroke
import io.github.halilozel1903.signaturepad.core.StrokeHistory
import io.github.halilozel1903.signaturepad.core.StrokeHitTest
import io.github.halilozel1903.signaturepad.core.StrokeKind
import io.github.halilozel1903.signaturepad.core.StrokePoint
import io.github.halilozel1903.signaturepad.core.SvgOptions
import io.github.halilozel1903.signaturepad.core.ToolType
import java.io.ByteArrayOutputStream

/** What a pointer does on a pad. */
public enum class DrawingTool {
    /** Draws ink with the pen color and width. */
    Pen,

    /** Rubs out ink under it, like a real eraser. */
    Eraser,

    /** Removes every stroke it touches, whole. */
    StrokeEraser,
}

/**
 * The strokes, pen settings and undo history of a [SignaturePad] or [SketchPad]. Create it with
 * [rememberSignaturePadState]; it survives configuration changes and process death.
 *
 * Stroke coordinates are pixels of the pad's canvas, see [canvasSize].
 */
@Stable
public class SignaturePadState(
    initialStrokes: List<Stroke> = emptyList(),
    penColor: Color = SignaturePadDefaults.InkColor,
    penWidth: Dp = SignaturePadDefaults.PenWidth,
    penConfig: PenConfig = PenConfig.Default,
    maxUndoSteps: Int = StrokeHistory.DEFAULT_MAX_STEPS,
) {
    /** The color of new strokes. */
    public var penColor: Color by mutableStateOf(penColor)

    /** The base width of new strokes. The drawn width varies around it with speed and pressure. */
    public var penWidth: Dp by mutableStateOf(penWidth)

    /** How speed and pressure change the width, for drawing and export. */
    public var penConfig: PenConfig by mutableStateOf(penConfig)

    /** The tool for fingers, mice and the stylus tip. */
    public var tool: DrawingTool by mutableStateOf(DrawingTool.Pen)

    /** The tool for the eraser end of a stylus, or null to draw with [tool] instead. */
    public var stylusEraserTool: DrawingTool? by mutableStateOf(DrawingTool.Eraser)

    /** The width of [DrawingTool.Eraser] and the reach of [DrawingTool.StrokeEraser]. */
    public var eraserWidth: Dp by mutableStateOf(SignaturePadDefaults.EraserWidth)

    /** The strokes with their undo and redo steps. */
    public var history: StrokeHistory by mutableStateOf(StrokeHistory(initialStrokes, maxUndoSteps))
        private set

    /** The size of the pad's canvas in pixels; zero until the pad is laid out. */
    public var canvasSize: IntSize by mutableStateOf(IntSize.Zero)
        private set

    // Strokes left while a stroke eraser gesture is in progress; committed as one undo step on lift.
    private var erasing: List<Stroke>? by mutableStateOf(null)

    internal val activePoints = mutableStateListOf<StrokePoint>()
    private var activeColor = 0
    private var activeWidth = 1f
    private var activeKind = StrokeKind.Pen
    private var pendingLoad: Pair<Signature, Float>? = null

    /** Where the eraser is while erasing, for drawing its outline. */
    internal var eraserCursor: Offset? by mutableStateOf(null)
    internal var eraserCursorRadius: Float = 0f

    /** The finished strokes, oldest first. */
    public val strokes: List<Stroke> get() = erasing ?: history.strokes

    /** True when there is no ink on the pad, counting the stroke being drawn. */
    public val isEmpty: Boolean
        get() = activePoints.isEmpty() && strokes.none { it.kind == StrokeKind.Pen && !it.isEmpty }

    /** True while a pointer is drawing or erasing. */
    public val isDrawing: Boolean get() = activePoints.isNotEmpty() || erasing != null

    public val canUndo: Boolean get() = history.canUndo
    public val canRedo: Boolean get() = history.canRedo

    /** Undoes the last stroke, erase or clear. */
    public fun undo() {
        history = history.undo()
    }

    /** Redoes the last undone step. */
    public fun redo() {
        history = history.redo()
    }

    /** Removes every stroke; [undo] brings them back. */
    public fun clear() {
        cancelStroke()
        history = history.clear()
    }

    /** Adds [stroke] on top as an undoable step. */
    public fun addStroke(stroke: Stroke) {
        history = history.add(stroke)
    }

    /**
     * Replaces the pad's content with [signature] and forgets the undo steps. With [fitToCanvas] the ink is scaled
     * and centered to fit the pad with [padding] pixels around it (as soon as the pad is laid out); otherwise the
     * strokes are used as they are.
     */
    public fun load(signature: Signature, fitToCanvas: Boolean = true, padding: Float = 0f) {
        cancelStroke()
        if (!fitToCanvas) {
            pendingLoad = null
            history = StrokeHistory(signature.strokes, history.maxSteps)
            return
        }
        pendingLoad = signature to padding
        applyPendingLoad()
    }

    /** Replaces the content with a text form written by [encode], see [load]. */
    public fun load(encoded: String, fitToCanvas: Boolean = false) {
        load(SignatureCodec.decode(encoded), fitToCanvas)
    }

    /** The strokes on a canvas of [canvasSize]. */
    public fun toSignature(): Signature =
        Signature(strokes, canvasSize.width.toFloat(), canvasSize.height.toFloat())

    /** The compact text form of [toSignature], see [SignatureCodec]. */
    public fun encode(): String = SignatureCodec.encode(toSignature())

    /** An SVG document of the pad, drawn with the pad's [penConfig] unless [options] says otherwise. */
    public fun toSvg(options: SvgOptions = SvgOptions(penConfig = penConfig)): String = toSignature().toSvg(options)

    /**
     * The pad as an image. [background] is transparent by default; pass [Color.White] for documents. With
     * [trimPadding] the image is cropped to the ink plus that many pixels.
     */
    public fun toImageBitmap(
        background: Color = Color.Transparent,
        trimPadding: Float? = null,
        scale: Float = 1f,
    ): ImageBitmap = toSignature().toImageBitmap(background, trimPadding, scale, penConfig)

    /** [toImageBitmap] as an Android [Bitmap]. */
    public fun toBitmap(
        background: Color = Color.Transparent,
        trimPadding: Float? = null,
        scale: Float = 1f,
    ): Bitmap = toImageBitmap(background, trimPadding, scale).asAndroidBitmap()

    /** [toImageBitmap] encoded as PNG. */
    public fun toPngBytes(
        background: Color = Color.Transparent,
        trimPadding: Float? = null,
        scale: Float = 1f,
    ): ByteArray = toBitmap(background, trimPadding, scale).toPngBytes()

    internal fun onCanvasSize(size: IntSize) {
        if (size == canvasSize) return
        canvasSize = size
        applyPendingLoad()
    }

    private fun applyPendingLoad() {
        val (signature, padding) = pendingLoad ?: return
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return
        pendingLoad = null
        val fitted = if (signature.isEmpty) {
            signature
        } else {
            signature.fitInto(canvasSize.width.toFloat(), canvasSize.height.toFloat(), padding, config = penConfig)
        }
        history = StrokeHistory(fitted.strokes, history.maxSteps)
    }

    internal fun effectiveTool(toolType: ToolType): DrawingTool =
        if (toolType == ToolType.StylusEraser) stylusEraserTool ?: tool else tool

    internal fun startStroke(point: StrokePoint, density: Density) {
        cancelStroke()
        when (effectiveTool(point.toolType)) {
            DrawingTool.Pen -> {
                activeKind = StrokeKind.Pen
                activeColor = penColor.toArgb()
                activeWidth = with(density) { penWidth.toPx() }.coerceAtLeast(0.5f)
                activePoints += point
            }
            DrawingTool.Eraser -> {
                activeKind = StrokeKind.Eraser
                activeColor = Color.Black.toArgb()
                activeWidth = with(density) { eraserWidth.toPx() }.coerceAtLeast(1f)
                eraserCursorRadius = activeWidth / 2f
                eraserCursor = Offset(point.x, point.y)
                activePoints += point
            }
            DrawingTool.StrokeEraser -> {
                eraserCursorRadius = with(density) { eraserWidth.toPx() } / 2f
                erasing = history.strokes
                eraseAt(point)
            }
        }
    }

    internal fun extendStroke(point: StrokePoint) {
        if (erasing != null) {
            eraseAt(point)
            return
        }
        if (activePoints.isEmpty()) return
        val last = activePoints.last()
        if (last.x == point.x && last.y == point.y) return
        activePoints += point
        if (activeKind == StrokeKind.Eraser) eraserCursor = Offset(point.x, point.y)
    }

    internal fun lastPressure(): Float = activePoints.lastOrNull()?.pressure ?: 1f

    /** Commits the stroke in progress and returns it, or null when nothing was drawn. */
    internal fun finishStroke(): Stroke? {
        erasing?.let { remaining ->
            erasing = null
            eraserCursor = null
            history = history.replaceAll(remaining)
            return null
        }
        val stroke = activeStroke() ?: return null
        activePoints.clear()
        eraserCursor = null
        history = history.add(stroke)
        return stroke
    }

    /** Drops the stroke in progress, for example when palm rejection decides it was a palm. */
    internal fun cancelStroke() {
        activePoints.clear()
        erasing = null
        eraserCursor = null
    }

    internal fun activeStroke(): Stroke? =
        if (activePoints.isEmpty()) null else Stroke(activePoints.toList(), activeColor, activeWidth, activeKind)

    private fun eraseAt(point: StrokePoint) {
        val current = erasing ?: return
        eraserCursor = Offset(point.x, point.y)
        val remaining = StrokeHitTest.eraseAt(current, point.x, point.y, eraserCursorRadius, penConfig)
        if (remaining.size != current.size) erasing = remaining
    }

    public companion object {
        /**
         * A [Saver] keeping the strokes (in the compact text form, so positions are rounded to 0.1 px), pen color,
         * pen width, eraser width and tool. Undo steps are not saved.
         */
        public fun saver(
            penConfig: PenConfig = PenConfig.Default,
            maxUndoSteps: Int = StrokeHistory.DEFAULT_MAX_STEPS,
        ): Saver<SignaturePadState, Any> = listSaver<SignaturePadState, Any>(
            save = { state ->
                listOf<Any>(
                    SignatureCodec.encode(Signature(state.history.strokes, 0f, 0f)),
                    state.penColor.toArgb(),
                    state.penWidth.value,
                    state.eraserWidth.value,
                    state.tool.name,
                )
            },
            restore = { saved ->
                SignaturePadState(
                    initialStrokes = SignatureCodec.decode(saved[0] as String).strokes,
                    penColor = Color(saved[1] as Int),
                    penWidth = (saved[2] as Float).dp,
                    penConfig = penConfig,
                    maxUndoSteps = maxUndoSteps,
                ).apply {
                    eraserWidth = (saved[3] as Float).dp
                    tool = DrawingTool.valueOf(saved[4] as String)
                }
            },
        )
    }
}

/**
 * Creates and remembers a [SignaturePadState] that survives configuration changes and process death.
 * The arguments are only used the first time.
 */
@Composable
public fun rememberSignaturePadState(
    penColor: Color = SignaturePadDefaults.InkColor,
    penWidth: Dp = SignaturePadDefaults.PenWidth,
    penConfig: PenConfig = PenConfig.Default,
    maxUndoSteps: Int = StrokeHistory.DEFAULT_MAX_STEPS,
): SignaturePadState = rememberSaveable(saver = SignaturePadState.saver(penConfig, maxUndoSteps)) {
    SignaturePadState(
        penColor = penColor,
        penWidth = penWidth,
        penConfig = penConfig,
        maxUndoSteps = maxUndoSteps,
    )
}

internal fun Bitmap.toPngBytes(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
