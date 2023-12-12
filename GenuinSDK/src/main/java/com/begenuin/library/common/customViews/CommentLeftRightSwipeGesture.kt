package com.begenuin.library.common.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.begenuin.library.core.interfaces.OnSwipeGestureListener
import kotlin.math.abs


open class CommentLeftRightSwipeGesture(
    var context: Context,
    var swipeGesture: OnSwipeGestureListener
) : View.OnTouchListener {
    private var gestureDetector: GestureDetector? = null

    init {
        gestureDetector = GestureDetector(context, GestureListener(swipeGesture))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector?.onTouchEvent(event)!!
    }

    private class GestureListener(var swipeGesture: OnSwipeGestureListener) :
        GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.rawY - e1.rawY
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            swipeGesture.onSwipeRight()
                        } else {
                            swipeGesture.onSwipeLeft()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }
    }
}


