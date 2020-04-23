package rtbo.flexosc.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import rtbo.flexosc.R
import rtbo.flexosc.viewmodel.ButtonControl
import rtbo.flexosc.viewmodel.Control
import rtbo.flexosc.viewmodel.IconId
import rtbo.flexosc.viewmodel.LedButtonControl

interface ControlViewFactory {
    fun createControlView(control: Control): View
}

abstract class BaseControlViewFactory(protected var context: Context) : ControlViewFactory {

    protected fun mapIconIdToRes(iconId: IconId): Int {
        return when (iconId) {
            IconId.PLAY -> R.drawable.ic_play_black_36dp
            IconId.STOP -> R.drawable.ic_stop_black_36dp
            IconId.REC -> R.drawable.ic_record_black_36dp
            IconId.STOP_TRASH -> R.drawable.ic_stop_trash_black_36dp
            IconId.ADD -> R.drawable.ic_add_black_36dp
            IconId.REM -> R.drawable.ic_remove_black_36dp
            IconId.START -> R.drawable.ic_start_black_36dp
            IconId.END -> R.drawable.ic_end_black_36dp
            IconId.PREV -> R.drawable.ic_prev_black_36dp
            IconId.NEXT -> R.drawable.ic_next_black_36dp
            else -> R.drawable.ic_not_found_black_36dp
        }
    }

    protected fun iconIdToDrawable(iconId: IconId): Drawable {
        val resId = mapIconIdToRes(iconId)
        val icon = AppCompatResources.getDrawable(context, resId)!!
        return DrawableCompat.wrap(icon)
    }
}

class SurfaceControlViewFactory(context: Context, private var lifecycleOwner: LifecycleOwner) :
    BaseControlViewFactory(context) {
    override fun createControlView(control: Control): View {
        return when (control) {
            is ButtonControl -> createButtonControlView(control)
            is LedButtonControl -> createLedButtonControlView(control)
        }
    }

    private fun createButtonControlView(control: ButtonControl): View {
        val view = SurfaceIconButton(context)
        view.setOnClickListener {
            control.click()
        }
        view.icon = iconIdToDrawable(control.icon.id)
        return view
    }

    private fun createLedButtonControlView(control: LedButtonControl): View {
        val view = SurfaceIconButton(context)
        view.setOnClickListener {
            control.click()
        }
        val light = control.icon.color
        val dark = darker(light)

        val icon = iconIdToDrawable(control.icon.id)
        DrawableCompat.setTint(icon, dark)
        view.icon = icon
        control.com.rcv.value.observe(lifecycleOwner, Observer {
            it?.let {
                DrawableCompat.setTint(icon, if (it) light else dark)
                view.invalidate()
            }
        })
        return view
    }
}

private fun darker(color: Int): Int {
    val hsl = floatArrayOf(0f, 0f, 0f)
    ColorUtils.colorToHSL(color, hsl)
    hsl[2] /= 3f
    return ColorUtils.HSLToColor(hsl)
}
