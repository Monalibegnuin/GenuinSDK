package com.begenuin.library.common.customViews

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.begenuin.library.R

class CustomEditTextWithError : LinearLayout {

    private var edtValue: EditText
    private var errorValue: TextView
    private var tvCounter: TextView
    private var borderView: LinearLayout
    private var labelTxt: TextView
    private var requiredTextView: TextView
    private var labelParent: RelativeLayout
    private var onTextChangeListener: CustomEditTextWithErrorListeners? = null

    var label: String?
        set(value) {
            labelTxt.text = value
        }
        get() {
            return labelTxt.text.toString()
        }
    var error: String?
        set(value) {
            errorValue.visibility = when (value) {
                null -> View.GONE
                "" -> View.GONE
                else -> View.VISIBLE
            }
            errorValue.text = value
            updateBorderColor()

        }
        get() {
            return errorValue.text.toString()
        }
    var text: String
        set(value) {
            edtValue.setText(value)
        }
        get() {
            return edtValue.text.toString()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    var edRequestFocus: Int = View.FOCUSABLE_AUTO
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            field = value
            edtValue.requestFocus(value)
        }

    var edtIsFocused: Boolean = false

    var textColor: Int = ContextCompat.getColor(context, R.color.colorBlack)
        set(value) {
            field = value
            edtValue.setTextColor(value)
        }

    var errorColor: Int = ContextCompat.getColor(context, R.color.red_F2545B)
        set(value) {
            field = value
            errorValue.setTextColor(value)
        }
    var hintColor: Int = ContextCompat.getColor(context, R.color.color_949494)
        set(value) {
            field = value
            edtValue.setHintTextColor(value)
        }

    var labelColor: Int = ContextCompat.getColor(context, R.color.color_111111)
        set(value) {
            field = value
            labelTxt.setTextColor(value)
        }

    var textCounter: String? = context.resources.getString(R.string.zero_24)
        set(value) {
            field = value
            tvCounter.text = value
        }
    var hint: String? = ""
        set(value) {
            edtValue.hint = value
            field = value
        }
    var edtBackground: Drawable? = ContextCompat.getDrawable(context, R.drawable.edit_text_e7e7_bg)
        set(value) {
            field = value
            borderView.background = value
        }

    fun edSelectionStart() = edtValue.selectionStart
    fun edIsFocused() = edtValue.isFocused
    fun edGetText() = edtValue.text

    var textSelection: Int = 0
        set(value) {
            field = value
            edtValue.setSelection(value)
        }
    var edtMinLines: Int = 1
        set(value) {
            field = value
            edtValue.minLines = value
        }
    var isTextRequired: Boolean = false
        set(value) {
            field = value
            if (value)
                requiredTextView.visibility = VISIBLE
            else
                requiredTextView.visibility = GONE
        }
    var edInputType: Int = InputType.TYPE_CLASS_TEXT
        set(value) {
            field = value
            edtValue.inputType = value
        }

    fun isSetTextRequired(value: Boolean) {
        if (value)
            requiredTextView.visibility = VISIBLE
        else
            requiredTextView.visibility = GONE
    }

    var edMaxLength: Int = Int.MAX_VALUE
        set(value) {
            field = value
            val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(value))
            edtValue.filters = filters
        }

    private fun updateBorderColor() {
        borderView.background = if (error.isNullOrEmpty()) ContextCompat.getDrawable(
            context,
            R.drawable.edit_text_e7e7_bg
        )
        else ContextCompat.getDrawable(context, R.drawable.error_border)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_custom_edit_text, this, true)
        edtValue = view.findViewById(R.id.edit_text)
        errorValue = view.findViewById(R.id.text_error)
        tvCounter = view.findViewById(R.id.tvHandleCounter)
        borderView = view.findViewById(R.id.edittext_parent)
        labelTxt = view.findViewById(R.id.label_txt)
        requiredTextView = view.findViewById(R.id.requiredText)
        labelParent = view.findViewById(R.id.label_parent)

        val styleable = context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomEditTextWithError,
            defStyleAttr,
            0
        )
        styleable.apply {
            hint = getString(R.styleable.CustomEditTextWithError_ed_hint)
            label = getString(R.styleable.CustomEditTextWithError_ed_label)
            error = getString(R.styleable.CustomEditTextWithError_ed_error)
            textColor =
                getColor(
                    R.styleable.CustomEditTextWithError_ed_textColor,
                    ContextCompat.getColor(context, R.color.colorBlack)
                )
            hintColor = getColor(
                R.styleable.CustomEditTextWithError_ed_hintColor,
                ContextCompat.getColor(context, R.color.color_949494)
            )
            labelColor = getColor(
                R.styleable.CustomEditTextWithError_ed_labelColor,
                ContextCompat.getColor(context, R.color.color_111111)
            )
            textCounter = getString(R.styleable.CustomEditTextWithError_ed_textCounter)
            edtBackground = getDrawable(R.styleable.CustomEditTextWithError_background)
            edtMinLines = getInt(R.styleable.CustomEditTextWithError_android_minLines, 1)
            edInputType = getInt(
                R.styleable.CustomEditTextWithError_android_inputType,
                InputType.TYPE_CLASS_TEXT
            )
            edMaxLength =
                getInt(R.styleable.CustomEditTextWithError_android_maxLength, Int.MAX_VALUE)
            recycle()
        }
        setTextChangeListeners()
    }

    interface CustomEditTextWithErrorListeners {
        fun beforeTextChange(s: CharSequence?, start: Int, count: Int, after: Int)
        fun onTextChange(s: CharSequence?, start: Int, count: Int, after: Int)
        fun afterTextChange(s: Editable?)
        fun setOnFocusChangeListener(v: View, hasFocus: Boolean)
    }

    fun setOnTextChangeListener(onTextChangeListener: CustomEditTextWithErrorListeners?) {
        this.onTextChangeListener = onTextChangeListener
    }

    private fun setTextChangeListeners() {
        edtValue.setOnFocusChangeListener { v, hasFocus ->
            onTextChangeListener?.setOnFocusChangeListener(v, hasFocus)
        }
        edtValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onTextChangeListener?.afterTextChange(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                onTextChangeListener?.beforeTextChange(s, start, count, after)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChangeListener?.onTextChange(s, start, before, count)
            }
        })
    }

    fun clearText() = edtValue.text.clear()
    fun getViewId(): View {
        return labelParent
    }
}
