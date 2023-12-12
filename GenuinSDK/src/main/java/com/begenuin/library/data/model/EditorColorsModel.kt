package com.begenuin.begenuin.data.model

import android.graphics.drawable.GradientDrawable
import com.begenuin.begenuin.ui.customview.draggableview.CustomBackgroundColorSpan
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EditorColorsModel(
    @SerializedName("ColorId")
    var ColorId: String,
    @SerializedName("ColorName")
    var ColorName: String,
    @SerializedName("ColorHexa")
    var ColorHexa: String,
    @SerializedName("ColorRGB")
    var ColorRGB: String,
    var isColorSelectFromList: Boolean,
    var gradientDrawable: GradientDrawable = GradientDrawable(),
    var isBackgroundApplied: Boolean,
    var currentFontColor: Int,
    var fontBackColorEnumValue: Int,
    var customSpan: CustomBackgroundColorSpan?,
    var colorScrollPos: Int,
) : Serializable {


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other?.javaClass != javaClass) return false

        val model = other as EditorColorsModel
        return (ColorId == model.ColorId)
    }

    override fun hashCode(): Int {
        return ColorId.hashCode()
    }

    override fun toString(): String {
        return "Color-Id: $ColorId  IsSelected: $isColorSelectFromList  ColorHexa: $ColorHexa " + " currentFontColor: $currentFontColor " +
                " isBackgroundApplied $isBackgroundApplied"
    }
    /*BackgroundColor: $currentBackgroundColor*/
}
