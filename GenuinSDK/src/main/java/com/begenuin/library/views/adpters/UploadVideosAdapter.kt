package com.begenuin.library.views.adpters

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.begenuin.library.R
import com.begenuin.library.common.customViews.PopupMenuCustomLayout
import com.begenuin.library.core.interfaces.VideoUploadInterface
import com.begenuin.library.data.model.LoopsModel
import com.begenuin.library.data.model.MessageModel
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.CircularProgressIndicator

class UploadVideosAdapter(
    private val mContext: Activity,
    private var loopsModel: LoopsModel,
    private var messageList: List<MessageModel>,
    private var videoUploadInterface: VideoUploadInterface
) : RecyclerView.Adapter<UploadVideosAdapter.VideosViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.raw_loop_upload_video_list, parent, false)
        return VideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        try {
            val model = messageList[position]

            // Show thumbnail of message
            holder.ivUploadVideoThumbnail?.let {
                Glide.with(mContext).asDrawable().load(model.localImagePath)
                    .placeholder(R.color.color_E7E7E7).error(R.drawable.ic_no_preivew)
                    .into(it)
                setImageGrayScale(true, it)
            }
            holder.ivUploadVideoThumbnail?.tag = model

            // if message is failed to upload show retry else loading indicator
            if (model.isRetry) {
                holder.progressUploadVideo?.visibility = View.GONE
                holder.ivRetryUpload?.visibility = View.VISIBLE
            } else {
                holder.progressUploadVideo?.visibility = View.VISIBLE
                holder.ivRetryUpload?.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class VideosViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var ivUploadVideoThumbnail: ImageView? = null
        var progressUploadVideo: CircularProgressIndicator? = null
        var ivRetryUpload: ImageView? = null
        var lavUploadSuccess: LottieAnimationView? = null


        init {
            ivUploadVideoThumbnail = itemView.findViewById(R.id.ivUploadVideoThumbnail)
            progressUploadVideo = itemView.findViewById(R.id.progressUploadVideo)
            ivRetryUpload = itemView.findViewById(R.id.ivRetryUpload)
            lavUploadSuccess = itemView.findViewById(R.id.lavUploadSuccess)

            // If message is failed to upload show retry popup with retry/clear option
            ivUploadVideoThumbnail?.setOnClickListener {
                // now we need to cast this model as MessageModel instead of ChatModel
                val messageModel = it.tag as MessageModel
                if (messageModel.isRetry) {
//                    @SuppressLint("NonConstantResourceId") val popupMenu = PopupMenuCustomLayout(
//                        mContext, R.layout.retry_custom_menu
//                    ) { itemId: Int ->
//                        when (itemId) {
//                            R.id.llRetry -> {
//                                if (loopsModel.chatId == "-101") {
//                                    videoUploadInterface.onRetryLoopClicked(loopsModel)
//                                } else {
//                                    videoUploadInterface.onRetryClicked(messageModel)
//                                }
//                            }
//                            R.id.llDelete -> {
//                                videoUploadInterface.onDeleteClicked(messageModel)
//                            }
//                        }
//                    }
//                    popupMenu.show(ivRetryUpload)
                }
            }
        }
    }

    // Apply grey scale to thumbnail
    private fun setImageGrayScale(greyScale: Boolean, imageView: ImageView) {
        if (greyScale) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            imageView.colorFilter = ColorMatrixColorFilter(matrix)
        } else {
            imageView.colorFilter = null
        }
    }

    // This method makes the lottie animation play.
    fun playUploadCompleteAnim(viewHolder: VideosViewHolder) {
        viewHolder.progressUploadVideo?.visibility = View.GONE
        viewHolder.lavUploadSuccess?.visibility = View.VISIBLE
        viewHolder.lavUploadSuccess?.playAnimation()
    }

    // show video progress indicator in percentage
    fun updateVideoProgress(
        viewHolder: VideosViewHolder,
        progress: Int
    ) {
        if (progress == 100) {
            viewHolder.progressUploadVideo?.isIndeterminate = true
        } else {
            viewHolder.progressUploadVideo?.isIndeterminate = false
            viewHolder.progressUploadVideo?.setProgress(progress, true)
        }
    }
}