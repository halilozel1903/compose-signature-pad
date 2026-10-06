package io.github.halilozel1903.signaturepad.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CodecTest {

    private val signature = Signature(
        strokes = listOf(
            Stroke(
                points = listOf(
                    StrokePoint(12.3f, 4.5f, 0.5f, 1000, ToolType.Stylus),
                    StrokePoint(13.3f, 4f, 0.52f, 1008, ToolType.Stylus),
                ),
                color = INK,
                width = 6f,
            ),
        ),
        width = 300f,
        height = 120f,
    )

    @Test
    fun encodesCompactly() {
        assertEquals("SP1:300x120|ff1a1a2e,6,PS:3f,19,1e,rs;a,-5,1g,8", signature.encode())
    }

    @Test
    fun roundTrips() {
        assertEquals(signature, Signature.decode(signature.encode()))
    }

    @Test
    fun roundTripsErasersEmptyStrokesAndTranslucentColors() {
        val original = Signature(
            listOf(
                stroke(1.25f, 2f, 30f, 40f, width = 3.5f, color = 0x40FF0000),
                Stroke(emptyList(), INK, 2f),
                stroke(5f, 5f, 6f, 6f, width = 12f, kind = StrokeKind.Eraser),
            ),
            width = 411.5f,
            height = 200f,
        )
        val decoded = Signature.decode(original.encode())
        assertEquals(3, decoded.strokes.size)
        assertEquals(0x40FF0000, decoded.strokes[0].color)
        assertEquals(3.5f, decoded.strokes[0].width)
        assertEquals(1.3f, decoded.strokes[0].points[0].x) // rounded to a tenth of a pixel
        assertTrue(decoded.strokes[1].isEmpty)
        assertEquals(StrokeKind.Eraser, decoded.strokes[2].kind)
        assertEquals(411.5f, decoded.width)
    }

    @Test
    fun emptySignature() {
        assertEquals("SP1:0x0", Signature.Empty.encode())
        assertEquals(Signature.Empty, Signature.decode("SP1:0x0"))
    }

    @Test
    fun rejectsGarbage() {
        assertFailsWith<IllegalArgumentException> { Signature.decode("{\"strokes\":[]}") }
        assertFailsWith<IllegalArgumentException> { Signature.decode("SP1:10x10|ff000000,2,PX:0,0,0,0") }
        assertFailsWith<IllegalArgumentException> { Signature.decode("SP1:10x10|ff000000,2,PF:0,0,0") }
        assertFailsWith<IllegalArgumentException> { Signature.decode("SP1:10") }
    }
}
