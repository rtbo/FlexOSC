package org.rtbo.flexosc.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

private fun oscStringLen(value: String) = value.length + 1 + (value.length + 1) % 4
private fun oscBlobLen(value: ByteArray): Int {
    if ((value.size % 4) != 0) {
        throw Exception("Blob is not 4 bytes aligned!")
    }
    return value.size
}

private fun oscStringPayload(value: String): ByteArray {
    for (c in value) {
        if (c.toInt() > 127) {
            throw Exception("\"${value}\" is not ASCII!")
        }
    }
    val numNulls = 1 + (value.length + 1) % 4
    return value.toByteArray(Charsets.US_ASCII) + ByteArray(numNulls)
}

private fun oscAtomicTagLen(arg: Any): Pair<Char, Int> {
    return when (arg) {
        is Int -> Pair('i', 4)
        is Float -> Pair('f', 4)
        is String -> Pair('s', oscStringLen(arg))
        is ByteArray -> Pair('b', oscBlobLen(arg))
        else -> throw Exception("Unsupported atomic type: ${arg.javaClass.name}")
    }
}

private fun bufferOscAtomic(arg: Any, buffer: ByteBuffer) {
    when (arg) {
        is Int -> buffer.putInt(arg)
        is Float -> buffer.putFloat(arg)
        is String -> buffer.put(oscStringPayload(arg))
        is ByteArray -> buffer.put(arg)
        else -> throw Exception("Unsupported atomic type: ${arg.javaClass.name}")
    }
}

data class OscMessage(val address: String, val args: Array<Any> = emptyArray()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OscMessage

        if (address != other.address) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + args.contentHashCode()
        return result
    }

    val payload: ByteArray
        get() {
            var msgSz = oscStringLen(address)
            var argsTypeTags = ","
            for (arg in args) {
                val (tag, len) = oscAtomicTagLen(arg)
                argsTypeTags += tag
                msgSz += len
            }
            msgSz += oscStringLen(argsTypeTags)

            val buffer = ByteBuffer.allocate(msgSz)
            buffer.order(ByteOrder.BIG_ENDIAN)
            bufferOscAtomic(address, buffer)
            bufferOscAtomic(argsTypeTags, buffer)
            for (arg in args) {
                bufferOscAtomic(arg, buffer)
            }

            return buffer.array()
        }

}
