package rtbo.flexosc.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
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
    private val model: SurfaceModel
) : ViewGroup(context) {

    private val paramsView: TextView = TextView(context)
    var onParamsChangeRequestListener: (() -> Unit)? = null

    private data class ViewWrapper(val control: ControlModel, val view: View)

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
        paramsView.text = model.params.value.toString()
        model.params.observe(lifecycleOwner, Observer {
            paramsView.text = (it?.toString() ?: "(no connection settings)")
        })
        paramsView.setOnClickListener {
            onParamsChangeRequestListener?.invoke()
        }

        for (control in model.controls.value!!) {
            val view = when (control) {
                is ButtonModel -> createButton(control)
                is LedButtonModel -> createLedButton(control)
                is ToggleButtonModel -> createToggleButton(control)
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
        gridWidth = (if (width == largest) largest / 20 else smallest / 10).coerceAtLeast(minGs.toInt())
        gridHeight = (if (height == largest) largest / 20 else smallest / 10).coerceAtLeast(minGs.toInt())

        var lef = 0
        var rig = 0
        var top = 0
        var bot = paramsView.measuredHeight
        for (w in wrappers) {
            w.view.measure(
                MeasureSpec.makeMeasureSpec(
                    (gridWidth * w.control.size.width), MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(
                    (gridHeight * w.control.size.height), MeasureSpec.EXACTLY
                )
            )
            val l = w.control.left * gridWidth
            val r = l + w.view.measuredWidth
            val t = paramsView.measuredHeight + w.control.top * gridHeight
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
            val lef = l + w.control.left * gridWidth
            val top = paramsBot + w.control.top * gridWidth
            val rig = lef + w.view.measuredWidth
            val bot = top + w.view.measuredHeight
            w.view.layout(
                lef, top, rig, bot
            )
        }
    }

    private fun mapIconToRes(icon: Int): Int {
        return when (icon) {
            PLAY_ICON -> R.drawable.ic_play_black_36dp
            STOP_ICON -> R.drawable.ic_stop_black_36dp
            REC_ICON -> R.drawable.ic_record_black_36dp
            STOP_TRASH_ICON -> R.drawable.ic_stop_trash_black_36dp
            ADD_ICON -> R.drawable.ic_add_black_36dp
            REM_ICON -> R.drawable.ic_remove_black_36dp
            START_ICON -> R.drawable.ic_start_black_36dp
            END_ICON -> R.drawable.ic_end_black_36dp
            PREV_ICON -> R.drawable.ic_prev_black_36dp
            NEXT_ICON -> R.drawable.ic_next_black_36dp
            else -> R.drawable.ic_not_found_black_36dp
        }
    }

    private fun iconIdToDrawable(iconId: Int): Drawable {
        val resId = mapIconToRes(iconId)
        val icon = AppCompatResources.getDrawable(context, resId)!!
        return DrawableCompat.wrap(icon)
    }

    private fun createButton(control: ButtonModel): View {
        val view = SurfaceIconButton(context)
        view.setOnClickListener {
            control.click()
        }
        control.icon?.let { iconIdToDrawable(it) }?.let {
            view.icon = it
            // view.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
        }
        return view
    }

    private fun createLedButton(control: LedButtonModel): View {
        val view = SurfaceIconButton(context)
        view.setOnClickListener {
            control.click()
        }
        val light = control.ledColor
        val dark = darker(light)

        val icon = iconIdToDrawable(control.ledIcon)
        DrawableCompat.setTint(icon, dark)
        // view.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        view.icon = icon
        //view.setPadding(icon.intrinsicWidth, 0, 0, 0)
        control.ledState.observe(lifecycleOwner, Observer {
            DrawableCompat.setTint(icon, if (it) light else dark)
            view.invalidate()
        })
        return view
    }

    private fun createToggleButton(control: ToggleButtonModel): View {
        val view = ToggleButton(context)
        view.setOnCheckedChangeListener { _, checked ->
            control.setState(checked)
        }
        control.state.observe(lifecycleOwner, Observer {
            view.isChecked = it
        })
        control.icon?.let { iconIdToDrawable(it) }?.let {
            view.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
        }
        return view
    }


}