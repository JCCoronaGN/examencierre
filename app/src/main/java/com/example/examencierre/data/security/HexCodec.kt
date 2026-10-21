package com.example.examencierre.data.security

/**
 * Codificación hexadecimal de bytes, usada para guardar el salt y el hash de las
 * contraseñas como texto en la base de datos.
 */
object HexCodec {

    private const val HEX_CHARS = "0123456789abcdef"

    fun encode(data: ByteArray): String {
        val builder = StringBuilder(data.size * 2)
        for (byte in data) {
            val value = byte.toInt() and 0xFF
            builder.append(HEX_CHARS[value ushr 4])
            builder.append(HEX_CHARS[value and 0x0F])
        }
        return builder.toString()
    }

    fun decode(data: String): ByteArray {
        require(data.length % 2 == 0) { "La cadena hexadecimal debe tener longitud par" }
        val result = ByteArray(data.length / 2)
        for (index in result.indices) {
            val high = Character.digit(data[index * 2], 16)
            val low = Character.digit(data[index * 2 + 1], 16)
            require(high >= 0 && low >= 0) { "La cadena contiene caracteres no válidos" }
            result[index] = ((high shl 4) or low).toByte()
        }
        return result
    }
}
