package com.begenuin.library.data.model

import com.begenuin.library.core.enums.LayerType
import java.io.Serializable

data class ImageStickerModel(
    var viewId: Int = 0,
    var viewX: Float = 0F,
    var viewY: Float = 0F,
    var filePath: String? = null,
    var gifFilePath: String? = null,
    var scaleFactor: Double = 0.0,
    var rotationAngel: Double = 0.0,
    var width: Float = 0F,
    var height: Float = 0F,
    var type: LayerType = LayerType.IMAGE,
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        val model = other as ImageStickerModel
        return (viewId == model.viewId)
    }

    override fun toString(): String {
        return "viewId:$viewId viewX:$viewX viewY:$viewY filePath:$filePath" //view:$dragBaseView"
    }
}