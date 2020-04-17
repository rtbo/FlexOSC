package org.rtbo.flexosc.model

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


sealed class ConnectionTransport(val params: ConnectionParams) {
    abstract suspend fun sendMessage(msg: OscMessage)
    abstract suspend fun receiveMessage(): OscMessage
}

class ConnectionTransportUDP(params: ConnectionParams) : ConnectionTransport(params) {
    private val sendSocket: DatagramSocket by lazy {
        DatagramSocket()
    }

    private val rcvSocket: DatagramSocket by lazy {
        val sock = DatagramSocket(params.rcvPort)
        sock.connect(InetAddress.getByName(params.address), params.rcvPort)
        sock
    }

    private val hostAddress: InetAddress by lazy {
        InetAddress.getByName(params.address)
    }

    private val rcvBuf = ByteArray(MAX_MSG_SIZE)

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

    override suspend fun receiveMessage(): OscMessage {
        return withContext(Dispatchers.IO) {
            val pkt = DatagramPacket(rcvBuf, rcvBuf.size)
            rcvSocket.receive(pkt)
            oscPacketToMessage(rcvBuf.copyOf(pkt.length))
        }
    }
}

const val MAX_MSG_SIZE = 16 * 1024
