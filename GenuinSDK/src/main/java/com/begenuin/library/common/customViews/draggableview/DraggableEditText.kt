package com.begenuin.begenuin.ui.customview.draggableview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.begenuin.library.R
import com.begenuin.library.core.interfaces.IDraggableCustomView

class DraggableEditText(
    context: Context?,
    attrs: AttributeSet?,
    listener: IDraggableCustomView,
) : DraggableBaseCustomView(context) {
    var mainContainer: LinearLayout? = null
    private var editText: EditText? = null

    init {
        val headerView: View = View.inflate(context, R.layout.include_draggable_edit_text, null)
        mainContainer = headerView as LinearLayout
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        super.addView(mainContainer, params)
        editText = mainContainer?.findViewById<EditText>(R.id.edtSticker) as EditText
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun addText(textToUpdate: String) {
        editText?.text = textToUpdate.toEditable()
    }


    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

}