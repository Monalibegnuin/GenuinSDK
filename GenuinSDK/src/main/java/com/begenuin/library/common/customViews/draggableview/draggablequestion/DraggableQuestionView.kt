package com.begenuin.library.common.customViews.draggableview.draggablequestion

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.begenuin.begenuin.ui.customview.draggableview.DraggableBaseCustomView
import com.begenuin.library.R
import com.begenuin.library.common.Utility
import com.begenuin.library.data.model.LottieAnimModel
import com.begenuin.library.data.model.QuestionModel
import com.begenuin.library.databinding.IncludeDraggableQuestionViewBinding
import kotlin.math.min
import kotlin.math.roundToInt

class DraggableQuestionView(context: Context?) :
    DraggableBaseCustomView(context) {


    private val desiredFontSize: Int = 25
    private var questionViewMaxHeight = 0 //Constants.QUESTION_VIEW_MAX_HEIGHT
    var maxFontSize: Int = 0
    var currentFontSize: Int = 0
    var minFontSize: Int = 0
    private var viewBinding: IncludeDraggableQuestionViewBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.include_draggable_question_view, null, false)

    constructor(
        context: Context,
        viewMaxHeight: Int,
    ) : this(context) {
        questionViewMaxHeight = viewMaxHeight //30% of height of video view
    }


    init {
        // window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.addRule(CENTER_IN_PARENT)
        super.addView(viewBinding.root, params)
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun updateTextViewAttrs(question: QuestionModel, currentFontValue: Float, maxFontValue: Float) {
        this.currentFontSize = currentFontValue.roundToInt()
        this.maxFontSize = maxFontValue.roundToInt()

        // tvQuestion.setText(R.string.para) //question.question
        viewBinding.tvQuestion.text = question.question
        setProfile(question)
        findMaxFontFromMaxHeight(findQuestionViewMaxHeight(), maxFontValue)
    }

    fun updateTextViewAttrs(currentFontValue: Float) {
        this.currentFontSize = currentFontValue.roundToInt()
        viewBinding.tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontValue)
    }

    private fun setProfile(questionModel: QuestionModel) {
        val owner = questionModel.owner
        if (owner != null) {
            if (owner.isAvatar) {
                val res = resources.getIdentifier(owner.profileImage,
                    "raw", context.packageName)
                val color: Drawable = ColorDrawable(
                    resources.getColor(
                        LottieAnimModel.getMapData()[res]!!, null))
                viewBinding.ivOwner.setImageDrawable(color)
                viewBinding.animationView.visibility = VISIBLE
                viewBinding.animationView.setAnimation(res)
                viewBinding.animationView.playAnimation()
            } else {
                viewBinding.animationView.visibility = GONE
                if (!TextUtils.isEmpty(owner.profileImageS)) {
                    Utility.displayProfileImage(context as Activity,
                        owner.profileImageS,
                        viewBinding.ivOwner)
                } else {
                    Utility.displayProfileImage(context as Activity,
                        owner.profileImage,
                        viewBinding.ivOwner)
                }
            }
            viewBinding.tvQuestionAskedBy.text = String.format("@%s", owner.nickname)
        } else {
            viewBinding.tvQuestionAskedBy.text = context.resources.getString(R.string.frequently_asked_question)
            viewBinding.animationView.visibility = GONE
            viewBinding.ivOwner.setImageResource(R.drawable.dunkinn_donuts_logo)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun findMaxFontFromMaxHeight(
        maxHeight: Int,
        selectedFont: Float,
    ) {
        viewBinding.tvQuestion.measure(0, 0)
        var currentHeight = 0
        var newMaxFontSize = selectedFont.toInt()
        var newCurrentFontSize = 0
        var newMinFontSize = 0
        val deviceWidth =
            (Utility.getScreenWidthHeight(context as Activity)!![0]) - Utility.dpToPx(120F, context)
        do {
            newMaxFontSize--
            currentHeight =
                getHeight( viewBinding.tvQuestion.text.toString(), newMaxFontSize, deviceWidth.toInt())
            //Utility.printErrorLog("MeasuredHeight: $currentHeight")
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

        viewBinding.tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSize.toFloat())
    }

    private fun getHeight(text: String, textSize: Int, deviceWidth: Int): Int {
        viewBinding.tvQuestion.apply {
            setText(text, TextView.BufferType.SPANNABLE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            measure(widthMeasureSpec, heightMeasureSpec)
        }
        return  viewBinding.tvQuestion.measuredHeight
    }

    private fun findQuestionViewMaxHeight(): Int {
        val tempOld =
            questionViewMaxHeight - ( viewBinding.tvQuestion.paddingTop -  viewBinding.ivBottomAngle.paddingBottom) - ( viewBinding.ivBottomAngle.height) - ( viewBinding.llQuestionBox.bottom -  viewBinding.ivBottomAngle.top)
        val temp = questionViewMaxHeight -  viewBinding.llQuestionOwner.height - Utility.dpToPx(10F, context)
        Utility.printErrorLog("findQuestionViewMaxHeight: $temp tempOld:$tempOld")
        return temp.toInt()
    }

    private fun setQuestionHeight(height: Int) {
        val tvParams =  viewBinding.tvQuestion?.layoutParams
        tvParams?.height = height
        viewBinding.tvQuestion.layoutParams = tvParams
    }
}