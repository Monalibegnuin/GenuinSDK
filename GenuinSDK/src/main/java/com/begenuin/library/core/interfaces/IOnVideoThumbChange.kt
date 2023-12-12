package com.begenuin.library.core.interfaces

import android.graphics.Bitmap

interface IOnVideoThumbChange {
    fun onCurrentThumbSelected(position: Int,bitmap: Bitmap)
}