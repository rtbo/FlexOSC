package org.rtbo.flexosc

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

abstract class OscConnection(val host: InetSocketAddress) {
    init {
        if (host.isUnresolved) {
            throw Exception("cannot resolve host")
        }
    }

    abstract fun sendMessage(msg: OscMessage)
}

class UdpOscConnection(host: InetSocketAddress) : OscConnection(host) {
    private val udp = DatagramSocket()

    override fun sendMessage(msg: OscMessage) {
        val buf = msg.payload.array()
        udp.send(DatagramPacket(buf, buf.size, host))
    }
}