package rtbo.flexosc.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import rtbo.flexosc.util.LiveDataObservatory
import rtbo.flexosc.util.liveDataValueDelegate

data class Rect(val x: Int = 0, val y: Int = 0, val width: Int = 0, val height: Int = 0)

val Rect.left: Int
    get() = x

val Rect.right: Int
    get() = x + width

val Rect.top: Int
    get() = y

val Rect.bottom: Int
    get() = y + height


enum class IconId {
    UNKNOWN,
    PLAY,
    STOP,
    REC,
    STOP_TRASH,
    ADD,
    REM,
    START,
    END,
    PREV,
    NEXT
}

data class Icon(val id: IconId = IconId.UNKNOWN, val color: Int = 0xff000000.toInt())

sealed class Control(open val com: ControlCom, initialRect: Rect = Rect()) {
    private val liveRect = MutableLiveData(initialRect)
    var rect: Rect by liveDataValueDelegate(liveRect)
    val rectObservatory = LiveDataObservatory(liveRect)
}

interface ControlWithIcon {
    var icon: Icon
    val iconObservatory: LiveDataObservatory<Icon>
}

interface ClickableControl {
    fun click()
}

class LedButtonControl(com: BoolReceivingCompoundCom, initialRect: Rect = Rect()) :
    Control(com, initialRect),
    ControlWithIcon, ClickableControl {

    override val com
        get() = super.com as BoolReceivingCompoundCom

    private val liveIcon = MutableLiveData<Icon>()
    override var icon: Icon by liveDataValueDelegate(liveIcon)
    override val iconObservatory = LiveDataObservatory(liveIcon)

    override fun click() {
        com.send.send()
    }
}

class ButtonControl(com: SendingCom, initialRect: Rect = Rect()) :
    Control(com, initialRect),
    ControlWithIcon, ClickableControl {

    override val com
        get() = super.com as SendingCom


    private val liveIcon = MutableLiveData<Icon>()
    override var icon: Icon by liveDataValueDelegate(liveIcon)
    override val iconObservatory = LiveDataObservatory(liveIcon)

    override fun click() {
        com.send()
    }
}
