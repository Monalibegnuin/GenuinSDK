package com.begenuin.library.common.customViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * This TextView is able to draw text on the padding area.
 * It's mainly used to support italic texts in custom fonts that can go out of bounds.
 * In this case, you've to set an horizontal padding (or just end padding).
 *
 * This implementation is doing a render-to-texture procedure, as such it consumes more RAM than a standard TextView,
 * it uses an additional bitmap of the size of the view.
 */
class TextViewNoClipping(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    override fun onDraw(canvas: Canvas) {
        val paint: Paint = paint
        val color: Int = paint.color
        // Draw what you have to in transparent
        // This has to be drawn, otherwise getting values from layout throws exceptions
        // Draw what you have to in transparent
        // This has to be drawn, otherwise getting values from layout throws exceptions
//        setTextColor(Color.TRANSPARENT)
        super.onDraw(canvas)
        // setTextColor invalidates the view and causes an endless cycle
        // setTextColor invalidates the view and causes an endless cycle
        paint.color = color

        println("Drawing text info:")

        val layout: Layout = layout
        val text = text.toString()

        for (i in 0 until layout.lineCount) {
            val start: Int = layout.getLineStart(i)
            val end: Int = layout.getLineEnd(i)
            val line = text.substring(start, end)
            println("Line:\t$line")
            val left: Float = layout.getLineLeft(i)
            val baseLine: Int = layout.getLineBaseline(i)
            canvas.drawText(
                line,
                left + totalPaddingLeft,  // The text will not be clipped anymore
                // You can add a padding here too, faster than string string concatenation
                (baseLine + totalPaddingTop).toFloat(),
                getPaint()
            )
        } // If rtt is not available, use default rendering process
    }
}