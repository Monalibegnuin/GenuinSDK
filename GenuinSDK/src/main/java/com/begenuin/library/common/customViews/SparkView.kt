package com.begenuin.library.common.customViews

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.begenuin.library.R

class SparkView : RelativeLayout {
    private lateinit var sparkView: View
    private lateinit var ivEmptyBulb: ImageView
    private lateinit var ivFilledBulb: ImageView
    private var isCurrentlySparked = false
    var size = 0
    constructor(context: Context) : super(context) {

    }

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

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initViews(context, attrs)
    }

    fun initViews(context: Context, attrs: AttributeSet) {
        sparkView = inflate(context, R.layout.spark_view, null)
        ivEmptyBulb = sparkView.findViewById(R.id.ivEmptyBulb)
        ivFilledBulb = sparkView.findViewById(R.id.ivFilledBulb)

        //get custom attributes
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SparkView)
        size = typedArray.getDimensionPixelSize(R.styleable.SparkView_size, resources.getDimensionPixelSize(R.dimen.dimen_32dp))
        val resourceFilledBulb = typedArray.getResourceId(R.styleable.SparkView_filled_bulb_resource, R.drawable.ic_bulb_filled_feed)
        val resourceEmptyBulb = typedArray.getResourceId(R.styleable.SparkView_empty_bulb_resource, R.drawable.ic_bulb_empty_feed)

        //set custom attributes
        ivFilledBulb.setImageResource(resourceFilledBulb)
        ivEmptyBulb.setImageResource(resourceEmptyBulb)

        typedArray.recycle()
        super.addView(sparkView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val emptyBulbParams = ivEmptyBulb.layoutParams
        emptyBulbParams.width = size
        emptyBulbParams.height = size
        ivEmptyBulb.layoutParams = emptyBulbParams

        val filledBulbParams = ivFilledBulb.layoutParams
        filledBulbParams.width = (size * 0.75).toInt()
        filledBulbParams.height = (size * 0.75).toInt()
        ivFilledBulb.layoutParams = filledBulbParams
    }

    fun toggleSpark(isAnim: Boolean) {
        if (isCurrentlySparked) {
            setUnSpark(isAnim)
        } else {
            setSpark(isAnim)
        }
     }

    fun setSpark() {
        ivEmptyBulb.alpha = 0f
        ivEmptyBulb.scaleX = 0.75f
        ivEmptyBulb.scaleY = 0.75f

        ivFilledBulb.alpha = 1f
        ivFilledBulb.scaleX = 1.33f
        ivFilledBulb.scaleY = 1.33f
        isCurrentlySparked = true
    }

    fun setSpark(isAnim: Boolean) {
        if (isCurrentlySparked) return
        if (!isAnim) {
            setSpark()
        } else {
            isCurrentlySparked = true
            clearAnim()
            val animatorSet = AnimatorSet()
            val scaleUpY = ObjectAnimator.ofFloat(ivFilledBulb, "scaleY", 1f, 1.33f)
            val scaleUpX = ObjectAnimator.ofFloat(ivFilledBulb, "scaleX", 1f, 1.33f)
            val fadeIn = ObjectAnimator.ofFloat(ivFilledBulb, "alpha", 0f, 1f)

            val scaleDownY = ObjectAnimator.ofFloat(ivEmptyBulb, "scaleY", 1f, 0.75f)
            val scaleDownX = ObjectAnimator.ofFloat(ivEmptyBulb, "scaleX", 1f, 0.75f)
            val fadeOut = ObjectAnimator.ofFloat(ivEmptyBulb, "alpha", 1f, 0f)

            animatorSet.playTogether(scaleUpX, scaleUpY, scaleDownX, scaleDownY, fadeIn, fadeOut)
            animatorSet.duration = 300
            animatorSet.start()
        }
    }

    fun setUnSpark() {
        ivEmptyBulb.alpha = 1f
        ivEmptyBulb.scaleX = 1f
        ivEmptyBulb.scaleY = 1f

        ivFilledBulb.alpha = 0f
        ivFilledBulb.scaleX = 1f
        ivFilledBulb.scaleY = 1f
        isCurrentlySparked = false
    }

    private fun clearAnim() {
        if (isCurrentlySparked) {
            /*ivEmptyBulb.animate().scaleX(1f).scaleY(1f).translationX(0f).translationY(0f).alpha(1f)
            ivFilledBulb.animate().scaleX(1f).scaleY(1f).translationX(0f).translationY(0f).alpha(0f)*/
            ivEmptyBulb.alpha = 1f
            ivEmptyBulb.scaleX = 1f
            ivEmptyBulb.scaleY = 1f

            ivFilledBulb.alpha = 0f
            ivFilledBulb.scaleX = 1f
            ivFilledBulb.scaleY = 1f

        } else {
            /*ivEmptyBulb.animate().scaleX(0.75f).scaleY(0.75f).translationX(0f).translationY(0f).alpha(0f)
            ivFilledBulb.animate().scaleX(1.33f).scaleY(1.33f).translationX(0f).translationY(0f).alpha(1f)*/
            ivEmptyBulb.alpha = 0f
            ivEmptyBulb.scaleX = 0.75f
            ivEmptyBulb.scaleY = 0.75f

            ivFilledBulb.alpha = 1f
            ivFilledBulb.scaleX = 1.33f
            ivFilledBulb.scaleY = 1.33f
        }
    }

    fun setUnSpark(isAnim: Boolean) {
        if (!isAnim) {
            setUnSpark()
        } else {
            isCurrentlySparked = false
            clearAnim()
            val animatorSet = AnimatorSet()
            val scaleUpY = ObjectAnimator.ofFloat(ivEmptyBulb, "scaleY", 0.75f, 1f)
            val scaleUpX = ObjectAnimator.ofFloat(ivEmptyBulb, "scaleX", 0.75f, 1f)
            val fadeIn = ObjectAnimator.ofFloat(ivEmptyBulb, "alpha", 0f, 1f)

            val scaleDownY = ObjectAnimator.ofFloat(ivFilledBulb, "scaleY", 1.33f, 1f)
            val scaleDownX = ObjectAnimator.ofFloat(ivFilledBulb, "scaleX", 1.33f, 1f)
            val fadeOut = ObjectAnimator.ofFloat(ivFilledBulb, "alpha", 1f, 0f)

            animatorSet.playTogether(scaleUpX, scaleUpY, scaleDownX, scaleDownY, fadeIn, fadeOut)
            animatorSet.duration = 300
            animatorSet.start()
        }
    }
}