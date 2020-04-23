package rtbo.flexosc.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Button

class SurfaceIconButton(context: Context, attrs: AttributeSet?) :
    Button(context, attrs) {

    constructor(context: Context) : this(context, null)


    var icon: Drawable? = null
        set(value) {
            value?.setBounds(0, 0, value.intrinsicWidth, value.intrinsicHeight)
            field = value
            requestLayout()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widMode = MeasureSpec.getMode(widthMeasureSpec)
        val widSize = MeasureSpec.getSize(widthMeasureSpec)
        val heiMode = MeasureSpec.getMode(heightMeasureSpec)
        val heiSize = MeasureSpec.getSize(heightMeasureSpec)

        val iconWid = icon?.intrinsicWidth ?: 0
        val iconHei = icon?.intrinsicHeight ?: 0

        val desiredWid =
            if (widMode == MeasureSpec.EXACTLY) widSize else paddingLeft + iconWid + paddingRight
        val desiredHei =
            if (widMode == MeasureSpec.EXACTLY) heiSize else paddingTop + iconHei + paddingBottom

        val maxWid = if (widMode == MeasureSpec.AT_MOST) widSize else Int.MAX_VALUE
        val maxHei = if (heiMode == MeasureSpec.AT_MOST) heiSize else Int.MAX_VALUE

        setMeasuredDimension(
            desiredWid.coerceAtMost(maxWid),
            desiredHei.coerceAtMost(maxHei)
        )
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        canvas.save()
        super.onDraw(canvas)
        canvas.restore()

        icon?.let {
            val left = (measuredWidth - it.intrinsicWidth) / 2
            val top = (measuredHeight - it.intrinsicHeight) / 2
            canvas.translate(left.toFloat(), top.toFloat())
            it.draw(canvas)
        }
    }
}