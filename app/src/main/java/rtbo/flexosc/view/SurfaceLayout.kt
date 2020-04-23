package rtbo.flexosc.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import rtbo.flexosc.R
import rtbo.flexosc.viewmodel.*

@SuppressLint("ViewConstructor")
class SurfaceLayout(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val surface: ControlSurface
) : ViewGroup(context) {

    private val paramsView: TextView = TextView(context)
    var onParamsChangeRequestListener: (() -> Unit)? = null

    private data class ViewWrapper(val control: Control, val view: View)

    private val wrappers = ArrayList<ViewWrapper>()
    private var gridWidth: Int = 0
    private var gridHeight: Int = 0

    private fun darker(color: Int): Int {
        val hsl = floatArrayOf(0f, 0f, 0f)
        ColorUtils.colorToHSL(color, hsl)
        hsl[2] /= 3f
        return ColorUtils.HSLToColor(hsl)
    }

    init {
        addView(paramsView)
        paramsView.text = surface.socketParams.value.toString()
        surface.socketParams.observe(lifecycleOwner, Observer {
            paramsView.text = (it?.toString() ?: "(no connection settings)")
        })
        paramsView.setOnClickListener {
            onParamsChangeRequestListener?.invoke()
        }

        for (control in surface.controls) {
            val view = when (control) {
                is ButtonControl -> createButton(control)
                is LedButtonControl -> createLedButton(control)
                else -> throw Exception("unsupported control")
            }
            wrappers.add(ViewWrapper(control, view))
            addView(view)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        paramsView.measure(
            MeasureSpec.makeMeasureSpec(
                width,
                MeasureSpec.AT_MOST
            ),
            MeasureSpec.makeMeasureSpec(
                0, MeasureSpec.UNSPECIFIED
            )
        )
        val height = MeasureSpec.getSize(heightMeasureSpec) - paramsView.measuredHeight
        val largest = width.coerceAtLeast(height)
        val smallest = width.coerceAtMost(height)
        val minGs = 32 * resources.displayMetrics.density
        gridWidth =
            (if (width == largest) largest / 20 else smallest / 10).coerceAtLeast(minGs.toInt())
        gridHeight =
            (if (height == largest) largest / 20 else smallest / 10).coerceAtLeast(minGs.toInt())

        var lef = 0
        var rig = 0
        var top = 0
        var bot = paramsView.measuredHeight
        for (w in wrappers) {
            val rect = w.control.rect
            w.view.measure(
                MeasureSpec.makeMeasureSpec(
                    (gridWidth * rect.width), MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                    (gridHeight * rect.height), MeasureSpec.EXACTLY
                )
            )
            val l = rect.left * gridWidth
            val r = l + w.view.measuredWidth
            val t = paramsView.measuredHeight + rect.top * gridHeight
            val b = t + w.view.measuredHeight

            lef = lef.coerceAtMost(l)
            rig = rig.coerceAtLeast(r)
            top = top.coerceAtMost(t)
            bot = bot.coerceAtLeast(b)
        }
        setMeasuredDimension(
            (rig - lef + paddingLeft + paddingRight).coerceAtLeast(
                MeasureSpec.getSize(
                    widthMeasureSpec
                )
            ),
            bot - top + paddingTop + paddingBottom
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paramsLef = l + (measuredWidth - paramsView.measuredWidth) / 2
        val paramsRig = paramsLef + paramsView.measuredWidth
        val paramsBot = t + paddingTop + paramsView.measuredHeight
        paramsView.layout(paramsLef, t + paddingTop, paramsRig, paramsBot)
        for (w in wrappers) {
            val rect = w.control.rect
            val lef = l + rect.left * gridWidth
            val top = paramsBot + rect.top * gridHeight
            val rig = lef + w.view.measuredWidth
            val bot = top + w.view.measuredHeight
            w.view.layout(
                lef, top, rig, bot
            )
        }
    }

    private fun mapIconIdToRes(iconId: IconId): Int {
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

    private fun iconIdToDrawable(iconId: IconId): Drawable {
        val resId = mapIconIdToRes(iconId)
        val icon = AppCompatResources.getDrawable(context, resId)!!
        return DrawableCompat.wrap(icon)
    }

    private fun createButton(control: ButtonControl): View {
        val view = SurfaceIconButton(context)
        view.setOnClickListener {
            control.click()
        }
        view.icon = iconIdToDrawable(control.icon.id)
        return view
    }

    private fun createLedButton(control: LedButtonControl): View {
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