package com.begenuin.library.common.customViews.draggableview

import android.content.Context
import android.widget.ImageView
import com.begenuin.begenuin.ui.customview.draggableview.DraggableBaseCustomView
import com.begenuin.library.R
import com.begenuin.library.data.model.TopicModel

class DraggableImageView(
    context: Context?,
) : DraggableBaseCustomView(context) {
    private var imageView: ImageView? = null

    init {
        imageView = ImageView(context)
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.addRule(CENTER_IN_PARENT)
        imageView?.layoutParams = params
        val padding = context?.resources?.getDimension(R.dimen.dimen_10dp)?.toInt()!!
        imageView?.setPadding(padding, padding, padding, padding)
        //textView?.setBackgroundColor(Color.RED)
        super.addView(imageView)
    }

    fun setImage(topicModel: TopicModel){
        imageView?.setImageResource(topicModel.sticker)
    }

    fun setImageColor(){
        imageView?.setImageResource(R.color.color_bg_2)
    }
}