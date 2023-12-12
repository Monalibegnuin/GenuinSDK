package com.begenuin.library.core.interfaces

interface OnSwipeGestureListener {
    fun onSwipeRight()

    fun onSwipeLeft()

    fun onScroll(diffY: Float)

    fun onScrollDown(diffY: Float)

    fun onSwipeTop()

    fun onSwipeBottom()

    fun onSwipeCancel()
}