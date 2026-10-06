package io.github.halilozel1903.signaturepad.core

internal const val INK: Int = 0xFF1A1A2E.toInt()

internal fun points(vararg xy: Float, toolType: ToolType = ToolType.Finger, stepMillis: Long = 10L): List<StrokePoint> =
    xy.toList().chunked(2).mapIndexed { index, (x, y) ->
        StrokePoint(x, y, pressure = 1f, timeMillis = index * stepMillis, toolType = toolType)
    }

internal fun stroke(vararg xy: Float, width: Float = 4f, kind: StrokeKind = StrokeKind.Pen, color: Int = INK): Stroke =
    Stroke(points(*xy), color, width, kind)
