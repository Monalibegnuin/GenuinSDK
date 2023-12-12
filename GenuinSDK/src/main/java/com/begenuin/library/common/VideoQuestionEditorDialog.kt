package com.begenuin.library.common

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.begenuin.library.R
import com.begenuin.library.core.interfaces.IQuestionCustomDialogInterface
import com.begenuin.library.data.model.LottieAnimModel
import com.begenuin.library.data.model.QuestionModel
import com.begenuin.library.databinding.DialogQuestionEditVideoTextBinding
import com.google.android.material.slider.Slider

class VideoQuestionEditorDialog(
    private var context: Activity,
    private var questionModel: QuestionModel?,
    private var videoViewLocation: IntArray,
    private var callBack: IQuestionCustomDialogInterface,
    private var maxFontSize: Float,
    private var minFontSize: Float,
    private var currentFontSize: Float,
) : Dialog(context, R.style.text_sticker_dialog), View.OnClickListener,
    DialogInterface.OnCancelListener, DialogInterface.OnKeyListener {

    private var isViewTranslated = false
    private var isFromInit = false
    var dataBinding: DialogQuestionEditVideoTextBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_question_edit_video_text, null, false)
    init {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(dataBinding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        this.isFromInit = true

        Utility.printErrorLog("min:$minFontSize, max:$maxFontSize, current:$currentFontSize")

        dataBinding.seekBarLayout.fontSeekbar.apply {
            value = currentFontSize
            valueFrom = minFontSize
            valueTo = maxFontSize
        }
        setFontSize(currentFontSize)
        init()
    }

    private fun init() {
        setListener()
        setProfile()

        dataBinding.rlDialogParent.post {
            val locationScreen = IntArray(2)
            dataBinding.rlTop.getLocationOnScreen(locationScreen);

            val finalTop = videoViewLocation[1] - locationScreen[1]

            Utility.printErrorLog("~~~~ Ratio: locationScreen: " + locationScreen[0] + " " + locationScreen[1])
            Utility.printErrorLog("~~~~ Ratio: videoViewLocation: " + videoViewLocation[0] + " " + videoViewLocation[1])
            Utility.printErrorLog("~~~~ Ratio: pxToDp Location: " + Utility.pxToDp(locationScreen[1].toFloat(),
                context))
            Utility.printErrorLog("~~~~ Ratio: finalTop: $finalTop")
            Utility.printErrorLog("~~~~ Ratio: pxToDp videoViewLocation: " + Utility.pxToDp(
                videoViewLocation[1].toFloat(),
                context))

            val params = dataBinding.rlTop.layoutParams as RelativeLayout.LayoutParams
            params.topMargin = finalTop
            dataBinding.rlTop.layoutParams = params
        }
    }

    private fun setProfile() {
        dataBinding.llQuestionLayout.tvQuestion.text = questionModel?.question
        val owner = questionModel?.owner
        if (owner != null) {
            if (owner.isAvatar) {
                val res =
                    context.resources.getIdentifier(owner.profileImage, "raw", context.packageName)
                val color: Drawable =
                    ColorDrawable(context.resources.getColor(
                        LottieAnimModel.getMapData()[res]!!,
                        null))
                dataBinding.llQuestionLayout.ivOwner.setImageDrawable(color)
                dataBinding.llQuestionLayout.animationView.apply {
                    visibility = RelativeLayout.VISIBLE
                    setAnimation(res)
                    playAnimation()
                }
            } else {
                dataBinding.llQuestionLayout.animationView.visibility = RelativeLayout.GONE
                val path = owner.profileImageS.ifEmpty {
                    owner.profileImage
                }
                Utility.displayProfileImage(context as Activity, path, dataBinding.llQuestionLayout.ivOwner)
            }
            dataBinding.llQuestionLayout.tvQuestionAskedBy.text = String.format("@%s", owner.nickname)
        } else {
            dataBinding.llQuestionLayout.tvQuestionAskedBy.text = context.resources.getString(R.string.frequently_asked_question)
            dataBinding.llQuestionLayout.animationView.visibility = RelativeLayout.GONE
            dataBinding.llQuestionLayout.ivOwner.setImageResource(R.drawable.dunkinn_donuts_logo)
        }
    }

    private fun setListener() {
        dataBinding.tvDone?.setOnClickListener(this)
        dataBinding.seekBarLayout.flFontSeekbar?.setOnClickListener(this)
        dataBinding.ivBack?.setOnClickListener(this)
        this.setOnCancelListener(this)
        this.setOnKeyListener(this)

        dataBinding.seekBarLayout.fontSeekbar.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            Utility.printErrorLog("addonChange called.. $value $fromUser")
            currentFontSize = value
            setFontSize(value)
        })
        dataBinding.seekBarLayout.fontSeekbar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                Utility.printErrorLog("onStartTrackingTouch called.. ")
            }

            override fun onStopTrackingTouch(slider: Slider) {
                Utility.printErrorLog("onStopTrackingTouch called.. ")
                translateView()
            }
        })

        //translate font slider
        Handler(Looper.getMainLooper()).postDelayed({
            translateView()
        }, 1000)
    }

    private fun setFontSize(textSize: Float) {
        dataBinding.llQuestionLayout.tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        if (isFromInit) {
            isFromInit = false
            dataBinding.llQuestionLayout.tvQuestion.invalidate()
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvDone -> {
                onDoneClick()
            }
            R.id.flFontSeekbar -> {
                dataBinding.seekBarLayout.flFontSeekbar.bringToFront()
                translateView()
            }
            R.id.ivBack -> {
                clearAll()
                callBack.onNegativeButtonClick()
                dismiss()
            }
        }
    }

    private fun translateView() {
        val anim: TranslateAnimation
        if (!isViewTranslated) {
            anim = TranslateAnimation(0F, (-dataBinding.seekBarLayout.flFontSeekbar?.width!! / 2).toFloat() + 10F, 0F, 0F)
            anim.duration = 300
            anim.fillAfter = true
            dataBinding.seekBarLayout.flFontSeekbar?.startAnimation(anim)
        } else {
            anim = TranslateAnimation((-dataBinding.seekBarLayout.flFontSeekbar?.width!! / 2).toFloat() + 10F, 0F, 0F, 0F)
            anim.duration = 300
            anim.fillAfter = true
            dataBinding.seekBarLayout.flFontSeekbar?.startAnimation(anim)
        }
        isViewTranslated = !isViewTranslated
    }

    private fun onDoneClick() {
        loadBitmapFromView(dataBinding.llQuestionView)?.let {
            callBack.onPositiveButtonClick(maxFontSize, minFontSize, currentFontSize,
                it
            )
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
            val b = Bitmap.createBitmap(
                v.width, v.height,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            v.layout(0, 0, v.width, v.height)
            v.draw(c) // w ww .jav a  2  s.c om
            cropBitmapTransparency(b)
        }
    }

    private fun cropBitmapTransparency(sourceBitmap: Bitmap): Bitmap? {
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


    override fun onCancel(dialog: DialogInterface?) {
        Utility.printErrorLog("onCancelListener")
        callBack.onDismissListener()
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
        //isEditModeEnable = false
        maxFontSize = 0F
    }
}


