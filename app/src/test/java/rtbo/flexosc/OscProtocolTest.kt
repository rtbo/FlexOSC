package rtbo.flexosc

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import rtbo.flexosc.model.OscAtomic
import rtbo.flexosc.model.OscMessage
import rtbo.flexosc.model.OscString
import rtbo.flexosc.model.toOsc
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OscProtocolTest {
    @Test
    fun stringTagIsCorrect() {
        assertEquals(Pair('s', 4), OscString("/b").tagLen)
        assertEquals(Pair('s', 8), OscString("/aba").tagLen)
    }

    @Test
    fun encodeStringsCorrectly() {
        assertArrayEquals(
            byteArrayOf('/'.toByte(), 'b'.toByte(), 0, 0),
            encode(OscString("/b"))
        )
        assertArrayEquals(
            byteArrayOf('/'.toByte(), 'a'.toByte(), 'b'.toByte(), 'a'.toByte(), 0, 0, 0, 0),
            encode(OscString("/aba"))
        )
    }

    @Test
    fun decodeStringsCorrectly() {
        assertEquals(
            OscString("/b"),
            decode('s', byteArrayOf('/'.toByte(), 'b'.toByte(), 0, 0))
        )
        assertEquals(
            OscString("/aba"),
            decode(
                's',
                byteArrayOf('/'.toByte(), 'a'.toByte(), 'b'.toByte(), 'a'.toByte(), 0, 0, 0, 0)
            )
        )
    }

    @Test
    fun printMessageToString() {
        assertEquals(
            "OscMessage(/some/address)", OscMessage("/some/address").toString()
        )
        assertEquals(
            "OscMessage(/some/address i:42 f:53.4 s:\"a string argument\" b:[7 bytes])",
            OscMessage(
                "/some/address",
                arrayOf<OscAtomic>(
                    42.toOsc(),
                    53.4f.toOsc(),
                    "a string argument".toOsc(),
                    byteArrayOf(1, 2, 3, 4, 5, 6, 7).toOsc()
                )
            ).toString()
        )
    }

    private fun encode(atomic: OscAtomic): ByteArray {
        val (_, sz) = atomic.tagLen
        val buf = ByteBuffer.allocate(sz)
        buf.order(ByteOrder.BIG_ENDIAN)
        atomic.encode(buf)
        return buf.array()
    }

    private fun decode(typeTag: Char, arr: ByteArray): OscAtomic {
        val buf = ByteBuffer.wrap(arr)
        buf.order(ByteOrder.BIG_ENDIAN)
        val res = OscAtomic.decode(typeTag, buf)
        assertEquals(arr.size, buf.position())
        return res
    }
}