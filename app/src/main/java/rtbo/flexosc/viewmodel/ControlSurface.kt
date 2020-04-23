package rtbo.flexosc.viewmodel

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import rtbo.flexosc.model.*
import rtbo.flexosc.util.LiveEventSource
import rtbo.flexosc.util.MutableLiveEventSource

class ControlSurface() : ViewModel() {

    private var connection: OscConnection? = null
    private val mutableSocketParams = MutableLiveData<OscSocketParams>(null)
    private val controlList = ArrayList<Control>()
    private val mutableOnControlAdd = MutableLiveEventSource<Control>()
    private val mutableOnControlRem = MutableLiveEventSource<Control>()
    private var receiveChannel: Channel<OscMessage>? = null
    private val mutableOnRcvMsg = RcvMessageLiveEventSource()
    private var hasActiveReceivers = false

    val socketParams: LiveData<OscSocketParams> = mutableSocketParams
    fun setSocketParams(value: OscSocketParams) {
        stopReceiving()
        connection?.close()
        connection = OscConnectionUDP(value)
        mutableSocketParams.value = value
        startReceiving()
    }

    val controls: List<Control>
        get() = controlList

    @MainThread
    fun addControl(control: Control) {
        assert(control.com.surface == this)
        controlList.add(control)
        mutableOnControlAdd.notifyEvent(control)
    }

    @MainThread
    fun remControl(control: Control) {
        assert(control.com.surface == this)
        controlList.remove(control)
        mutableOnControlRem.notifyEvent(control)
    }

    val onControlAdd: LiveEventSource<Control> = mutableOnControlAdd
    val onControlRem: LiveEventSource<Control> = mutableOnControlRem

    fun sendMessage(msg: OscMessage) {
        Log.d("OSC_MSG", "Sending $msg")
        viewModelScope.launch {
            connection?.sendMessage(msg)
        }
    }

    val onRcvMessage: LiveEventSource<OscMessage> = mutableOnRcvMsg

    private val receiving
        get() = receiveChannel != null

    private fun startReceiving() {
        assert(!receiving)
        if (hasActiveReceivers) {
            connection?.let{ conn ->
                viewModelScope.launch(Dispatchers.Main) {
                    receiveLoop(conn)
                }
            }
        }
    }

    private fun stopReceiving() {
        receiveChannel?.cancel()
    }

    private suspend fun receiveLoop(conn: OscConnection) {
        assert(receiveChannel == null)
        assert(hasActiveReceivers)
        try {
            Log.d("FlexOSX", "Entering listening loop on ${socketParams.toString()}")
            // discovering a DAW such as Ardour means to receive a lot of messages at once.
            // Buffering should speed up the process
            val channel = Channel<OscMessage>(64)
            receiveChannel = channel
            conn.receiveMessages(channel)
            for (msg in channel) {
                Log.d("OSC_MSG", "Received $msg")
                mutableOnRcvMsg.notifyEvent(msg)
            }
            Log.d("FlexOSX", "Exiting listening loop")
        } finally {
            receiveChannel = null
        }
    }

    private inner class RcvMessageLiveEventSource : MutableLiveEventSource<OscMessage>() {
        override fun onActive() {
            hasActiveReceivers = true
            startReceiving()
        }

        override fun onInactive() {
            stopReceiving()
            hasActiveReceivers = false
        }
    }
}
