package org.rtbo.flexosc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.rtbo.flexosc.model.OscConnection
import org.rtbo.flexosc.model.OscConnectionUDP
import org.rtbo.flexosc.model.OscMessage
import org.rtbo.flexosc.model.OscSocketParams

class SurfaceModel : ViewModel() {

    private var connection: OscConnection? = null
    private val mutableParams = MutableLiveData<OscSocketParams>(null)

    val params: LiveData<OscSocketParams> = mutableParams
    fun setParams(value: OscSocketParams) {
        mutableParams.value = value
        connection = OscConnectionUDP(value)
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
            connection?.sendMessage(msg)
        }
    }

    val receiveMessage = object : MutableLiveData<OscMessage>() {
        override fun onActive() {
            super.onActive()
            if (connection != null) {
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
        while (connection != null && listening) {
            connection?.receiveMessage()?.let {
                receiveMessage.postValue(it)
            }
        }
    }
}
