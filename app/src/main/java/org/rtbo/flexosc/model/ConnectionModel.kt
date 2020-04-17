package org.rtbo.flexosc.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class ConnectionParams(
    val address: String,
    val sendPort: Int,
    val rcvPort: Int
) {
    override fun toString(): String {
        return "$address:\u2191$sendPort:\u2193$rcvPort"
    }
}

class ConnectionModel : ViewModel() {

    private var transport: ConnectionTransport? = null
    private val mutableParams = MutableLiveData<ConnectionParams>(null)

    val params: LiveData<ConnectionParams> = mutableParams
    fun setParams(value: ConnectionParams) {
        mutableParams.value = value
        transport = ConnectionTransportUDP(value)
        if (receiveMessage.hasActiveObservers() && !listening) {
            startListening()
        }
    }

/*
    private val controllist = arraylist<controlmodel>()
    private val mutablecontrols = mutablelivedata<list<controlmodel>>(controllist)

    val controls: livedata<list<controlmodel>> = mutablecontrols
    fun addcontrol(control: controlmodel) {
        controllist.add(control)
        mutablecontrols.value = controllist
    }
    fun remcontrol(control: controlmodel) {
        controllist.remove(control)
        mutablecontrols.value = controllist
    }
*/

    fun sendMessage(msg: OscMessage) {
        viewModelScope.launch {
            transport?.sendMessage(msg)
        }
    }

    val receiveMessage = object : MutableLiveData<OscMessage>() {
        override fun onActive() {
            super.onActive()
            if (transport != null) {
                startListening()
            }
        }

        override fun onInactive() {
            super.onInactive()
            stopListening()
        }
    }

    private var listening = false

    private fun startListening() {
        listening = true
        viewModelScope.launch {
            listenLoop()
        }
    }

    private fun stopListening() {
        listening = false
    }

    private suspend fun listenLoop() {
        while (transport != null && listening) {
            transport?.receiveMessage()?.let {
                receiveMessage.postValue(it)
            }
        }
    }
}
