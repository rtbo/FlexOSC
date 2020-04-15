package org.rtbo.flexosc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.rtbo.flexosc.model.ConnectionParams
import org.rtbo.flexosc.model.OscConnection
import org.rtbo.flexosc.model.OscMessage
import org.rtbo.flexosc.model.UdpOscConnection

class DummySurfaceModel : ViewModel() {

    val connection: MutableLiveData<OscConnection> by lazy {
        MutableLiveData<OscConnection>(
            UdpOscConnection(
                ConnectionParams(
                    // TODO place these params at the right place
                    "192.168.1.1", 3819, 8000
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