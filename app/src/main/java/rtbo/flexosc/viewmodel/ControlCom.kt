package rtbo.flexosc.viewmodel

import rtbo.flexosc.model.OscInt
import rtbo.flexosc.model.OscMessage
import rtbo.flexosc.util.asLiveData
import rtbo.flexosc.util.filter
import rtbo.flexosc.util.map

sealed class ControlCom(val surface: ControlSurface) {

    open var address: String = ""

}

abstract class ReceivingCom(surface: ControlSurface) : ControlCom(surface) {
    val onRcvMessage = surface.onRcvMessage.filter { it.address.value == address }
}

open class BoolReceivingCom(surface: ControlSurface) : ReceivingCom(surface) {
    val value = onRcvMessage.map { mapSingleBool(it) }.asLiveData()
}

open class SendingCom(surface: ControlSurface) : ControlCom(surface) {
    open fun send() {
        surface.sendMessage(OscMessage(address))
    }
}

open class CompoundCom<S : SendingCom, R : ReceivingCom>(
    surface: ControlSurface,
    val send: S,
    val rcv: R
) : ControlCom(surface) {

    init {
        assert(send.surface == surface)
        assert(rcv.surface == surface)
    }

    override var address: String
        get() {
            // This is not an invariant.
            // Having different addresses is supported, but in that case,
            // send.address and rcv.address must be used directly
            assert(send.address == rcv.address)
            return send.address
        }
        set(value) {
            send.address = value
            rcv.address = value
        }
}

class BoolReceivingCompoundCom(surface: ControlSurface, send: SendingCom, rcv: BoolReceivingCom) :
    CompoundCom<SendingCom, BoolReceivingCom>(surface, send, rcv) {
    constructor(surface: ControlSurface) : this (surface,
        SendingCom(surface),
        BoolReceivingCom(surface)
    )
}

private fun mapSingleBool(msg: OscMessage): Boolean? {
    return if (msg.args.isEmpty() || msg.args[0] !is OscInt) null
    else {
        (msg.args[0] as OscInt).value > 0
    }
}
