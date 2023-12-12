package com.begenuin.library.core.interfaces

import com.begenuin.begenuin.ui.customview.draggableview.DraggableBaseCustomView

interface IDraggableCustomView {
    fun onTouchDown(view: DraggableBaseCustomView)
    fun onTouchClick(dx: Float, dy: Float, view: DraggableBaseCustomView)
    fun onTouchMove(dx: Float, dy: Float, view: DraggableBaseCustomView)
    fun onTouchRelease(
        dx: Float,
        dy: Float,
        view: DraggableBaseCustomView
    )

    fun onRotateView(dx: Float, dy: Float, rotation: Float, view: DraggableBaseCustomView)
    fun onScaleView(scaleX:Float,scaleY:Float,scaleFactor:Float,view: DraggableBaseCustomView)
    fun onRotateBegin(view: DraggableBaseCustomView)
    fun onScaleBegin(view: DraggableBaseCustomView)
}