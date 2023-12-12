package com.begenuin.library.core.interfaces

import android.graphics.Bitmap
import com.begenuin.begenuin.data.model.EditorColorsModel
import com.begenuin.library.data.model.EditorFontModel

interface ICustomDialogInterface {
    fun onNegativeButtonClick()
    fun onPositiveButtonClick(textToUpdate: String, fontModel: EditorFontModel, colorsModel: EditorColorsModel, bmp: Bitmap)
    fun onDismissListener()
    fun onClearCurrentOverlay()
}