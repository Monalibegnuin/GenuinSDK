package com.begenuin.library.common.customViews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.begenuin.library.R
import com.begenuin.library.common.Utility

class InfinityView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var progress: Float = 100.0f
        set(value) {
            field = value
            drawView()
            invalidate()
        }

    private val maskLayer = Path()

    private val lineWidth: Float = 1f
    private var progressBarPaint: Paint = Paint()
    private var strokePaint: Paint = Paint()

    init {
        progressBarPaint.color = context.resources.getColor(
            R.color.black_opacity60, null
        )

        progressBarPaint.isAntiAlias = true
        progressBarPaint.style = Paint.Style.FILL

        strokePaint.color = context.resources.getColor(
            R.color.white_opacity40, null
        )
        strokePaint.strokeWidth = Utility.dpToPx(
            lineWidth, getContext()
        )
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawView()
        canvas.drawPath(maskLayer, progressBarPaint)
        canvas.drawPath(maskLayer, strokePaint)
    }

    private fun drawView() {
        val betweenWidth = width / 8f
        val bounds = RectF(0f, 0f, width - betweenWidth, height.toFloat())
        maskLayer.reset()
        val radius = Utility.dpToPx(16f, context)
        maskLayer.moveTo(bounds.left + radius, bounds.top)
        maskLayer.lineTo(bounds.right - radius, bounds.top)
        maskLayer.cubicTo(
            bounds.right - radius,
            bounds.top,
            bounds.right,
            bounds.top,
            bounds.right + radius / 4,
            bounds.top + radius / 4
        )
        maskLayer.lineTo(bounds.right + betweenWidth, (height / 2).toFloat())
        maskLayer.lineTo(bounds.right + radius / 4, bounds.bottom - radius / 4)
        maskLayer.cubicTo(
            bounds.right + radius / 4,
            bounds.bottom - radius / 4,
            bounds.right,
            bounds.bottom,
            bounds.right - radius,
            bounds.bottom
        )
        maskLayer.lineTo(bounds.left + radius, bounds.bottom)
        maskLayer.cubicTo(
            bounds.left + radius,
            bounds.bottom,
            bounds.left,
            bounds.bottom,
            bounds.left,
            bounds.bottom - radius
        )
        maskLayer.lineTo(bounds.left, bounds.top + radius)
        maskLayer.cubicTo(
            bounds.left,
            bounds.top + radius,
            bounds.left,
            bounds.top,
            bounds.left + radius,
            bounds.top
        )
//        maskLayer.quadTo(bounds.left, bounds.top, bounds.left + radius, bounds.top)

        maskLayer.close()
    }
}
