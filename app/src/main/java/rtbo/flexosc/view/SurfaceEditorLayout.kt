package rtbo.flexosc.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class SurfaceEditorLayout(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : ViewGroup(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }
}