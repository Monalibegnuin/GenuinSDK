package com.begenuin.library.views.adpters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.begenuin.begenuin.core.interfaces.IVideoEditorColorStyleChange
import com.begenuin.begenuin.data.model.EditorColorsModel
import com.begenuin.library.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily

class EditorColorAdapter(
    private var context: Context,
    private var colorsList: ArrayList<EditorColorsModel>,
    private var onTextColorChange: IVideoEditorColorStyleChange,
) :
    RecyclerView.Adapter<EditorColorAdapter.EditorColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.editor_color_list_item, parent, false)
        return EditorColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditorColorViewHolder, position: Int) {
        val model = getItem(position)
        holder.setBackground(model)
    }

    override fun getItemCount(): Int {
        return colorsList.size
    }


    inner class EditorColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var colorView: ShapeableImageView? = null
        private var llMain: LinearLayout? = null

        init {
            colorView = itemView.findViewById(R.id.viewColor)
            llMain = itemView.findViewById(R.id.llMain)

            colorView?.shapeAppearanceModel = colorView?.shapeAppearanceModel
                ?.toBuilder()
                ?.setAllCorners(CornerFamily.ROUNDED, 13F)!!.build()

            llMain?.setOnClickListener {
                changeBackground()
            }
        }

        private fun changeBackground() {
            colorsList.forEach { it.isColorSelectFromList = false }
            colorsList[absoluteAdapterPosition].isColorSelectFromList = true
            onTextColorChange.onSelectedTextColorChange(absoluteAdapterPosition)
            //setBackground(colorsList[absoluteAdapterPosition])
            notifyDataSetChanged()
        }

        fun setBackground(model: EditorColorsModel) {
            if (model.isColorSelectFromList && absoluteAdapterPosition != 0) {
                llMain?.background = context.resources.getDrawable(R.drawable.rounded_corner_border_white_no_fill, null)
            }
            else {
                llMain?.background = null
            }
            colorView?.setBackgroundColor(Color.parseColor(model.ColorHexa))
        }
    }

    fun getItem(pos: Int): EditorColorsModel {
        return colorsList[pos]
    }
}