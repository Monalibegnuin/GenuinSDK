package com.begenuin.begenuin.ui.customview.draggableview

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide

class DraggableImageViewFull(
    context: Context?,
) : DraggableBaseCustomView(context) {
    private var imageView: ImageView? = null

    init {
        imageView = ImageView(context)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        params.addRule(CENTER_IN_PARENT)
        imageView?.layoutParams = params
        super.addView(imageView)
    }

    fun setImageResourceBg(resource: Int){
        imageView?.setImageResource(resource)
        imageView?.adjustViewBounds = true
    }

    fun setImageResourceBg(path: String){
        imageView?.let { Glide.with(context).load(path).into(it) }
        imageView?.adjustViewBounds = true
    }

    fun setImageResourceBg(bmp: Bitmap){
        imageView?.setImageBitmap(bmp)
        imageView?.adjustViewBounds = true
    }
}