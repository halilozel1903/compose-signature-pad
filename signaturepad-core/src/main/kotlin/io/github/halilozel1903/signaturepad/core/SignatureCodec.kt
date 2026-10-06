package io.github.halilozel1903.signaturepad.core

import kotlin.math.roundToLong

/**
 * A compact, JSON free text form of a [Signature], small enough for a database column, a saved state bundle or a
 * form field.
 *
 * ```
 * SP1:360x200|ff1a1a2e,6,PS:3c,2s,50,0;a,-2,52,8
 * ```
 *
 * - `SP1:<width>x<height>`: version and canvas size in pixels.
 * - One `|<color>,<width>,<kind><tool>:<points>` per stroke: ARGB color as 8 hex digits, base width, kind (`P` pen,
 *   `E` eraser) and tool (`F` finger, `S` stylus, `R` stylus eraser, `M` mouse, `U` unknown).
 * - Points separated by `;`, each `x,y,pressure,time` as base 36 integers: x and y in tenths of a pixel, pressure
 *   in hundredths, time in milliseconds. The first point is absolute; x, y and time of later points are the change
 *   from the previous point, pressure is always absolute.
 *
 * Positions are rounded to 0.1 px and pressure to 0.01. Every point of a stroke gets the stroke's tool when read
 * back, which is how the pads record them (one stroke per pointer).
 */
public object SignatureCodec {
    private const val HEADER = "SP1"

    /** Writes [signature] as text. */
    public fun encode(signature: Signature): String = buildString {
        append(HEADER).append(':')
        append(formatNumber(signature.width)).append('x').append(formatNumber(signature.height))
        for (stroke in signature.strokes) {
            append('|')
            append((stroke.color.toLong() and 0xFFFFFFFFL).toString(16).padStart(8, '0'))
            append(',').append(formatNumber(stroke.width))
            append(',').append(kindCode(stroke.kind)).append(toolCode(stroke.toolType))
            append(':')
            var previousX = 0L
            var previousY = 0L
            var previousTime = 0L
            stroke.points.forEachIndexed { index, point ->
                if (index > 0) append(';')
                val x = (point.x.toDouble() * 10).roundToLong()
                val y = (point.y.toDouble() * 10).roundToLong()
                val pressure = (point.pressure.coerceIn(0f, 1f).toDouble() * 100).roundToLong()
                append((x - previousX).toString(36)).append(',')
                append((y - previousY).toString(36)).append(',')
                append(pressure.toString(36)).append(',')
                append((point.timeMillis - previousTime).toString(36))
                previousX = x
                previousY = y
                previousTime = point.timeMillis
            }
        }
    }

    /** Reads text written by [encode]. Throws [IllegalArgumentException] when [text] is not in that form. */
    public fun decode(text: String): Signature {
        val parts = text.trim().split('|')
        val header = parts[0]
        require(header.startsWith("$HEADER:")) { "Not a signature: expected the $HEADER header" }
        val size = header.removePrefix("$HEADER:").split('x')
        require(size.size == 2) { "Bad canvas size in \"$header\"" }
        val width = size[0].toFloatOrNull() ?: throw IllegalArgumentException("Bad canvas width in \"$header\"")
        val height = size[1].toFloatOrNull() ?: throw IllegalArgumentException("Bad canvas height in \"$header\"")
        val strokes = parts.drop(1).map(::decodeStroke)
        return Signature(strokes, width, height)
    }

    private fun decodeStroke(text: String): Stroke {
        val colon = text.indexOf(':')
        require(colon > 0) { "Bad stroke \"${text.take(32)}\"" }
        val head = text.substring(0, colon).split(',')
        require(head.size == 3 && head[2].length == 2) { "Bad stroke header \"${text.substring(0, colon)}\"" }
        val color = head[0].toLongOrNull(16)?.toInt() ?: throw IllegalArgumentException("Bad color \"${head[0]}\"")
        val width = head[1].toFloatOrNull() ?: throw IllegalArgumentException("Bad width \"${head[1]}\"")
        val kind = kindFrom(head[2][0])
        val tool = toolFrom(head[2][1])
        val body = text.substring(colon + 1)
        var x = 0L
        var y = 0L
        var time = 0L
        val points = if (body.isEmpty()) {
            emptyList()
        } else {
            body.split(';').map { pointText ->
                val fields = pointText.split(',')
                require(fields.size == 4) { "Bad point \"$pointText\"" }
                val values = fields.map {
                    it.toLongOrNull(36) ?: throw IllegalArgumentException("Bad number \"$it\" in point \"$pointText\"")
                }
                x += values[0]
                y += values[1]
                time += values[3]
                StrokePoint(x / 10f, y / 10f, values[2] / 100f, time, tool)
            }
        }
        return Stroke(points, color, width, kind)
    }

    private fun kindCode(kind: StrokeKind): Char = when (kind) {
        StrokeKind.Pen -> 'P'
        StrokeKind.Eraser -> 'E'
    }

    private fun kindFrom(code: Char): StrokeKind = when (code) {
        'P' -> StrokeKind.Pen
        'E' -> StrokeKind.Eraser
        else -> throw IllegalArgumentException("Bad stroke kind '$code'")
    }

    private fun toolCode(tool: ToolType): Char = when (tool) {
        ToolType.Finger -> 'F'
        ToolType.Stylus -> 'S'
        ToolType.StylusEraser -> 'R'
        ToolType.Mouse -> 'M'
        ToolType.Unknown -> 'U'
    }

    private fun toolFrom(code: Char): ToolType = when (code) {
        'F' -> ToolType.Finger
        'S' -> ToolType.Stylus
        'R' -> ToolType.StylusEraser
        'M' -> ToolType.Mouse
        'U' -> ToolType.Unknown
        else -> throw IllegalArgumentException("Bad tool '$code'")
    }
}
