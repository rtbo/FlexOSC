package org.rtbo.flexosc.model

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

data class OscSocketParams(
    val address: String,
    val sendPort: Int,
    val rcvPort: Int
) {
    override fun toString(): String {
        return "$address:\u2191$sendPort:\u2193$rcvPort"
    }
}

sealed class OscConnection(val params: OscSocketParams) {
    abstract suspend fun sendMessage(msg: OscMessage)
    abstract suspend fun receiveMessage(): OscMessage?
}

class OscConnectionUDP(params: OscSocketParams) : OscConnection(params) {
    private val sendSocket: DatagramSocket by lazy {
        DatagramSocket()
    }

    private val rcvSocket: DatagramSocket by lazy {
        val sock = DatagramSocket(params.rcvPort)
        sock.connect(InetAddress.getByName(params.address), params.rcvPort)
        sock.soTimeout = 1000
        sock
    }

    private val hostAddress: InetAddress by lazy {
        InetAddress.getByName(params.address)
    }

    override suspend fun sendMessage(msg: OscMessage) {
        val arr = oscMessageToPacket(msg)

        withContext(Dispatchers.IO) {
            Log.d(
                "OSC_MSG",
                "sending message ${msg.address} on $hostAddress:$params.sendPort"
            )
            sendSocket.send(DatagramPacket(arr, arr.size, hostAddress, params.sendPort))
        }
    }

    private val rcvBuf = ByteArray(MAX_MSG_SIZE)

    override suspend fun receiveMessage(): OscMessage? {
        return withContext(Dispatchers.IO) {
            val pkt = DatagramPacket(rcvBuf, rcvBuf.size)
            try {
                rcvSocket.receive(pkt)
                oscPacketToMessage(rcvBuf.copyOf(pkt.length))
            } catch (ex: SocketTimeoutException) {
                null
            }
        }
    }
}

const val MAX_MSG_SIZE = 16 * 1024
