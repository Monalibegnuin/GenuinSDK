package com.begenuin.library.views.adpters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.begenuin.begenuin.core.interfaces.IVideoEditorFontStyleChange
import com.begenuin.library.R
import com.begenuin.library.common.Utility
import com.begenuin.library.data.model.EditorFontModel


class EditorFontAdapter(
    private var context: Context,
    private var fontsList: ArrayList<EditorFontModel>,
    private var selectedFontListener: IVideoEditorFontStyleChange,
) :
    RecyclerView.Adapter<EditorFontAdapter.EditorFontViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorFontViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.editor_font_list_item, parent, false)
        return EditorFontViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditorFontViewHolder, position: Int) {
        val model = getItem(position)
        holder.setFont(model)
        holder.updateItemViewStyle(model, holder.absoluteAdapterPosition)
    }

    override fun getItemCount(): Int {
        return fontsList.size
    }


    inner class EditorFontViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView? = null
        var llMain: LinearLayout? = null

        init {
            textView = itemView.findViewById(R.id.tvFontToApply)
            llMain = itemView.findViewById(R.id.llEditorFontMain)

            llMain?.setOnClickListener {
                fontsList.forEach { it.isSelected = false }
                fontsList[absoluteAdapterPosition].isSelected = true
                selectedFontListener.onSelectedFont(absoluteAdapterPosition)
                notifyDataSetChanged()
            }
        }

        fun setFont(model: EditorFontModel?) {
            if (model == null)
                return
            val typeface = Utility.setFontFromRes(context, model.FontId)
            var style = typeface?.style
            when (style) {
                Typeface.BOLD -> {
                    style = Typeface.BOLD
                }
                Typeface.ITALIC -> {
                    style = Typeface.ITALIC
                }
                Typeface.NORMAL -> {
                    style = Typeface.NORMAL
                }
                Typeface.BOLD_ITALIC -> {
                    style = Typeface.BOLD_ITALIC
                }
                else -> {
                    style = Typeface.NORMAL
                }
            }
            textView?.setTypeface(typeface, style)
        }

        fun updateItemViewStyle(model: EditorFontModel, selectedPos: Int) {
            if (model.isSelected) {
                setFont(model)
                textView?.setTextColor(context.getColor(R.color.colorPrimary))
                llMain?.background =
                    context.resources.getDrawable(R.drawable.font_rounded_corner_border_white, null)
            } else {
                setFont(null)
                textView?.setTextColor(context.getColor(R.color.colorWhite))
                llMain?.background =
                    context.resources.getDrawable(R.drawable.font_rounded_corner_border_grey, null)
            }
        }

    }


    fun getItem(pos: Int): EditorFontModel {
        return this.fontsList[pos]
    }
}