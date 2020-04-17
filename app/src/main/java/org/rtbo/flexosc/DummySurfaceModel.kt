package org.rtbo.flexosc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.rtbo.flexosc.model.ConnectionParams
import org.rtbo.flexosc.model.ConnectionTransport
import org.rtbo.flexosc.model.ConnectionTransportUDP
import org.rtbo.flexosc.model.OscMessage

class DummySurfaceModel : ViewModel() {

    val transport: MutableLiveData<ConnectionTransport> by lazy {
        MutableLiveData<ConnectionTransport>(
            ConnectionTransportUDP(
                ConnectionParams(
                    "192.168.1.23", 3819, 8000
                )
            )
        )
    }

    fun sendMessage(msg: OscMessage) {
        viewModelScope.launch {
            transport.value?.sendMessage(msg)
        }
    }

}