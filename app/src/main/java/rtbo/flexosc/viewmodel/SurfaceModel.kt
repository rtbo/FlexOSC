package rtbo.flexosc.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rtbo.flexosc.model.OscConnection
import rtbo.flexosc.model.OscConnectionUDP
import rtbo.flexosc.model.OscMessage
import rtbo.flexosc.model.OscSocketParams

// dimension units are in grid units
data class Position(val x: Int, val y: Int)
data class Size(val width: Int, val height: Int)

class SurfaceModel : ViewModel() {

    var gridSize = 64

    private var connection: OscConnection? = null
    private val mutableParams = MutableLiveData<OscSocketParams>(null)

    val params: LiveData<OscSocketParams> = mutableParams
    fun setParams(value: OscSocketParams) {
        mutableParams.value = value
        connection?.close()
        connection = OscConnectionUDP(value)
        if (receiveMessage.hasActiveObservers() && !listening) {
            startListening()
        }
    }

    private val controlList = ArrayList<ControlModel>()
    private val mutableControls = MutableLiveData<List<ControlModel>>(controlList)
    private val mutableControlAddEvent = MutableLiveData<ControlModel>()
    private val mutableControlRemEvent = MutableLiveData<ControlModel>()

    val controls: LiveData<List<ControlModel>> = mutableControls
    val controlAddEvent: LiveData<ControlModel> = mutableControlAddEvent
    val controlRemEvent: LiveData<ControlModel> = mutableControlRemEvent

    fun setControls(controls: List<ControlModel>) {
        controlList.clear()
        controlList.addAll(controls)
        mutableControls.value = controlList
    }

    fun addControl(control: ControlModel) {
        controlList.add(control)
        mutableControlAddEvent.value = control
    }

    fun remControl(control: ControlModel) {
        controlList.remove(control)
        mutableControlRemEvent.value = control
    }

    fun sendMessage(msg: OscMessage) {
        viewModelScope.launch {
            connection?.let{
                Log.d(
                    "OSC_MSG",
                    "Sending $msg"
                )
                it.sendMessage(msg)
            }
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
        Log.d("FlexOSX", "Entering listening loop on ${params.value.toString()}")
        while (connection != null && listening) {
            connection?.receiveMessage()?.let {
                Log.d("OSC_MSG", "Received $it")
                receiveMessage.postValue(it)
            }
        }
        Log.d("FlexOSX", "Exiting listening loop")
    }
}
