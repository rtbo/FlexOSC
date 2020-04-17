package org.rtbo.flexosc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.rtbo.flexosc.model.OscConnection
import org.rtbo.flexosc.model.OscConnectionUDP
import org.rtbo.flexosc.model.OscMessage
import org.rtbo.flexosc.model.OscSocketParams

class DummySurfaceModel : ViewModel() {

    val connection: MutableLiveData<OscConnection> by lazy {
        MutableLiveData<OscConnection>(
            OscConnectionUDP(
                OscSocketParams(
                    "192.168.1.23", 3819, 8000
                )
            )
        )
    }

    fun sendMessage(msg: OscMessage) {
        viewModelScope.launch {
            connection.value?.sendMessage(msg)
        }
    }

}