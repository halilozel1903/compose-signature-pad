package io.github.halilozel1903.signaturepad.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SvgExporterTest {

    private val corner = stroke(0f, 0f, 10f, 0f, 10f, 10f)

    @Test
    fun pathDataUsesQuadraticMidpoints() {
        assertEquals("M0 0 L5 0 Q10 0 10 5 L10 10", SvgExporter.pathData(corner))
    }

    @Test
    fun pathDataOfLongerStroke() {
        val s = stroke(0f, 0f, 10f, 0f, 20f, 10f, 30f, 10f)
        assertEquals("M0 0 L5 0 Q10 0 15 5 Q20 10 25 10 L30 10", SvgExporter.pathData(s))
    }

    @Test
    fun pathDataOfTwoPointsIsALineAndOfOnePointADot() {
        assertEquals("M1.5 2 L3.25 4", SvgExporter.pathData(stroke(1.5f, 2f, 3.25f, 4f)))
        assertEquals("M7 8 L7 8", SvgExporter.pathData(stroke(7f, 8f)))
    }

    @Test
    fun exportsUniformStrokeOnWhite() {
        val svg = Signature(listOf(corner), 20f, 20f).toSvg(
            SvgOptions(background = 0xFFFFFFFF.toInt(), variableWidth = false, penConfig = PenConfig.Uniform),
        )
        val expected = """
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20">
            <rect width="20" height="20" fill="#FFFFFF"/>
            <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M0 0 L5 0 Q10 0 10 5 L10 10" stroke="#1A1A2E" stroke-width="4"/>
            </g>
            </svg>
            
        """.trimIndent()
        assertEquals(expected, svg)
    }

    @Test
    fun variableWidthGroupsRunsOfEqualWidth() {
        val svg = SvgExporter.export(Signature(listOf(corner), 20f, 20f), SvgOptions(penConfig = PenConfig.Uniform))
        val expected = """
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20">
            <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <g stroke="#1A1A2E">
            <path d="M0 0 L5 0 Q10 0 10 5 L10 10" stroke-width="4"/>
            </g>
            </g>
            </svg>
            
        """.trimIndent()
        assertEquals(expected, svg)
    }

    @Test
    fun variableWidthSplitsWhereTheWidthChanges() {
        // Slow then fast: the stroke gets thinner, so the path splits into several runs.
        val points = listOf(
            StrokePoint(0f, 0f, timeMillis = 0),
            StrokePoint(2f, 0f, timeMillis = 100),
            StrokePoint(4f, 0f, timeMillis = 200),
            StrokePoint(104f, 0f, timeMillis = 210),
            StrokePoint(204f, 0f, timeMillis = 220),
        )
        val svg = Signature(listOf(Stroke(points, INK, 10f)), 220f, 20f).toSvg()
        assertTrue(svg.lines().count { it.startsWith("<path") } > 1, svg)
    }

    @Test
    fun widthStepRoundsWidths() {
        val s = stroke(0f, 0f, 10f, 0f, width = 4.3f)
        val sig = Signature(listOf(s), 20f, 20f)
        assertTrue("stroke-width=\"4.5\"" in sig.toSvg(SvgOptions(penConfig = PenConfig.Uniform)))
        assertTrue("stroke-width=\"4.25\"" in sig.toSvg(SvgOptions(penConfig = PenConfig.Uniform, widthStep = 0.25f)))
        assertTrue("stroke-width=\"4.3\"" in sig.toSvg(SvgOptions(penConfig = PenConfig.Uniform, widthStep = 0f)))
    }

    @Test
    fun eraserBecomesAMaskOverEarlierInk() {
        val pen = stroke(0f, 0f, 10f, 0f)
        val eraser = stroke(5f, 0f, 5f, 10f, width = 2f, kind = StrokeKind.Eraser)
        val svg = Signature(listOf(pen, eraser), 20f, 20f)
            .toSvg(SvgOptions(variableWidth = false, penConfig = PenConfig.Uniform))
        val expected = """
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20">
            <defs>
            <mask id="erase1" maskUnits="userSpaceOnUse" x="0" y="0" width="20" height="20">
            <rect width="20" height="20" fill="#FFFFFF"/>
            <path d="M5 0 L5 10" stroke="#000000" stroke-width="2"/>
            </mask>
            </defs>
            <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <g mask="url(#erase1)">
            <path d="M0 0 L10 0" stroke="#1A1A2E" stroke-width="4"/>
            </g>
            </g>
            </svg>
            
        """.trimIndent()
        assertEquals(expected, svg)
    }

    @Test
    fun eraserBeforeAnyInkIsDropped() {
        val eraser = stroke(5f, 0f, 5f, 10f, kind = StrokeKind.Eraser)
        val svg = Signature(listOf(eraser), 20f, 20f).toSvg()
        assertTrue("mask" !in svg)
    }

    @Test
    fun translucentColorsGetAnOpacity() {
        val s = stroke(0f, 0f, 4f, 4f, color = 0x801A1A2E.toInt())
        val svg = Signature(listOf(s), 10f, 10f).toSvg(SvgOptions(variableWidth = false, penConfig = PenConfig.Uniform))
        assertTrue("<path d=\"M0 0 L4 4\" stroke=\"#1A1A2E\" stroke-opacity=\"0.5\" stroke-width=\"4\"/>" in svg, svg)
    }

    @Test
    fun trimPaddingCropsTheCanvas() {
        val s = stroke(50f, 50f, 70f, 50f, width = 2f)
        val svg = Signature(listOf(s), 300f, 200f)
            .toSvg(SvgOptions(variableWidth = false, penConfig = PenConfig.Uniform, trimPadding = 4f))
        assertTrue(svg.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"30\" height=\"10\" viewBox=\"0 0 30 10\">"), svg)
        assertTrue("d=\"M5 5 L25 5\"" in svg, svg)
    }

    @Test
    fun formatsNumbersWithoutLocaleOrTrailingZeros() {
        assertEquals("3", formatNumber(3f))
        assertEquals("2.5", formatNumber(2.5f))
        assertEquals("0.05", formatNumber(0.05f))
        assertEquals("-1.25", formatNumber(-1.25f))
        assertEquals("0", formatNumber(-0.004f))
        assertEquals("12.35", formatNumber(12.349f))
    }
}
