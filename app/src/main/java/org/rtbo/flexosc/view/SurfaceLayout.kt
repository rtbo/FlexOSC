package org.rtbo.flexosc.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.rtbo.flexosc.viewmodel.ButtonModel
import org.rtbo.flexosc.viewmodel.ControlModel
import org.rtbo.flexosc.viewmodel.SurfaceModel
import org.rtbo.flexosc.viewmodel.ToggleButtonModel

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
        val gs = model.gridSize
        paramsView.measure(
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.AT_MOST
            ),
            MeasureSpec.makeMeasureSpec(
                gs, MeasureSpec.AT_MOST
            )
        )
        var lef = 0
        var rig = 0
        var top = paramsView.measuredHeight
        var bot = 0
        for (w in wrappers) {
            lef = lef.coerceAtMost(w.control.left * gs)
            rig = rig.coerceAtLeast(w.control.right * gs)
            top = top.coerceAtMost(paramsView.measuredHeight + w.control.top * gs)
            bot = bot.coerceAtLeast(top + w.control.bottom * gs)
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
        val gs = model.gridSize
        val paramsLef = l + (measuredWidth - paramsView.measuredWidth) / 2
        val paramsRig = paramsLef + paramsView.measuredWidth
        val paramsBot = t + paddingTop + paramsView.measuredHeight
        paramsView.layout(paramsLef, t + paddingTop, paramsRig, paramsBot)
        for (w in wrappers) {
            val lef = l + w.control.left * gs
            val top = paramsBot + w.control.top * gs
            val rig = l + w.control.right * gs
            val bot = paramsBot + w.control.bottom * gs
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