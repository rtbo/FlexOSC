package rtbo.flexosc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rtbo.flexosc.model.OscConnection
import rtbo.flexosc.model.OscConnectionUDP
import rtbo.flexosc.model.OscMessage
import rtbo.flexosc.model.OscSocketParams

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