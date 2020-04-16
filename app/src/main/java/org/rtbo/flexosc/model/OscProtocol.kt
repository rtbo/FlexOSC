package org.rtbo.flexosc.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class OscAtomic {
    abstract val tagLen: Pair<Char, Int>
    abstract fun encode(buffer: ByteBuffer)
}

data class OscInt(val value: Int) : OscAtomic() {
    override val tagLen = Pair('i', 4)
    override fun encode(buffer: ByteBuffer) {
        buffer.putInt(value)
    }
}

data class OscFloat(val value: Float) : OscAtomic() {
    override val tagLen = Pair('f', 4)
    override fun encode(buffer: ByteBuffer) {
        buffer.putFloat(value)
    }
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
    override fun encode(buffer: ByteBuffer) {
        var numNulls = 1 + (value.length + 1) % 4
        buffer.put(value.toByteArray(Charsets.US_ASCII))
        while ((numNulls--) > 0) {
            buffer.put(0.toByte())
        }
    }

    companion object {
        fun decode(buffer: ByteBuffer): OscString {
            val list = mutableListOf<ByteArray>()
            var sz = 0
            while (true) {
                val buf = ByteArray(4)
                buffer.get(buf)
                if (buf.last().toInt() != 0) {
                    list.add(buf)
                    sz += 4
                } else {
                    val numChars = buf.indexOfFirst { it.toInt() == 0 }
                    if (numChars != 0) {
                        list.add(buf.sliceArray(IntRange(0, numChars)))
                        sz += numChars
                    }
                    break
                }
            }
            val ascii = ByteArray(sz)
            var offset = 0
            for (ba in list) {
                ba.copyInto(ascii, offset)
                offset += ba.size
            }
            return OscString(ascii.toString(Charsets.US_ASCII))
        }
    }
}

data class OscBlob(val value: ByteArray) : OscAtomic() {

    override fun encode(buffer: ByteBuffer) {
        buffer.putInt(value.size)
        buffer.put(value)
        var numNulls = value.size % 4
        while (numNulls > 0) {
            buffer.put(0.toByte())
            numNulls--
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OscBlob

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override val tagLen = Pair('b', 4 + value.size + value.size % 4)

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    companion object {
        fun decode(buffer: ByteBuffer): OscBlob {
            val len = buffer.int
            val value = ByteArray(len)
            buffer.get(value)
            // align up
            var numNulls = len % 4
            while (numNulls > 0) {
                if (buffer.get() != 0.toByte()) {
                    throw Exception("Inconsistent blob state")
                }
                numNulls--
            }
            return OscBlob(value)
        }
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
    msg.address.encode(buffer)
    attAtomic.encode(buffer)
    msg.args.forEach { it.encode(buffer) }

    return buffer.array()
}

fun oscPacketToMessage(pkt: ByteArray): OscMessage {
    val buffer = ByteBuffer.wrap(pkt)
    buffer.order(ByteOrder.BIG_ENDIAN)
    val address = OscString.decode(buffer)
    val typeTags = OscString.decode(buffer)
    if (typeTags.value.isEmpty() || typeTags.value[0] != ',') {
        throw Exception("Ill-formed Message for $address: missing ',' in arguments type tags")
    }
    val args = Array<OscAtomic>(typeTags.value.length - 1) {
        val tag = typeTags.value[it + 1]
        when (tag) {
            'i' -> OscInt(buffer.int)
            'f' -> OscFloat(buffer.float)
            's' -> OscString.decode(buffer)
            'b' -> OscBlob.decode(buffer)
            else -> throw Exception("Unsupported OSC message argument type: '$tag'")
        }
    }
    return OscMessage(address, args)
}
