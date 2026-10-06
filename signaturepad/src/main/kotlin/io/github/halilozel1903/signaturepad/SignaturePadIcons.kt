package io.github.halilozel1903.signaturepad

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The toolbar icons, as vectors, so the library needs no icon dependency. Paths follow the Material Symbols
 * (Apache 2.0) shapes.
 */
public object SignaturePadIcons {
    public val Undo: ImageVector by lazy {
        icon(
            "Undo",
            "M12.5,8c-2.65,0 -5.05,0.99 -6.9,2.6L2,7v9h9l-3.62,-3.62c1.39,-1.16 3.16,-1.88 5.12,-1.88 " +
                "3.54,0 6.55,2.31 7.6,5.5l2.37,-0.78C21.08,11.03 17.15,8 12.5,8z",
        )
    }

    public val Redo: ImageVector by lazy {
        icon(
            "Redo",
            "M18.4,10.6C16.55,8.99 14.15,8 11.5,8c-4.65,0 -8.58,3.03 -9.96,7.22L3.9,16c1.05,-3.19 4.05,-5.5 " +
                "7.6,-5.5 1.95,0 3.73,0.72 5.12,1.88L13,16h9V7l-3.6,3.6z",
        )
    }

    public val Clear: ImageVector by lazy {
        icon("Clear", "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z")
    }

    public val Pen: ImageVector by lazy {
        icon(
            "Pen",
            "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41" +
                "l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z",
        )
    }

    public val Eraser: ImageVector by lazy {
        icon(
            "Eraser",
            "M15.14,3c-0.51,0 -1.02,0.2 -1.41,0.59L2.59,14.73c-0.78,0.78 -0.78,2.05 0,2.83L5.03,20h7.66" +
                "l8.72,-8.72c0.78,-0.78 0.78,-2.05 0,-2.83l-4.86,-4.86C16.16,3.2 15.65,3 15.14,3z" +
                "M6.04,18l-2.03,-2.03 5.03,-5.03 4.86,4.86L11.86,18H6.04zM14,20h8v2h-8z",
        )
    }

    public val StrokeEraser: ImageVector by lazy {
        ImageVector.Builder(
            name = "Stroke eraser",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString("M3,19C5,12 8,8 10.5,10.5S11,17 14,16.5 17.5,12 19,10.5").toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ).addPath(
            pathData = PathParser().parsePathString("M15,3L21,9M21,3L15,9").toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
        ).build()
    }

    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            pathFillType = PathFillType.EvenOdd,
            fill = SolidColor(Color.Black),
        ).build()
}
