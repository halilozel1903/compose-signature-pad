package io.github.halilozel1903.signaturepad.core

/**
 * Options for [SvgExporter].
 *
 * @property background the background as ARGB, or null for a transparent background.
 * @property variableWidth true draws each stroke with the widths the pad draws (speed and pressure); false draws each
 *   stroke as a single path at its average width, which is smaller and easier to edit.
 * @property penConfig the width model, normally the one the pad used.
 * @property simplifyTolerance when above 0, strokes are simplified with [Simplification] first (in pixels).
 * @property trimPadding when not null, the canvas is cropped to the ink plus this padding, see [Signature.trim].
 * @property widthStep with [variableWidth], widths are rounded to multiples of this (in pixels) and consecutive
 *   pieces of a stroke with the same width share one path. Smaller steps follow the pen more closely and make larger
 *   files; 0 keeps two decimals.
 */
public data class SvgOptions(
    val background: Int? = null,
    val variableWidth: Boolean = true,
    val penConfig: PenConfig = PenConfig.Default,
    val simplifyTolerance: Float = 0f,
    val trimPadding: Float? = null,
    val widthStep: Float = 0.5f,
) {
    init {
        require(widthStep >= 0f) { "widthStep must not be negative, was $widthStep" }
    }
}

/**
 * Exports a [Signature] as an SVG document. Strokes are smoothed exactly like on screen (quadratic Bezier midpoints,
 * see [StrokeSmoothing]) and drawn with round caps and joins. Eraser strokes become masks over the ink drawn before
 * them, so the result matches the pad even with a transparent background.
 *
 * Numbers have at most two decimals and do not depend on the locale, so the output is stable for tests and diffs.
 */
public object SvgExporter {

    /** The SVG document for [signature]. */
    public fun export(signature: Signature, options: SvgOptions = SvgOptions()): String {
        var source = signature
        if (options.simplifyTolerance > 0f) source = source.simplify(options.simplifyTolerance)
        options.trimPadding?.let { source = source.trim(it, options.penConfig) }
        val width = formatNumber(source.width)
        val height = formatNumber(source.height)

        val masks = StringBuilder()
        var content = StringBuilder()
        var maskCount = 0
        for (stroke in source.strokes) {
            if (stroke.isEmpty) continue
            when (stroke.kind) {
                StrokeKind.Pen -> content.append(strokeElement(stroke, options, colorAttributes(stroke.color)))
                StrokeKind.Eraser -> {
                    if (content.isEmpty()) continue
                    maskCount++
                    val id = "erase$maskCount"
                    masks.append("<mask id=\"").append(id)
                        .append("\" maskUnits=\"userSpaceOnUse\" x=\"0\" y=\"0\" width=\"").append(width)
                        .append("\" height=\"").append(height).append("\">\n")
                        .append("<rect width=\"").append(width).append("\" height=\"").append(height)
                        .append("\" fill=\"#FFFFFF\"/>\n")
                        .append(strokeElement(stroke, options, " stroke=\"#000000\""))
                        .append("</mask>\n")
                    content = StringBuilder("<g mask=\"url(#").append(id).append(")\">\n").append(content).append("</g>\n")
                }
            }
        }

        return buildString {
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width)
            append("\" height=\"").append(height)
            append("\" viewBox=\"0 0 ").append(width).append(' ').append(height).append("\">\n")
            if (masks.isNotEmpty()) append("<defs>\n").append(masks).append("</defs>\n")
            options.background?.let { background ->
                append("<rect width=\"").append(width).append("\" height=\"").append(height)
                append("\" fill=\"").append(hexRgb(background)).append('"')
                if (alphaOf(background) < 1f) append(" fill-opacity=\"").append(formatNumber(alphaOf(background))).append('"')
                append("/>\n")
            }
            append("<g fill=\"none\" stroke-linecap=\"round\" stroke-linejoin=\"round\">\n")
            append(content)
            append("</g>\n")
            append("</svg>\n")
        }
    }

    /**
     * The path data of [stroke] smoothed, without widths: `M x y L x y Q cx cy x y ... L x y`. A single point gives
     * a zero length line, which SVG draws as a dot with round caps.
     */
    public fun pathData(stroke: Stroke): String = pathData(StrokeSmoothing.segments(stroke, PenConfig.Uniform))

    /** The path data of [segments], as in [pathData]. */
    public fun pathData(segments: List<PathSegment>): String = buildString {
        if (segments.isEmpty()) return@buildString
        appendMove(segments.first().start)
        for (segment in segments) appendSegment(segment)
    }

    private fun strokeElement(stroke: Stroke, options: SvgOptions, colorAttributes: String): String {
        val segments = StrokeSmoothing.segments(stroke, options.penConfig)
        if (!options.variableWidth) {
            val widths = StrokeWidths.compute(stroke, options.penConfig)
            val average = widths.average().toFloat()
            return "<path d=\"${pathData(segments)}\"$colorAttributes stroke-width=\"${formatNumber(average)}\"/>\n"
        }
        // One path per run of segments with the same rounded width: smaller files, same look.
        val out = StringBuilder()
        out.append("<g").append(colorAttributes).append(">\n")
        var runWidth: String? = null
        val run = StringBuilder()
        fun flush() {
            if (runWidth != null) out.append("<path d=\"").append(run).append("\" stroke-width=\"").append(runWidth).append("\"/>\n")
            run.setLength(0)
            runWidth = null
        }
        for (segment in segments) {
            val width = formatNumber(quantize((segment.startWidth + segment.endWidth) / 2f, options.widthStep))
            if (width != runWidth) {
                flush()
                runWidth = width
                run.appendMove(segment.start)
            }
            run.appendSegment(segment)
        }
        flush()
        out.append("</g>\n")
        return out.toString()
    }

    private fun quantize(value: Float, step: Float): Float =
        if (step <= 0f) value else (kotlin.math.round(value / step) * step).coerceAtLeast(step)

    private fun colorAttributes(color: Int): String {
        val alpha = alphaOf(color)
        val opacity = if (alpha < 1f) " stroke-opacity=\"${formatNumber(alpha)}\"" else ""
        return " stroke=\"${hexRgb(color)}\"$opacity"
    }

    private fun StringBuilder.appendMove(point: Point) {
        append('M').append(formatNumber(point.x)).append(' ').append(formatNumber(point.y))
    }

    private fun StringBuilder.appendSegment(segment: PathSegment) {
        when (segment) {
            is LineSegment -> append(" L").append(formatNumber(segment.end.x)).append(' ').append(formatNumber(segment.end.y))
            is QuadSegment -> append(" Q").append(formatNumber(segment.control.x)).append(' ')
                .append(formatNumber(segment.control.y)).append(' ')
                .append(formatNumber(segment.end.x)).append(' ').append(formatNumber(segment.end.y))
        }
    }
}
