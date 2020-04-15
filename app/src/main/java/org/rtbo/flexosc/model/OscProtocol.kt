package org.rtbo.flexosc.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class OscAtomic {
    abstract val tagLen: Pair<Char, Int>
    abstract fun bufferUp(buffer: ByteBuffer)
}

data class OscString(val value: String) : OscAtomic() {
    init {
        for (c in value) {
            if (c.toInt() > 127) {
                throw Exception("\"${value}\" is not ASCII!")
            }
        }
    }

    override val tagLen = Pair('s', value.length + 1 + (value.length + 1) % 4)
    override fun bufferUp(buffer: ByteBuffer) {
        var numNulls = 1 + (value.length + 1) % 4
        buffer.put(value.toByteArray(Charsets.US_ASCII))
        while ((numNulls--) > 0) {
            buffer.put(0.toByte())
        }
    }
}

data class OscInt(val value: Int) : OscAtomic() {
    override val tagLen = Pair('i', 4)
    override fun bufferUp(buffer: ByteBuffer) {
        buffer.putInt(value)
    }
}

data class OscFloat(val value: Float) : OscAtomic() {
    override val tagLen = Pair('f', 4)
    override fun bufferUp(buffer: ByteBuffer) {
        buffer.putFloat(value)
    }
}

data class OscBlob(val value: ByteArray) : OscAtomic() {
    init {
        if ((value.size % 4) != 0) {
            throw Exception("Blob is not 4 bytes aligned!")
        }
    }

    override val tagLen = Pair('b', value.size)

    override fun bufferUp(buffer: ByteBuffer) {
        buffer.put(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OscBlob

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}


data class OscMessage(val address: OscString, val args: Array<OscAtomic> = emptyArray()) {

    constructor(address: String, args: Array<OscAtomic> = emptyArray()) : this(
        OscString(address),
        args
    )

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
}

fun oscMessageToPacket(msg: OscMessage): ByteArray {
    var (_, msgSz) = msg.address.tagLen
    var argsTypeTags = ","
    for (arg in msg.args) {
        val (tag, len) = arg.tagLen
        argsTypeTags += tag
        msgSz += len
    }
    val attAtomic = OscString(argsTypeTags)
    msgSz += attAtomic.tagLen.second

    val buffer = ByteBuffer.allocate(msgSz)
    buffer.order(ByteOrder.BIG_ENDIAN)
    msg.address.bufferUp(buffer)
    attAtomic.bufferUp(buffer)
    msg.args.forEach { it.bufferUp(buffer) }

    return buffer.array()
}
