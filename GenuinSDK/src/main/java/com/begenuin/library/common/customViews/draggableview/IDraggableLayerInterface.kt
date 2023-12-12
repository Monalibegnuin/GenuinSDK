package com.begenuin.library.common.customViews.draggableview

interface IDraggableLayerInterface {
    fun onTouchDown()
    fun onTouchMove(touchX : Float, touchY : Float, layer: Layer)
    fun onTouchRelease(touchX : Float, touchY : Float, layer: Layer)
    fun onTouchClick(layer: Layer)
    fun onCaptureImage(path : String, layer: Layer)
    fun onDeleteSticker(layer: Layer)
}