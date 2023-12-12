package com.begenuin.library.common.customViews.draggableview

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.begenuin.begenuin.core.enums.VideoEditorTextAlignEnum
import com.begenuin.begenuin.data.model.EditorColorsModel
import com.begenuin.begenuin.ui.customview.draggableview.CustomBackgroundColorSpan
import com.begenuin.begenuin.ui.customview.draggableview.DraggableBaseCustomView
import com.begenuin.library.R
import com.begenuin.library.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.data.model.EditorFontModel

class DraggableTextView(
    context: Context?,
) : DraggableBaseCustomView(context) {
    private var textView: TextView? = null
    private var customSpan: CustomBackgroundColorSpan? = null
    private var padding = 0
    private var radius = 0
    private var selectedFontModel: EditorFontModel? = null
    private var selectedColorsModel: EditorColorsModel? = null


    init {
        textView = TextView(context)
        textView?.textSize = Constants.TEXT_EDITOR_FONT_DEFAULT_SIZE
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.addRule(CENTER_IN_PARENT)
        textView?.layoutParams = params
        textView?.gravity = Gravity.CENTER
        val padding = context?.resources?.getDimension(R.dimen.dimen_10dp)?.toInt()!!
        textView?.setPadding(padding, padding, padding, padding)
        //textView?.setBackgroundColor(Color.RED)
        super.addView(textView)
    }


    fun updateTextViewAttrs(
        textToUpdate: String,
        fontModel: EditorFontModel,
        colorsModel: EditorColorsModel,
    ) {
        this.selectedFontModel = fontModel
        this.selectedColorsModel = colorsModel
        textView?.gravity = Gravity.NO_GRAVITY

        if (!colorsModel.isBackgroundApplied) {
            addUpdateText(textToUpdate)
            Utility.setShadow(context,colorsModel.currentFontColor,textView!!)
            onTextAlignmentChange(fontModel.alignmentClickEnum, colorsModel.isBackgroundApplied)
        } else {
            initEditTextParams(textToUpdate)
        }

        setFontSize(fontModel.FontSize)
        setFontFromRes(fontModel)
        applyCurrentTextColor(colorsModel)
    }

    private fun setBackgroundValues() {
        padding = Utility.dpConversionForEditor(context, Constants.TEXT_BACKGROUND_PADDING)
        radius = Utility.dpConversionForEditor(context, Constants.TEXT_BACKGROUND_RADIUS)
        textView?.setShadowLayer(padding.toFloat(), 0f, 0f, 0)
        textView?.setPadding(padding, padding, padding, padding)
    }


    private fun initEditTextParams(textToUpdate: String) {
        resetValues(textToUpdate)
        setBackgroundValues()
        customSpan =
            CustomBackgroundColorSpan(Constants.textBackgroundColorArray[selectedColorsModel?.fontBackColorEnumValue!!],
                padding,
                radius)
        onTextAlignmentChange(selectedFontModel?.alignmentClickEnum!!, selectedColorsModel?.isBackgroundApplied!!)
       addUpdateText(textToUpdate)
        //remove underline
        textView?.clearComposingText()
        val str: Spannable = textView?.text as Spannable
        str.setSpan(customSpan, 0, textToUpdate.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    }

    private fun resetValues(textToUpdate: String) {
        val content = SpannableString(textToUpdate)
        if (customSpan != null)
            content.removeSpan(customSpan)
        padding = 0
        radius = 0
    }

    private fun onTextAlignmentChange(alignmentClickEnumValue: Int, isBackgroundSelected: Boolean) {
        try {
            var alignment = 0
            when (alignmentClickEnumValue) {
                VideoEditorTextAlignEnum.LEFT.alignmentType -> {
                    textView?.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    alignment = CustomBackgroundColorSpan.ALIGN_START
                }
                VideoEditorTextAlignEnum.RIGHT.alignmentType -> {
                    textView?.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    alignment = CustomBackgroundColorSpan.ALIGN_END
                }
                VideoEditorTextAlignEnum.CENTER.alignmentType -> {
                    textView?.gravity = Gravity.CENTER
                    alignment = CustomBackgroundColorSpan.ALIGN_CENTER
                }
            }
            if (isBackgroundSelected) {
                customSpan?.setAlignment(alignment)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private fun addUpdateText(textToUpdate: String) {
        if (!TextUtils.isEmpty(textToUpdate)) {
            textView?.setText(textToUpdate, TextView.BufferType.SPANNABLE)
            //textView?.text = textToUpdate
            //remove underline
            textView?.clearComposingText()
        }
    }

    private fun setFontSize(size: Float) {
        textView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    private fun setFontFromAsset(fontFamily: String) {
        textView?.typeface = Typeface.createFromAsset(context!!.assets, "fonts/$fontFamily")
    }

    private fun setFontFromRes(fontModel: EditorFontModel) {
        val typeface = Utility.setFontFromRes(context, fontModel.FontId)
        textView?.setTypeface(typeface, typeface!!.style)
    }

    private fun applyTextColor(colorsModel: EditorColorsModel) {
        textView?.setTextColor(Color.parseColor(colorsModel.ColorHexa))
    }

    private fun applyCurrentTextColor(colorsModel: EditorColorsModel) {
        textView?.setTextColor(colorsModel.currentFontColor)
    }

    fun getDraggableTextView(): TextView {
        return textView!!
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }
}