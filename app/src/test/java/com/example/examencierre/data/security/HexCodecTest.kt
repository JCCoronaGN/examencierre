package com.example.examencierre.data.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class HexCodecTest {

    @Test
    fun `encode produces the expected representation`() {
        val data = byteArrayOf(0x00, 0x0F, 0x10, 0xFF.toByte())

        assertEquals("000f10ff", HexCodec.encode(data))
    }

    @Test
    fun `decode restores the original bytes`() {
        val data = byteArrayOf(0x00, 0x0F, 0x10, 0xFF.toByte())

        assertArrayEquals(data, HexCodec.decode(HexCodec.encode(data)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decode rejects an odd length string`() {
        HexCodec.decode("abc")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decode rejects invalid characters`() {
        HexCodec.decode("zz")
    }
}
