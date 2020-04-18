package org.rtbo.flexosc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.rtbo.flexosc.model.OscInt
import org.rtbo.flexosc.model.OscMessage

sealed class ControlModel(val model: SurfaceModel) {
    var position = Position(0, 0)
    abstract val size: Size

    val left: Int
        get() = position.x
    val top: Int
        get() = position.y
    val right: Int
        get() = position.x + size.width
    val bottom: Int
        get() = position.y + size.height
}


abstract class ReceivingModel(model: SurfaceModel) : ControlModel(model) {
    var rcvAddress: String
        get() {
            return _rcvMsg.address
        }
        set(value) {
            _rcvMsg.address = value
        }

    private val _rcvMsg = AddressedMsgLiveData(model.receiveMessage)

    val receiveMessage: LiveData<OscMessage> = _rcvMsg
}

class ButtonModel(model: SurfaceModel) : ControlModel(model) {
    var sendAddress: String = ""
    override val size = Size(1, 1)

    fun click() = model.sendMessage(OscMessage(sendAddress))
}

class ToggleButtonModel(model: SurfaceModel) : ReceivingModel(model) {
    var sendAddress: String = ""

    private var _state = MsgMapLiveData<Boolean>(receiveMessage, mapSingleBool(true))

    val state: LiveData<Boolean> = this._state

    fun setState(value: Boolean) {
        if (state.value != null && state.value == value) return
        model.sendMessage(
            OscMessage(
                sendAddress,
                OscInt(if (value) 1 else 0)
            )
        )
        _state.value = value
    }

    override val size = Size(1, 1)
}

private fun mapSingleBool(default: Boolean): (OscMessage) -> Boolean {
    return {
        if (it.args.isEmpty() || it.args[0] !is OscInt) default
        else {
            (it.args[0] as OscInt).value > 0
        }
    }
}

private class AddressedMsgLiveData(source: LiveData<OscMessage>) : MediatorLiveData<OscMessage>() {
    var address = ""

    init {
        addSource(source) {
            if (it.address.value == address) {
                value = it
            }
        }
    }
}

private class MsgMapLiveData<T>(source: LiveData<OscMessage>, mapFn: (OscMessage) -> T) :
    MediatorLiveData<T>() {
    init {
        addSource(source) {
            setValue(mapFn(it))
        }
    }
}

