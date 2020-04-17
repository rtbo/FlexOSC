package org.rtbo.flexosc.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

// dimension units are in grid units
data class Position(val x: Int, val y: Int)
data class Size(val width: Int, val height: Int)

sealed class ControlModel(val model: ConnectionModel) {
    var position = Position(0, 0)
    abstract val size: Size
}

abstract class ReceivingModel(model: ConnectionModel) : ControlModel(model) {
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

class ButtonModel(model: ConnectionModel) : ControlModel(model) {
    var sendAddress: String = ""
    override val size = Size(1, 1)

    fun click() = model.sendMessage(OscMessage(sendAddress))
}

class ToggleButtonModel(model: ConnectionModel) : ReceivingModel(model) {
    var sendAddress: String = ""

    private var _state = MsgMapLiveData<Boolean>(receiveMessage, mapSingleBool(true))

    val state: LiveData<Boolean> = this._state

    fun setState(value: Boolean) {
        model.sendMessage(OscMessage(sendAddress, OscInt(if (value) 1 else 0)))
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

