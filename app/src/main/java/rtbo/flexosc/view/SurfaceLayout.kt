package rtbo.flexosc.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import rtbo.flexosc.viewmodel.ButtonModel
import rtbo.flexosc.viewmodel.ControlModel
import rtbo.flexosc.viewmodel.SurfaceModel
import rtbo.flexosc.viewmodel.ToggleButtonModel

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

    private fun createButton(control: ButtonModel): Button {
        val view = Button(context)
        view.setOnClickListener {
            control.click()
        }
        view.text = "B"
        return view
    }

    private fun createToggleButton(control: ToggleButtonModel): ToggleButton {
        val view = ToggleButton(context)
        view.setOnCheckedChangeListener { _, checked ->
            control.setState(checked)
        }
        control.state.observe(lifecycleOwner, Observer {
            view.isChecked = it
        })
        view.text = "T"
        return view
    }


}