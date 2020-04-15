package org.rtbo.flexosc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DummySurfaceModel : ViewModel() {

    val connection: MutableLiveData<OscConnection> by lazy {
        MutableLiveData<OscConnection>()
    }

    fun sendMessage(msg: OscMessage) {
        viewModelScope.launch {
            connection.value?.sendMessage(msg)
        }
    }

}