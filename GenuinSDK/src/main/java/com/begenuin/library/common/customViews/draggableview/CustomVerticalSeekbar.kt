package com.begenuin.library.common.customViews.draggableview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import com.begenuin.library.R

open class CustomVerticalSeekbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : AppCompatSeekBar(context, attrs) {


    companion object {
        val MAX_SEEKBAR_VALUE = 70
        val MIN_SEEKBAR_VALUE = 25
        private val STEP = 1
    }

    init {
        this.isFocusableInTouchMode = true
        this.max = ((MAX_SEEKBAR_VALUE - MIN_SEEKBAR_VALUE) / STEP)
        this.progressDrawable = resources.getDrawable(R.drawable.font_seekbar_style, null)
        this.thumb = resources.getDrawable(R.drawable.font_custom_thumb, null)
        isFocusable = false
        isClickable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        } else {
            when (event!!.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_UP,
                -> {
                    progress = max - (max * event.y / height).toInt()
                    onSizeChanged(width, height, 0, 0)
                }
                MotionEvent.ACTION_CANCEL -> {

                }
            }
            return true
        }
    }

    @Synchronized
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        onSizeChanged(width, height, 0, 0)
    }
}