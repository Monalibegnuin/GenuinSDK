package com.begenuin.library.common.customViews.changecover

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.begenuin.library.R
import com.begenuin.library.common.Utility
import com.begenuin.library.common.customViews.RangeSeekBarView
import com.begenuin.library.core.interfaces.IOnVideoThumbChange
import com.begenuin.library.core.interfaces.SeekBarRangeChanged
import com.begenuin.library.databinding.VideoMultiChangeCoverViewBinding

open class CustomVideoCoverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val TAG = "CustomChangeThumbnilCoverView"
    private var mMaxWidth = 0
    var mRangeSeekBarView: RangeSeekBarView? = null

    //var videoSeekBar: AppCompatSeekBar? = null
    private var averagePxMs = 0f
    var mVideoThumbAdapter: VideoChangeCoverAdapter? = null
    private var isFromRestore = false
    var mLeftProgressPos = 0L
    var mRightProgressPos = 10000L
    var mRedProgressBarPos = 0L
    private var rangeListener: SeekBarRangeChanged? = null
    private var iOnVideoThumbChange: IOnVideoThumbChange? = null
    var videoMultiChangeCoverViewBinding: VideoMultiChangeCoverViewBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),R.layout.video_multi_change_cover_view, null, false)

    init {

        val rvFrames = findViewById<RecyclerView>(R.id.rvFrames)
        rvFrames.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mVideoThumbAdapter = VideoChangeCoverAdapter(context)
        rvFrames.adapter = mVideoThumbAdapter
        //mMaxWidth = DeviceUtil.getDeviceWidth() - Utility.dpToPx(30f, context).toInt()
        mMaxWidth = 300
        super.addView(videoMultiChangeCoverViewBinding.root)
    }

    open fun initRangeSeekbar(context: Context) {
        if (mRangeSeekBarView != null) {
            videoMultiChangeCoverViewBinding.seekBarLayout.removeAllViews()
            mRangeSeekBarView = null
        }
        mRangeSeekBarView = RangeSeekBarView(context, mLeftProgressPos, mRightProgressPos, false, false)
        mRangeSeekBarView?.selectedMinValue = mLeftProgressPos
        mRangeSeekBarView?.selectedMaxValue = mRightProgressPos
        mRangeSeekBarView?.setStartEndTime(mLeftProgressPos, mRightProgressPos)
        mRangeSeekBarView?.setMinShootTime(1000L)
        mRangeSeekBarView?.isNotifyWhileDragging = true
        mRangeSeekBarView?.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener)
        videoMultiChangeCoverViewBinding.seekBarLayout.addView(mRangeSeekBarView)
        averagePxMs = mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos)
    }

    private fun getRestoreState(): Boolean {
        return isFromRestore
    }

    open fun setRestoreState(fromRestore: Boolean) {
        isFromRestore = fromRestore
    }

    open fun setRangeListener(rangeListener: SeekBarRangeChanged) {
        this.rangeListener = rangeListener
    }

    open fun setOnThmbImageSelected(iOnVideoThumbChange: IOnVideoThumbChange) {
        this.iOnVideoThumbChange = iOnVideoThumbChange
    }

    private val mOnRangeSeekBarChangeListener =
        RangeSeekBarView.OnRangeSeekBarChangeListener { bar, minValue, maxValue, action, isMin, pressedThumb ->
            Utility.showLog(
                TAG,
                "-----minValue----->>>>>>$minValue"
            )
            Utility.showLog(
                TAG,
                "-----maxValue----->>>>>>$maxValue"
            )
            val scrollPos: Long = 0
            mLeftProgressPos = minValue + scrollPos
            mRedProgressBarPos = mLeftProgressPos
            mRightProgressPos = maxValue + scrollPos
            videoMultiChangeCoverViewBinding.videoSeekBar!!.progress = mRedProgressBarPos.toInt()
            val isLeft = pressedThumb == RangeSeekBarView.Thumb.MIN
            Utility.showLog(
                TAG,
                "-----mLeftProgressPos----->>>>>>$mLeftProgressPos"
            )
            Utility.showLog(
                TAG,
                "-----mRightProgressPos----->>>>>>$mRightProgressPos"
            )
            when (action) {
                MotionEvent.ACTION_DOWN -> if (rangeListener != null) {
                    rangeListener!!.onRangeStart(isLeft)
                }

                MotionEvent.ACTION_MOVE -> {
                    Utility.printErrorLog("rangeSeekbar VideoTrimmerView passing to VideoTrimmerFragment. on action move")
                    if (rangeListener != null) {
                        rangeListener!!.onRangeSelection(
                            mLeftProgressPos,
                            mRightProgressPos,
                            isLeft
                        )
                    }
                }

                MotionEvent.ACTION_UP -> if (rangeListener != null) {
                    rangeListener!!.onRangeChanged(mLeftProgressPos, mRightProgressPos, isLeft)
                }

                else -> {}
            }
            mRangeSeekBarView?.setStartEndTime(mLeftProgressPos, mRightProgressPos)
        }

    inner class VideoChangeCoverAdapter(val context: Context) : RecyclerView.Adapter<VideoChangeCoverAdapter.VideoCoverViewHolder>() {
        private val mBitmaps: MutableList<Bitmap> = ArrayList()


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoCoverViewHolder {
            return VideoCoverViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.video_thumb_change_cover_item_layout,
                    parent,
                    false))
        }

        override fun onBindViewHolder(holder: VideoCoverViewHolder, position: Int) {
            holder.thumbImageView?.setImageBitmap(mBitmaps[position])
        }

        override fun getItemCount(): Int {
            return mBitmaps.size
        }

        fun addBitmaps(bitmap: Bitmap) {
            mBitmaps.add(bitmap)
            notifyDataSetChanged()
        }

        fun clear() {
            mBitmaps.clear()
            notifyDataSetChanged()
        }

        inner class VideoCoverViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var thumbImageView: ImageView? = null

            init {
                thumbImageView = itemView.findViewById(R.id.thumb)
            }

        }

    }

    open fun cancelThreads() {
        //BackgroundExecutor.cancelAll("1", true)
        //UiThreadExecutor.cancelAll("1")
    }

    /**
     * Cancel trim thread execute action when finish
     */
    open fun onDestroy() {
        cancelThreads()
    }
}