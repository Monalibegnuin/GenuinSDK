package com.begenuin.library.data.model

import com.begenuin.begenuin.core.enums.VideoEditorTextAlignEnum
import com.begenuin.library.common.Constants
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EditorFontModel(
    @SerializedName("FontId")
    var FontId: Int = 0,
    @SerializedName("FontName")
    var FontName: String = "",
    @SerializedName("FontURL")
    var FontURL: String = "",
    var isSelected: Boolean = false,
    var FontSize: Float = Constants.TEXT_EDITOR_FONT_DEFAULT_SIZE,
    var alignmentClickEnum: Int = VideoEditorTextAlignEnum.CENTER.alignmentType,
    var fontScrollPos:Int =0
) :
    Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other?.javaClass != javaClass) return false

        val model = other as EditorFontModel
        return (FontId == model.FontId)
    }

    override fun hashCode(): Int {
        return FontId.hashCode()
    }

    override fun toString(): String {
        return "FontId: $FontId  IsSelected: $isSelected  FontName: $FontName Font-Size:$FontSize"
    }


}
