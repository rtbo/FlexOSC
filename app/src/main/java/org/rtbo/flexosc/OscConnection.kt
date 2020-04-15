package org.rtbo.flexosc

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

data class ConnectionParams(val address: String, val sendPort: Int, val rcvPort: Int) {
    override fun toString(): String {
        return "$address:\u2191$sendPort:\u2193$rcvPort"
    }
}

abstract class OscConnection(val params: ConnectionParams) {
    abstract suspend fun sendMessage(msg: OscMessage)
}

class UdpOscConnection(params: ConnectionParams) : OscConnection(params) {

    private val sendSocket: DatagramSocket by lazy {
        DatagramSocket()
    }

    private val hostAddress: InetAddress by lazy {
        InetAddress.getByName(params.address)
    }

    override suspend fun sendMessage(msg: OscMessage) {
        val arr = msg.payload
        val zer = arr[msg.address.length].toInt();

        withContext(Dispatchers.IO) {
            Log.d("OSC", "sending message ${msg.address} on ${hostAddress.toString()}:$params.sendPort")
            Log.d("OSC", "arr.size = ${arr.size} / address.length = ${msg.address.length}")
            Log.d("OSC", "zero = ${arr[msg.address.length].toInt()}")
            Log.d("OSC", "arr = ${toHex(arr)}")
            Log.d("OSC", "msg.address = ${toHex(msg.address.toByteArray(Charsets.US_ASCII))}")
            sendSocket.send(DatagramPacket(arr, arr.size, hostAddress, params.sendPort))
        }
    }
}

fun toHex(arr: ByteArray): String {
    return arr.joinToString(", ") { String.format("%02X", it) }
}