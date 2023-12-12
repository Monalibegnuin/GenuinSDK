package com.begenuin.library.common.customViews

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.begenuin.library.R
import com.begenuin.library.common.Utility
import com.begenuin.library.data.model.LottieAnimModel
import com.begenuin.library.data.model.QuestionModel
import com.begenuin.library.databinding.IncludeAddQuestionCustomViewBinding
import com.begenuin.library.databinding.IncludeDraggableQuestionViewBinding
import java.text.MessageFormat
import kotlin.math.min
import kotlin.math.roundToInt

class QuestionView : RelativeLayout {

    private val desiredFontSize: Int = 25
    var questionViewMaxHeight = 0 //Constants.QUESTION_VIEW_MAX_HEIGHT
    private var maxFontSize: Int = 0
    private var currentFontSize: Int = 0
    private var minFontSize: Int = 0
    var maxWidth: Int = 0
    private var isCustomQuestionNeed = false
    var maxChar = 75
    lateinit var questionViewBindingCustom: IncludeAddQuestionCustomViewBinding
    lateinit var questionViewBinding: IncludeDraggableQuestionViewBinding

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initViews(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initViews(context, attrs)
    }


    private fun initViews(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.QuestionView)
            isCustomQuestionNeed =
                typedArray.getBoolean(R.styleable.QuestionView_addCustomQuestion, false)
        }
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.addRule(CENTER_IN_PARENT)
        questionViewBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.include_draggable_question_view, null, false)
        questionViewBindingCustom = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.include_add_question_custom_view, null, false)
        if (isCustomQuestionNeed) {
            super.addView(questionViewBinding.root, params)
        } else {
            super.addView(questionViewBindingCustom.root, params)
        }

        //questionView.layoutParams = params
    }


    fun updateTextViewAttrs(question: QuestionModel, currentFontValue: Float, maxFontValue: Float) {
        this.currentFontSize = currentFontValue.roundToInt()
        this.maxFontSize = maxFontValue.roundToInt()
        questionViewBinding.tvQuestion.text = question.question
        setProfile(question, questionViewBinding.ivOwner, questionViewBinding.tvQuestionAskedBy)
        findMaxFontFromMaxHeight(findQuestionViewMaxHeight(), maxFontValue)
    }

    fun updateTextViewAttrs(currentFontValue: Float, maxFontValue: Float) {
        this.currentFontSize = currentFontValue.roundToInt()
        this.maxFontSize = maxFontValue.roundToInt()
        findMaxFontFromMaxHeight(findQuestionViewMaxHeight(), maxFontValue)
    }

    fun setCustomQuestionData(question: QuestionModel, charCount: Int) {
        questionViewBindingCustom.edtAddQuestion.setText(question.question)
        questionViewBindingCustom.edtAddQuestion.setSelection(question.question.length)
        setCharCount(charCount)
        setProfile(question, questionViewBindingCustom.ivOwnerCustom, questionViewBindingCustom.tvLoggedInUserName)
//        tvLoggedInUserName.setText(question.owner.getName())
    }

    fun setCharCount(charLength: Int) {
        questionViewBindingCustom.tvCharCount.text = MessageFormat.format("{0}/{1}", charLength, maxChar)
    }

    private fun setProfile(questionModel: QuestionModel) {
        val owner = questionModel.owner
        if (owner != null) {
            if (owner.isAvatar) {
                val res =
                    context.resources.getIdentifier(owner.profileImage, "raw", context.packageName)
                val color: Drawable =
                    ColorDrawable(context.resources.getColor(
                        LottieAnimModel.getMapData()[res]!!,
                        null))
                questionViewBinding.ivOwner.setImageDrawable(color)
                questionViewBindingCustom.animationView.apply {
                    visibility = RelativeLayout.VISIBLE
                    setAnimation(res)
                    playAnimation()
                }
            } else {
                questionViewBindingCustom.animationView.visibility = RelativeLayout.GONE
                val path = owner.profileImageS.ifEmpty {
                    owner.profileImage
                }
                Utility.displayProfileImage(context as Activity, path, questionViewBinding.ivOwner)
            }
            questionViewBinding.tvQuestionAskedBy.text = String.format("@%s", owner.nickname)
        } else {
            questionViewBinding.tvQuestionAskedBy.text = context.resources.getString(R.string.frequently_asked_question)
            questionViewBindingCustom.animationView.visibility = RelativeLayout.GONE
            questionViewBinding.ivOwner.setImageResource(R.drawable.dunkinn_donuts_logo)
        }
    }

    private fun setProfile(questionModel: QuestionModel, imageView: ImageView, textView: TextView) {
        val owner = questionModel.owner
        if (owner != null) {
            if (owner.isAvatar) {
                val res =
                    context.resources.getIdentifier(owner.profileImage, "raw", context.packageName)
                val color: Drawable =
                    ColorDrawable(context.resources.getColor(LottieAnimModel.getMapData()[res]!!,
                        null))
                imageView.setImageDrawable(color)
                questionViewBindingCustom.animationView.apply {
                    visibility = RelativeLayout.VISIBLE
                    setAnimation(res)
                    playAnimation()
                }
            } else {
                questionViewBindingCustom.animationView.visibility = RelativeLayout.GONE
                val path = owner.profileImageS.ifEmpty {
                    owner.profileImage
                }
                Utility.displayProfileImage(context as Activity, path, imageView)
            }
            textView.text = String.format("@%s", owner.nickname)
        } else {
            textView.text = context.resources.getString(R.string.frequently_asked_question)
            questionViewBindingCustom.animationView.visibility = RelativeLayout.GONE
            imageView.setImageResource(R.drawable.dunkinn_donuts_logo)
        }
    }

    private fun findMaxFontFromMaxHeight(
        maxHeight: Int,
        selectedFont: Float,
    ) {
        questionViewBinding.tvQuestion.measure(0, 0)
        var currentHeight = 0
        var newMaxFontSize = selectedFont.toInt()
        var newCurrentFontSize = 0
        var newMinFontSize = 0
        do {
            newMaxFontSize--
            currentHeight =
                getHeight(questionViewBinding.tvQuestion.text.toString(), newMaxFontSize)
        } while (currentHeight > maxHeight)

        Utility.printErrorLog("finalFont:$newMaxFontSize, finalHeight: $currentHeight")

        newCurrentFontSize = min(newMaxFontSize, desiredFontSize)
        newMinFontSize = newCurrentFontSize / 2

        if (newMaxFontSize < desiredFontSize) {
            newCurrentFontSize = newMaxFontSize
        } else {
            newCurrentFontSize = desiredFontSize
        }

        this.maxFontSize = newMaxFontSize
        this.currentFontSize = newCurrentFontSize
        this.minFontSize = newMinFontSize

        Utility.printErrorLog("finalFont:$newMaxFontSize, finalHeight: $currentHeight, finalCurrentFont:$newCurrentFontSize, finalMinFont:$minFontSize")
    }

    private fun getHeight(text: String, textSize: Int): Int {
//        val padding = context.resources.getDimension(R.dimen.dimen_5dp).toInt()
//        tvQuestion.setPadding(padding, 0, padding, padding)
        questionViewBinding.tvQuestion.setText(text, TextView.BufferType.SPANNABLE)
        questionViewBinding.tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        val widthMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        questionViewBinding.tvQuestion.measure(widthMeasureSpec, heightMeasureSpec)
        Utility.printErrorLog("measuredWidth:${questionViewBinding.tvQuestion.measuredWidth}")
        return questionViewBinding.tvQuestion.measuredHeight
    }

    private fun findQuestionViewMaxHeight(): Int {
        val tempOld =
            questionViewMaxHeight - (questionViewBinding.tvQuestion.paddingTop - questionViewBinding.ivBottomAngle.paddingBottom) - (questionViewBinding.ivBottomAngle.height) - (questionViewBinding.llQuestionBox.bottom - questionViewBinding.ivBottomAngle.top)
        val temp = questionViewMaxHeight - questionViewBinding.llQuestionOwner.height - Utility.dpToPx(28F, context)
        Utility.printErrorLog("findQuestionViewMaxHeight: $temp tempOld:$tempOld maxWidth:$maxWidth")
        return temp.toInt()
    }

    private fun setQuestionHeight(height: Int) {
        val tvParams = questionViewBinding.tvQuestion?.layoutParams
        tvParams?.height = height
        questionViewBinding.tvQuestion.layoutParams = tvParams
    }
}