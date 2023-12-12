package com.begenuin.library.core.interfaces

import android.graphics.Bitmap


interface IQuestionCustomDialogInterface {
    fun onNegativeButtonClick()
    fun onPositiveButtonClick(
        maxFontSize: Float,
        minFontSize: Float,
        currentFontValue: Float,
        bmp: Bitmap,
    )

    fun onDismissListener()
}