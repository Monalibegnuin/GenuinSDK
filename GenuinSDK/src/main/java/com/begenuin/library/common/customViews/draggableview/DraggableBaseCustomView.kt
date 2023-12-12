package com.begenuin.begenuin.ui.customview.draggableview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import java.util.*


open class DraggableBaseCustomView(context: Context?) :
    RelativeLayout(context), View.OnTouchListener {

    private val TAG = "BaseView"
    private var videoActualSize: Size? = null
    private var videoContainerSize: Size? = null
    private var videoContainerRect: Rect? = null
    private var resizeFactor: Double = 1.0

    private var touchX: Float = 0F
    private var touchY: Float = 0F

    init {
        init()
    }

    protected fun getBaseView(): RelativeLayout {
        return this
    }

    private fun init() {
        this.id = Random().nextInt()
        this.setOnTouchListener(this)

        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
//        this.setPadding(10, 10, 10, 10)
        layoutParams.addRule(CENTER_IN_PARENT)
        this.layoutParams = layoutParams
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event != null) {
            touchX = event.rawX
            touchY = event.rawY
        }

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

            }

            MotionEvent.ACTION_POINTER_DOWN -> {

            }

            MotionEvent.ACTION_MOVE -> {

            }

            MotionEvent.ACTION_UP -> {

            }
            else -> {
                print("else case")
                return false
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.save()
        super.dispatchDraw(canvas)
        canvas?.restore()
    }

    fun setVideoActualSize(size: Size) {
        this.videoActualSize = size

        val widthFactor =
            size.width.toDouble() / getVideoContainerSize().width.toDouble()
        val heightFactor =
            size.height.toDouble() / getVideoContainerSize().height.toDouble()

        resizeFactor = widthFactor
        if (getVideoContainerSize().height > getVideoContainerSize().width) {
            resizeFactor = heightFactor
        }
    }

    private fun setVideoContainerSize(size: Size) {
        this.videoContainerSize = size

        val widthFactor =
            getVideoActualSize().width.toDouble() / size.width.toDouble()
        val heightFactor =
            getVideoActualSize().height.toDouble() / size.height.toDouble()

        resizeFactor = widthFactor
        if (size.height > size.width) {
            resizeFactor = heightFactor
        }
    }

    fun setVideoContainerRect(rect: Rect) {
        this.videoContainerRect = rect
        var width: Int = rect.right - rect.left
        var height: Int = rect.bottom - rect.top
        var size: Size = Size(width, height)
        setVideoContainerSize(size)
    }

    private fun getVideoContainerSize(): Size {
        videoContainerSize?.let {
        } ?: run {
            return Size(width, height)
        }

        return videoContainerSize!!
    }

    private fun getVideoActualSize(): Size {
        videoActualSize?.let {
        } ?: run {
            return Size(width, height)
        }

        return videoActualSize!!
    }

}


