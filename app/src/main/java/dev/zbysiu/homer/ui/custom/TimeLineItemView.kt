package dev.zbysiu.homer.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import dev.zbysiu.homer.R

class TimeLineItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorAccent)
        isAntiAlias = true
    }

    private val lineWidth = context.resources.getDimension(R.dimen.time_line_item_view_width)

    private val dotRadius = context.resources.getDimension(R.dimen.time_line_item_view_dot_radius)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawLine(canvas)
        drawDot(canvas)
    }

    private fun drawLine(canvas: Canvas?) {

        val startX = (width / 2f) - lineWidth
        val stopX = (width / 2f) + lineWidth

        canvas?.drawRect(startX, 0f, stopX, height.toFloat(), paint)
    }

    private fun drawDot(canvas: Canvas?) {
        canvas?.drawCircle(width / 2f, height / 2f, dotRadius, paint)
    }
}