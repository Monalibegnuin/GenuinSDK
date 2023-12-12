package com.begenuin.library.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.*
import android.util.TypedValue
import android.view.*
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.begenuin.begenuin.core.enums.VideoEditorTextAlignEnum
import com.begenuin.begenuin.core.interfaces.IVideoEditorColorStyleChange
import com.begenuin.begenuin.core.interfaces.IVideoEditorFontStyleChange
import com.begenuin.begenuin.data.model.EditorColorsModel
import com.begenuin.library.views.adpters.EditorColorAdapter
import com.begenuin.library.views.adpters.EditorFontAdapter
import com.begenuin.begenuin.ui.customview.draggableview.CustomBackgroundColorSpan
import com.begenuin.library.R
import com.begenuin.library.common.Utility.getScreenWidthHeight
import com.begenuin.library.common.customViews.draggableview.Layer
import com.begenuin.library.core.enums.LayerType
import com.begenuin.library.core.enums.VideoEditorBackgroundEnum
import com.begenuin.library.core.interfaces.ICustomDialogInterface
import com.begenuin.library.data.model.EditorFontModel
import com.begenuin.library.databinding.DialogEditVideoTextBinding
import com.google.android.material.slider.Slider

@RequiresApi(Build.VERSION_CODES.R)
class VideoTextEditorDialog(
    private var context: Activity,
    private var textToUpdate: String?,
    private var videoViewLocation: IntArray,
    private var selectedFont: EditorFontModel?,
    private var selectedColor: EditorColorsModel?,
    private var layerType: LayerType,
    private var callBack: ICustomDialogInterface,
) : Dialog(context, R.style.text_sticker_dialog), View.OnClickListener,
    IVideoEditorColorStyleChange, IVideoEditorFontStyleChange, DialogInterface.OnCancelListener,
    DialogInterface.OnKeyListener {

    private var padding = 0
    private var radius = 0
    private var customSpan: CustomBackgroundColorSpan? = null
    private var backColor = -1
    private var fontColor = -1
    private var deviceWidth: IntArray? = IntArray(0)
    private var isEditModeEnable = false
    private var isBackgroundSelected = false
    private val colorWidth = 15F
    private val fontWidth = 15F
    private var isViewTranslated = false
    private var fontBackColorEnumValue = -1
    private var alignmentClickEnumValue = -1
    private var colorAdapter: EditorColorAdapter? = null
    private var fontAdapter: EditorFontAdapter? = null
    private var colorsList: ArrayList<EditorColorsModel>? = null
    private var fontsList: ArrayList<EditorFontModel>? = null
    private var backgroundArray = Constants.textBackgroundColorArray
    var _dialogBinding: DialogEditVideoTextBinding

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deviceWidth = getScreenWidthHeight(context)
        }
        colorsList = ArrayList()
        fontsList = ArrayList<EditorFontModel>()
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
       // this.setContentView(R.layout.dialog_edit_video_text)
        _dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_edit_video_text, null, false)
       // window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(_dialogBinding.root);
        setListener()
        updateDataFromScreen()
        setColorAdapter()
        setFontAdapter()
        initEditTextParams()
    }

    private fun showKeyboard() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        _dialogBinding.editVideoText?.requestFocus()
    }

    private fun updateDataFromScreen() {
        if (layerType == LayerType.TRANSCRIBE) {
            _dialogBinding.tvEditTranscript.visibility = View.VISIBLE
            _dialogBinding.llAlignmentOptions.visibility = View.GONE
        } else {
            _dialogBinding.llAlignmentOptions.visibility = View.VISIBLE
            _dialogBinding.tvEditTranscript.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(textToUpdate)) {
            isEditModeEnable = true

            fontBackColorEnumValue = selectedColor?.fontBackColorEnumValue!!
            backColor = backgroundArray[selectedColor?.fontBackColorEnumValue!!]
            fontColor = selectedColor?.currentFontColor!!
            isBackgroundSelected = selectedColor?.isBackgroundApplied!!
            alignmentClickEnumValue = selectedFont?.alignmentClickEnum!!

        } else {
            backColor = backgroundArray[0]
            fontColor = Color.WHITE

            //Default fontBackColor will be transperent and default alignment will be center so storing next value when user clicks on btns
            fontBackColorEnumValue = VideoEditorBackgroundEnum.TRANSPARENT.backgroundType
            alignmentClickEnumValue = VideoEditorTextAlignEnum.CENTER.alignmentType
            _dialogBinding.includeFontSlider.fontSeekbar.value = Constants.TEXT_EDITOR_FONT_DEFAULT_SIZE
        }
        _dialogBinding.includeFontSlider.fontSeekbar.valueFrom = Constants.MIN_SEEKBAR_VALUE
        _dialogBinding.includeFontSlider.fontSeekbar.valueTo = Constants.MAX_SEEKBAR_VALUE
    }

    private fun setListener() {
        _dialogBinding.tvDone?.setOnClickListener(this)
        _dialogBinding.ivTextAlignment?.setOnClickListener(this)
        _dialogBinding.rlVideoEditorMain?.setOnClickListener(this)
        _dialogBinding. ivTextBackground?.setOnClickListener(this)
        _dialogBinding.includeFontSlider.flFontSeekbar?.setOnClickListener(this)
        this.setOnCancelListener(this)
        this.setOnKeyListener(this)

        _dialogBinding.includeFontSlider.fontSeekbar.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            Utility.printErrorLog("addonChange called.. $value $fromUser")
            selectedFont?.FontSize = value
            setFontSize(value)
        })

        _dialogBinding.includeFontSlider.fontSeekbar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                Utility.printErrorLog("onStartTrackingTouch called.. ")
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Utility.printErrorLog("onStopTrackingTouch called.. ")
                translateView()
            }
        })
    }

    private fun setFontSize(value: Float) {
        _dialogBinding.editVideoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
        _dialogBinding.editVideoText.setSelection(_dialogBinding.editVideoText?.text?.length!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEditTextParams() {
        setSpannableBackground()

        if (isEditModeEnable) {
            customSpan = selectedColor?.customSpan
            setHintVisibility(textToUpdate!!)
            _dialogBinding.editVideoText.setText(textToUpdate)
            _dialogBinding. editVideoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedFont?.FontSize!!)
            _dialogBinding.editVideoText.setSelection(_dialogBinding.editVideoText.text?.length!!)
            Utility.setShadow(context, selectedColor?.currentFontColor!!, _dialogBinding.editVideoText)

            if (selectedColor?.isBackgroundApplied!!) {
                Utility.removeShadow(_dialogBinding.editVideoText)
                onTextBackgroundChange()
            } else {
                _dialogBinding.editVideoText.setTextColor(fontColor)
            }

            onTextAlignmentChange()
            _dialogBinding.includeFontSlider.fontSeekbar.value = selectedFont?.FontSize!!
            isEditModeEnable = false
        }

        _dialogBinding.editVideoText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
                setHintVisibility(charSequence.toString())
                //editVideoText.clearComposingText()
            }

            override fun afterTextChanged(s: Editable) {
                setHintVisibility(s.toString())
                if (customSpan != null && isBackgroundSelected) {
                    s.setSpan(customSpan, 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    Utility.setShadow(context, fontColor, _dialogBinding.editVideoText)
                }
            }
        })

        _dialogBinding.rlVideoEditorMain.post {
            val locationScreen = IntArray(2)
            _dialogBinding.rlTop.getLocationOnScreen(locationScreen);

            val finalTop = videoViewLocation[1] - locationScreen[1]
            val params = _dialogBinding.rlTop.layoutParams as RelativeLayout.LayoutParams
            params.topMargin = finalTop
            _dialogBinding.rlTop.layoutParams = params
            showKeyboard()
        }
    }


    private fun setHintVisibility(text: String) {
        if (text.isNotEmpty())
            _dialogBinding.tvHint.visibility = View.GONE
        else
            _dialogBinding.tvHint.visibility = View.VISIBLE
    }

    private fun setColorAdapter() {
        setRecyclerView(_dialogBinding.rvColorList!!)
        var objectPos = 0
        colorsList?.clear()
        //load color from assets/jsonFile
        colorsList?.addAll(Utility.getColorListFromAssets(context)!!)

        if (selectedColor == null) {
            colorsList!!.first().currentFontColor = fontColor
            selectedColor = colorsList?.first() as EditorColorsModel
            Utility.setShadow(context, fontColor, _dialogBinding.editVideoText)
        } else {
            objectPos = colorsList!!.indexOf(selectedColor)
            if (selectedColor?.isColorSelectFromList!!) {
                colorsList!![objectPos].isColorSelectFromList = true
            }
            colorsList!![objectPos].currentFontColor = fontColor
        }

        colorAdapter = EditorColorAdapter(context, colorsList!!, this)
        _dialogBinding.rvColorList?.adapter = colorAdapter

        loadRecyclerViewFromCenter(_dialogBinding.rvColorList!!, colorWidth)
        _dialogBinding.rvColorList?.scrollToPosition(selectedColor?.colorScrollPos!!)
    }

    private fun setFontAdapter() {
        setRecyclerView(_dialogBinding.rvFontList!!)
        var objectPos = 0

        //load font from res/fonts dir
        //fontsList?.addAll(Utility.getFontListFromJSONAssets(context))

        if (selectedFont == null) {
            fontsList!!.first().FontSize = Constants.TEXT_EDITOR_FONT_DEFAULT_SIZE
            fontsList!!.first().isSelected = true
            selectedFont = fontsList?.first() as EditorFontModel
        } else {
            objectPos = fontsList!!.indexOf(selectedFont)
            fontsList!![objectPos].isSelected = true
            fontsList!![objectPos].FontSize = selectedFont?.FontSize!!
        }

        val typeface = Utility.setFontFromRes(context, selectedFont?.FontId!!)
        _dialogBinding.editVideoText.setTypeface(typeface, typeface!!.style)

        fontAdapter = EditorFontAdapter(context, fontsList!!, this)
        _dialogBinding.rvFontList?.adapter = fontAdapter

        loadRecyclerViewFromCenter(_dialogBinding.rvFontList!!, fontWidth)
        _dialogBinding.rvFontList?.scrollToPosition(selectedFont?.fontScrollPos!!)

        //translate font slider
        Handler(Looper.getMainLooper()).postDelayed({
            translateView()
        }, 1000)
    }

    private fun setRecyclerView(recyclerView: RecyclerView) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun loadRecyclerViewFromCenter(recyclerView: RecyclerView, itemWidth: Float) {

        val padding = (deviceWidth!![0] / 2) - Utility.dpToPx(itemWidth, context).toInt()
        recyclerView.setPadding(
            padding,
            0,
            padding,
            0
        )
        recyclerView.invalidate()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvDone, R.id.editVideoText -> {
                onDoneClick()
            }
            R.id.ivTextBackground -> {
                Utility.removeShadow(_dialogBinding.editVideoText)
                getNextBackgroundColor()
                onTextBackgroundChange()
            }
            R.id.ivTextAlignment -> {
                getNextAlignmentValue()
                onTextAlignmentChange()
            }
            R.id.flFontSeekbar -> {
                translateView()
            }
        }
    }


    private fun translateView() {
        val anim: TranslateAnimation
        if (!isViewTranslated) {
            anim = TranslateAnimation(0F, (-_dialogBinding.includeFontSlider.flFontSeekbar?.width!! / 2).toFloat() + 10F, 0F, 0F)
            anim.duration = 300
            anim.fillAfter = true
            _dialogBinding.includeFontSlider.flFontSeekbar?.startAnimation(anim)
        } else {
            anim = TranslateAnimation((-_dialogBinding.includeFontSlider.flFontSeekbar?.width!! / 2).toFloat() + 10F, 0F, 0F, 0F)
            anim.duration = 300
            anim.fillAfter = true
            _dialogBinding.includeFontSlider.flFontSeekbar?.startAnimation(anim)
        }
        isViewTranslated = !isViewTranslated
    }

    private fun applyTextBackground() {
        var alignment = CustomBackgroundColorSpan.ALIGN_CENTER

        when (alignmentClickEnumValue) {
            VideoEditorTextAlignEnum.LEFT.alignmentType -> {
                alignment = CustomBackgroundColorSpan.ALIGN_START  //customSpan?.getAlignStart()!!
            }
            VideoEditorTextAlignEnum.RIGHT.alignmentType -> {
                alignment = CustomBackgroundColorSpan.ALIGN_END //customSpan?.getAlignEnd()!!
            }
            VideoEditorTextAlignEnum.CENTER.alignmentType -> {
                alignment = CustomBackgroundColorSpan.ALIGN_CENTER
            }
        }

        customSpan = CustomBackgroundColorSpan(
            backColor,
            padding,
            radius
        )
        customSpan?.setAlignment(alignment)

        _dialogBinding.editVideoText.text?.setSpan(
            customSpan,
            0,
            _dialogBinding.editVideoText.text?.length!!,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        _dialogBinding.editVideoText.setSelection(_dialogBinding.editVideoText.text?.length!!)

        _dialogBinding.editVideoText.invalidate()
    }

    private fun onTextAlignmentChange() {

        _dialogBinding.editVideoText.gravity = Gravity.NO_GRAVITY

        _dialogBinding.ivTextAlignment.setImageResource(0)
        try {

            when (alignmentClickEnumValue) {
                VideoEditorTextAlignEnum.LEFT.alignmentType -> {
                    _dialogBinding.ivTextAlignment?.setImageResource(R.drawable.ic_icon_paragraph_left_alignment)
                    _dialogBinding.editVideoText.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
                VideoEditorTextAlignEnum.RIGHT.alignmentType -> {
                    _dialogBinding.ivTextAlignment?.setImageResource(R.drawable.ic_icon_paragraph_right_alignment)
                    _dialogBinding.editVideoText.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
                VideoEditorTextAlignEnum.CENTER.alignmentType -> {
                    _dialogBinding.ivTextAlignment?.setImageResource(R.drawable.ic_icon_paragraph_center_alignment)
                    _dialogBinding.editVideoText.gravity = Gravity.CENTER
                }
            }
            if (customSpan != null) {
                removeTextBackground()

                if (isBackgroundSelected) {
                    applyTextBackground()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private fun getNextAlignmentValue() {
        when (alignmentClickEnumValue) {
            VideoEditorTextAlignEnum.CENTER.alignmentType -> {
                alignmentClickEnumValue = VideoEditorTextAlignEnum.LEFT.alignmentType
                selectedFont?.alignmentClickEnum = alignmentClickEnumValue
            }
            VideoEditorTextAlignEnum.LEFT.alignmentType -> {
                alignmentClickEnumValue = VideoEditorTextAlignEnum.RIGHT.alignmentType
                selectedFont?.alignmentClickEnum = alignmentClickEnumValue
            }
            VideoEditorTextAlignEnum.RIGHT.alignmentType -> {
                alignmentClickEnumValue = VideoEditorTextAlignEnum.CENTER.alignmentType
                selectedFont?.alignmentClickEnum = alignmentClickEnumValue
            }
        }
    }


    private fun onTextBackgroundChange() {
        removeTextBackground()

        _dialogBinding.ivTextBackground.setImageResource(0)

        when (fontBackColorEnumValue) {
            VideoEditorBackgroundEnum.TRANSPARENT.backgroundType -> {
                _dialogBinding.ivTextBackground?.setImageResource(R.drawable.ic_icon_text_no_background)
                isBackgroundSelected = false
            }

            VideoEditorBackgroundEnum.WHITE.backgroundType -> {
                _dialogBinding.ivTextBackground?.setImageResource(R.drawable.ic_icon_text_with_background)
                isBackgroundSelected = true
            }
            VideoEditorBackgroundEnum.BLACK.backgroundType -> {
                _dialogBinding.ivTextBackground?.setImageResource(R.drawable.ic_icon_text_with_background)
                isBackgroundSelected = true
            }
        }
        if (!isFromColor)
            fontColor = selectedColor?.currentFontColor!!

        checkFontBackColorSame()
        _dialogBinding.editVideoText.setTextColor(fontColor)

        selectedColor?.isBackgroundApplied = isBackgroundSelected

        if (isBackgroundSelected) {
            applyTextBackground()
        }
    }

    private fun getNextBackgroundColor() {
        when (fontBackColorEnumValue) {
            VideoEditorBackgroundEnum.TRANSPARENT.backgroundType -> {
                backColor = backgroundArray[1] //white
                fontBackColorEnumValue = VideoEditorBackgroundEnum.WHITE.backgroundType
                selectedColor?.fontBackColorEnumValue = fontBackColorEnumValue
            }
            VideoEditorBackgroundEnum.WHITE.backgroundType -> {
                backColor = backgroundArray[2] //black
                fontBackColorEnumValue = VideoEditorBackgroundEnum.BLACK.backgroundType
                selectedColor?.fontBackColorEnumValue = fontBackColorEnumValue
            }
            VideoEditorBackgroundEnum.BLACK.backgroundType -> {
                backColor = backgroundArray[0]
                fontBackColorEnumValue = VideoEditorBackgroundEnum.TRANSPARENT.backgroundType
                selectedColor?.fontBackColorEnumValue = fontBackColorEnumValue

            }
        }
    }

    private fun checkFontBackColorSame() {
        if (backColor == fontColor) {
            fontColor = when (fontColor) {
                Utility.getColorById(context, R.color.colorWhite) -> {
                    Utility.getColorById(context, R.color.video_editor_black)
                }
                else -> {
                    Utility.getColorById(context, R.color.colorWhite)
                }
            }
        }
    }


    private fun setSpannableBackground() {
        padding = Utility.dpConversionForEditor(context, Constants.TEXT_BACKGROUND_PADDING)
        radius = Utility.dpConversionForEditor(context, Constants.TEXT_BACKGROUND_RADIUS)
        _dialogBinding.editVideoText.setShadowLayer(padding.toFloat(), 0f, 0f, 0)
        _dialogBinding.editVideoText.setPadding(padding, padding, padding, padding)
    }

    private fun onDoneClick() {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if (_dialogBinding.editVideoText.text?.toString()?.isNotEmpty()!!) {
            selectedColor?.isBackgroundApplied = isBackgroundSelected
            selectedColor?.fontBackColorEnumValue = fontBackColorEnumValue
            selectedFont?.alignmentClickEnum = alignmentClickEnumValue
            val textToUpdate = _dialogBinding.editVideoText.text.toString()
            selectedColor?.currentFontColor = fontColor
            selectedColor?.customSpan = customSpan
            //editVideoText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            _dialogBinding.editVideoText.isCursorVisible = false
            _dialogBinding.editVideoText.clearComposingText()

            //removing selection
            _dialogBinding.editVideoText.setSelection(0)
            loadBitmapFromView(_dialogBinding.scrollView.getChildAt(0))?.let {
                callBack.onPositiveButtonClick(textToUpdate, selectedFont!!, selectedColor!!, it)
            }
        }else if(!TextUtils.isEmpty(textToUpdate)){
            callBack.onClearCurrentOverlay()
        } else {
            clearAll()
            callBack.onDismissListener()
        }
        dismiss()
    }

    private fun loadBitmapFromView(v: View): Bitmap? {
        return if (v.measuredHeight <= 0) {
            v.measure(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.layout(0, 0, v.measuredWidth, v.measuredHeight)
            v.draw(c)
            cropBitmapTransparency(b)
        } else {
            Utility.printErrorLog("Height:${v.height} Width:${v.width}")
            val b = Bitmap.createBitmap(
                v.width, v.height,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            v.layout(0, 0, v.width, v.height)
            v.draw(c)
            cropBitmapTransparency(b)
        }
    }

    open fun cropBitmapTransparency(sourceBitmap: Bitmap): Bitmap? {
        var minX = sourceBitmap.width
        var minY = sourceBitmap.height
        var maxX = -1
        var maxY = -1
        for (y in 0 until sourceBitmap.height) {
            for (x in 0 until sourceBitmap.width) {
                val alpha = sourceBitmap.getPixel(x, y) shr 24 and 255
                if (alpha > 0) // pixel is not 100% transparent
                {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        return if (maxX < minX || maxY < minY) null else Bitmap.createBitmap(
            sourceBitmap,
            minX,
            minY,
            maxX - minX + 1,
            maxY - minY + 1
        ) // Bitmap is entirely transparent

        // crop bitmap to non-transparent area and return:
    }

    private var isFromColor = false

    private fun removeTextBackground() {
        val editable = _dialogBinding.editVideoText.text
        if (customSpan != null && editable != null) {
            editable.removeSpan(customSpan)
        }
    }

    override fun onSelectedTextColorChange(position: Int) {
        this.isFromColor = true
        this.fontColor = 0

        val model = colorAdapter?.getItem(position)

        fontColor = convertColor(model?.ColorHexa!!)

        if (isBackgroundSelected) {
            Utility.removeShadow(_dialogBinding.editVideoText)
            val blackColor = Utility.getColorById(context, R.color.video_editor_black)
            val whiteColor = Utility.getColorById(context, R.color.colorWhite)

            when (fontColor) {
                blackColor -> {
                    backColor = backgroundArray[1]//white
                    fontBackColorEnumValue = VideoEditorBackgroundEnum.WHITE.backgroundType
                    selectedColor?.fontBackColorEnumValue = fontBackColorEnumValue
                }
                whiteColor -> {
                    backColor = backgroundArray[2] //black
                    fontBackColorEnumValue = VideoEditorBackgroundEnum.BLACK.backgroundType
                    selectedColor?.fontBackColorEnumValue = fontBackColorEnumValue
                }
                else -> {
                    _dialogBinding.editVideoText.setTextColor(fontColor)
                }
            }
            onTextBackgroundChange()

        } else {
            removeTextBackground()

            _dialogBinding.editVideoText.setTextColor(fontColor)
            Utility.setShadow(context, fontColor, _dialogBinding.editVideoText)
        }

        selectedColor?.isBackgroundApplied = isBackgroundSelected

        model.currentFontColor = fontColor

        loadRecyclerViewFromCenter(_dialogBinding.rvColorList!!, colorWidth)
        _dialogBinding.rvColorList?.scrollToPosition(position)

        model.colorScrollPos = position

        this.selectedColor = model

    }


    override fun onSelectedFont(position: Int) {
        val model = fontAdapter?.getItem(position)

        model?.FontSize = _dialogBinding.includeFontSlider.fontSeekbar.value

        val typeface = Utility.setFontFromRes(context, model?.FontId!!)
        _dialogBinding.editVideoText.setTypeface(typeface, typeface!!.style)

        setFontSize(model.FontSize)

        loadRecyclerViewFromCenter(_dialogBinding.rvFontList!!, fontWidth)
        _dialogBinding.rvFontList?.scrollToPosition(position)

        model.fontScrollPos = position

        this.selectedFont = model

    }

    override fun onCancel(dialog: DialogInterface?) {
        Utility.printErrorLog("onCancelListener")
        callBack.onDismissListener()
    }


    private fun convertColor(hexaColor: String): Int {
        if (TextUtils.isEmpty(hexaColor))
            return Color.WHITE
        return Color.parseColor(hexaColor)
    }


    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.isShowing) {
                clearAll()
                callBack.onNegativeButtonClick()
                dismiss()
                return false
            }

        }
        return false
    }

    private fun clearAll() {
        selectedFont = null
        selectedColor = null
        fontBackColorEnumValue = -1
        alignmentClickEnumValue = -1
        colorsList?.clear()
        fontsList?.clear()
        customSpan = null
        isEditModeEnable = false
        isBackgroundSelected = false
        isViewTranslated = false
    }
}


