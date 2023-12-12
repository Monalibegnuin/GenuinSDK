package com.begenuin.library.common.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.begenuin.library.common.Utility
import com.begenuin.library.core.interfaces.OnSwipeGestureListener
import kotlin.math.abs


open class CustomLeftRightSwipeGesture(
    var context: Context,
    var swipeGesture: OnSwipeGestureListener
) : View.OnTouchListener {
    private var gestureDetector: GestureDetector? = null

    init {
        gestureDetector = GestureDetector(context, GestureListener(swipeGesture))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        Utility.printErrorLog("CustomLeftRightSwipeGesture: onTouch called")
        val isGestureHandled = gestureDetector?.onTouchEvent(event)!!
        return when (event.actionMasked) {
            MotionEvent.ACTION_UP -> {
                if (!isGestureHandled) {
                    swipeGesture.onSwipeCancel()
                }
                true
            }
            else -> {
                isGestureHandled
            }
        }
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
            Utility.printErrorLog("CustomLeftRightSwipeGesture : onFling calling")
            try {
                val diffY = e2.rawY - e1.rawY
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Utility.printErrorLog("OnRight..")
                            swipeGesture.onSwipeRight()
                        } else {
                            Utility.printErrorLog("OnLeft..")
                            swipeGesture.onSwipeLeft()
                        }
                    } else {
                        swipeGesture.onSwipeCancel()
                    }
                } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        swipeGesture.onSwipeBottom()
                    } else {
                        swipeGesture.onSwipeTop()
                    }
                } else {
                    swipeGesture.onSwipeCancel()
                }

            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffY) > abs(diffX)) {
                if (diffY < 0) {
                    swipeGesture.onScroll(abs(diffY))
                } else {
                    swipeGesture.onScrollDown(abs(diffY))
                }
            }
            return true
        }
    }
}


