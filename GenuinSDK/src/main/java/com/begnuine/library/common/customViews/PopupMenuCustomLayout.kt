package com.begnuine.library.common.customViews

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.begnuine.library.common.Utility.dpToPx
import com.begnuine.library.common.Utility.getScreenWidthHeight
import com.begnuine.library.R

class PopupMenuCustomLayout {
    private var onClickListener: PopupMenuCustomOnClickListener? = null
    private val context: Context
    private val popupWindow: PopupWindow
    private var rLayoutId = 0
    private val popupView: View

    constructor(context: Context, rLayoutId: Int, onClickListener: PopupMenuCustomOnClickListener) {
        this.context = context
        this.onClickListener = onClickListener
        this.rLayoutId = rLayoutId
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(rLayoutId, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.elevation = 10f
        val linearLayout = popupView as LinearLayout
        for (i in 0 until linearLayout.childCount) {
            val v = linearLayout.getChildAt(i)
            v.setOnClickListener { v1: View ->
                onClickListener.onClick(v1.id)
                popupWindow.dismiss()
            }
        }
    }

    constructor(
        context: Activity,
        popupView: View,
        onDismissListener: PopupWindow.OnDismissListener?
    ) {
        // For Feed Options
        this.context = context
        this.popupView = popupView
        val width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        val height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val c = ConstraintSet()
        val dimens = getScreenWidthHeight(context)
        val defaultHalfScreenWidth = dimens!![0] / 2 - dpToPx(16f, context).toInt()
        val maxHalfScreenHeight = dimens[1] / 2
        c.constrainWidth(R.id.cvFeedOptions, defaultHalfScreenWidth)
        c.constrainMaxHeight(R.id.cvFeedOptions, maxHalfScreenHeight)
        (popupView as ConstraintLayout).setConstraintSet(c)
        popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.setOnDismissListener(onDismissListener)
        popupWindow.elevation = 10f
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    // This function will display failed video count in popup window(Retry All({count}))
    fun setRetryCountsForDialog(count: Int) {
        val tvRetry = popupView.findViewById<TextView>(R.id.tvRetry)
        val tvDelete = popupView.findViewById<TextView>(R.id.tvDelete)
        tvRetry.text = context.resources.getString(R.string.retry_all_count, count)
        tvDelete.text = context.resources.getString(R.string.clear_all_count, count)
    }

    fun setReportDeleteDialog() {
        val tvRetry = popupView.findViewById<TextView>(R.id.tvRetry)
        val tvDelete = popupView.findViewById<TextView>(R.id.tvDelete)
        val ivRetry = popupView.findViewById<ImageView>(R.id.ivRetry)
        tvRetry.text = context.resources.getString(R.string.report)
        tvDelete.text = context.resources.getString(R.string.delete)
        ivRetry.imageTintList =
            ColorStateList.valueOf(context.resources.getColor(R.color.red_F2545B, null))
        tvRetry.setTextColor(context.resources.getColor(R.color.red_F2545B, null))
        ivRetry.setImageResource(R.drawable.ic_report_comment)
    }

    fun setOnlyReportDialog() {
        val llDelete = popupView.findViewById<LinearLayout>(R.id.llDelete)
        val llDivider = popupView.findViewById<LinearLayout>(R.id.llDivider)
        llDivider.visibility = View.GONE
        llDelete.visibility = View.GONE
        val tvRetry = popupView.findViewById<TextView>(R.id.tvRetry)
        tvRetry.text = context.resources.getString(R.string.report)
        val ivRetry = popupView.findViewById<ImageView>(R.id.ivRetry)
        ivRetry.setImageResource(R.drawable.ic_report_comment)
        ivRetry.imageTintList =
            ColorStateList.valueOf(context.resources.getColor(R.color.red_F2545B, null))
        tvRetry.setTextColor(context.resources.getColor(R.color.red_F2545B, null))
    }

    fun setOnlyDeleteDialog() {
        val tvDelete = popupView.findViewById<TextView>(R.id.tvDelete)
        tvDelete.text = context.resources.getString(R.string.delete)
        val llRetry = popupView.findViewById<LinearLayout>(R.id.llRetry)
        val llDivider = popupView.findViewById<LinearLayout>(R.id.llDivider)
        llDivider.visibility = View.GONE
        llRetry.visibility = View.GONE
    }

    fun setAnimationStyle(animationStyle: Int) {
        popupWindow.animationStyle = animationStyle
    }

    fun show() {
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }

    fun show(anchorView: View?) {
        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    interface PopupMenuCustomOnClickListener {
        fun onClick(menuItemId: Int)
    }
}