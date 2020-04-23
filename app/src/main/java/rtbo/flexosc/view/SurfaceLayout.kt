package rtbo.flexosc.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import rtbo.flexosc.viewmodel.Control
import rtbo.flexosc.viewmodel.ControlSurface
import rtbo.flexosc.viewmodel.left
import rtbo.flexosc.viewmodel.top

@SuppressLint("ViewConstructor")
class SurfaceLayout(
    context: Context,
    private val paramsView: TextView,
    private val controlViewFactory: ControlViewFactory
) : ViewGroup(context) {

    private data class ViewWrapper(val control: Control, val view: View)

    private val wrappers = ArrayList<ViewWrapper>()
    private var gridWidth: Int = 0
    private var gridHeight: Int = 0

    init {
        addView(paramsView)
    }

    fun addControl(control: Control) {
        val view = controlViewFactory.createControlView(control)
        wrappers.add(ViewWrapper(control, view))
        addView(view)
    }

    fun remControl(control: Control) {
        val ind = wrappers.indexOfFirst { it.control == control }
        if (ind != -1) {
            val view = wrappers[ind].view
            wrappers.removeAt(ind)
            removeView(view)
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
}