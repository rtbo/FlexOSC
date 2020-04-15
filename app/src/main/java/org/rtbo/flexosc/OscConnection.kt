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

        withContext(Dispatchers.IO) {
            Log.d("OSC_MSG", "sending message ${msg.address} on ${hostAddress.toString()}:$params.sendPort")
            sendSocket.send(DatagramPacket(arr, arr.size, hostAddress, params.sendPort))
        }
    }
}
