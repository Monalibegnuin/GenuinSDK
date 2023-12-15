package com.begenuin.library.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.begenuin.library.R
import com.begenuin.library.common.Utility
import com.begenuin.library.common.customViews.CustomTextView
import com.begenuin.library.common.customViews.DisplayPictureView
import com.begenuin.library.common.customViews.expandablelayout.ExpandableLayout
import com.begenuin.library.core.interfaces.LoopsAdapterListener
import com.begenuin.library.core.interfaces.VideoUploadInterface
import com.begenuin.library.data.eventbus.ConversationUpdateEvent
import com.begenuin.library.data.model.LoopsModel
import com.begenuin.library.data.model.MessageModel
import com.begenuin.library.data.viewmodel.UploadQueueManager
import com.begenuin.library.data.viewmodel.VideoAPIManager
import com.begenuin.library.views.adpters.UploadVideosAdapter
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import org.greenrobot.eventbus.EventBus

class LoopsAdapter(
    private val mContext: Activity,
    private var loopList: List<LoopsModel>,
    private val isFromInbox: Boolean,
    private val loopsAdapterListener: LoopsAdapterListener,
) : RecyclerView.Adapter<LoopsAdapter.LoopsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoopsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.raw_inbox_loop_list, parent, false)
        return LoopsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoopsViewHolder, position: Int) {
        try {
            if (holder is LoopsViewHolder) {
                val model = loopList[position]

                if (isFromInbox) {
                    holder.llPrivacySettings?.visibility = View.VISIBLE
                    holder.llSubscribersViews?.visibility = View.GONE
                    if (model.settings != null && !model.settings!!.discoverable) {
                        holder.ivPrivacyTypeRT?.setImageResource(R.drawable.ic_link_with_bg)
                        holder.tvPrivacyOptionRT?.text =
                            mContext.resources.getString(R.string.unlisted)
                    } else {
                        holder.ivPrivacyTypeRT?.setImageResource(R.drawable.ic_globe_with_bg)
                        holder.tvPrivacyOptionRT?.text =
                            mContext.resources.getString(R.string.everyone)
                    }
                } else {
                    holder.llPrivacySettings?.visibility = View.GONE
                    holder.llSubscribersViews?.visibility = View.VISIBLE
                }

                holder.ivPost?.tag = model
                holder.llUploadOptions?.tag = model
                holder.cardMain?.tag = model
                holder.rlImagesContainer?.tag = model

                // Get filtered chat which are not in uploading or failure state
                val filteredList = getFilteredList(model)

                // Find latest time to show time from filtered list
                val lastUpdateTime: String = if (filteredList.isNotEmpty()) {
                    Utility.timeFormat(filteredList[0].messageAt!!.toLong() / 1000)
                } else {
                    Utility.timeFormat(System.currentTimeMillis() / 1000)
                }
                holder.tvConvTime?.text = lastUpdateTime

                // Get unread count of loop
                val unreadMessageCount = if (!TextUtils.isEmpty(model.unreadMessageCount)) {
                    model.unreadMessageCount!!.toLong()
                } else {
                    0
                }

                // If filtered list is empty than its a case of creating loop
                if (filteredList.isEmpty()) {
                    holder.rlUnReadContainer?.visibility = View.GONE
                    holder.tvNewVideos?.visibility = View.GONE
                    holder.tvUserName?.visibility = View.VISIBLE
                    holder.tvPosted?.visibility = View.VISIBLE
                    holder.tvUserName?.text = mContext.resources.getString(R.string.you)
                    holder.ivLatestThumbnail?.visibility = View.INVISIBLE
                    holder.tvPosted?.text =
                        String.format(" %s", mContext.resources.getString(R.string.created))
                } else {
                    // If there are any unread videos in loop than show unread count
                    if (unreadMessageCount > 0) {
                        holder.tvNewVideos?.visibility = View.VISIBLE
                        holder.tvUserName?.visibility = View.GONE
                        holder.tvPosted?.visibility = View.GONE

                        // If only 1 unread video than show 1 new video
                        if (unreadMessageCount == 1L) {
                            holder.tvNewVideos?.text = String.format(
                                "%s %s",
                                model.unreadMessageCount,
                                mContext.resources.getString(R.string.new_video)
                            )
                        } else {

                            // else show {count} new videos
                            holder.tvNewVideos?.text = String.format(
                                "%s %s",
                                model.unreadMessageCount,
                                mContext.resources.getString(R.string.new_videos)
                            )
                        }
                    } else {
                        // Otherwise show latest video posted info
                        holder.tvNewVideos?.visibility = View.GONE
                        holder.tvUserName?.visibility = View.VISIBLE
                        holder.tvPosted?.visibility = View.VISIBLE

                        // If latest video posted by logged in user than show 'You posted' otherwise '{user} posted'
//                        if (isLoggedInUser(filteredList[0].owner!!.userId)) {
//                            holder.tvUserName?.text = mContext.resources.getString(R.string.you)
//                        } else {
                        holder.tvUserName?.text =
                            String.format("@%s", filteredList[0].owner!!.userName)
                        //}
                        holder.tvPosted?.text =
                            String.format(" %s", mContext.resources.getString(R.string.posted))
                    }

                    // If only one video available in loop
                    if (filteredList.size == 1) {
                        holder.rlUnReadContainer?.visibility = View.GONE
                        holder.ivLatestThumbnail?.visibility = View.VISIBLE

                        // Show latest thumbnail of loop
                        holder.ivLatestThumbnail?.let {
                            Glide.with(mContext).asDrawable().load(filteredList[0].thumbnailUrl)
                                .placeholder(R.color.color_E7E7E7).error(R.drawable.ic_no_preivew)
                                .into(it)
                        }
                    } else {

                        // If more than one videos available in loop
                        holder.ivLatestThumbnail?.visibility = View.GONE
                        holder.rlUnReadContainer?.visibility = View.VISIBLE

                        // Show latest thumbnail
                        holder.ivUnRead1?.let {
                            Glide.with(mContext).asDrawable().load(filteredList[0].thumbnailUrl)
                                .placeholder(R.color.color_E7E7E7).error(R.drawable.ic_no_preivew)
                                .into(it)
                        }

                        // Show 2nd latest thumbnail
                        holder.ivUnRead2?.let {
                            Glide.with(mContext).asDrawable().load(filteredList[1].thumbnailUrl)
                                .placeholder(R.color.color_E7E7E7).error(R.drawable.ic_no_preivew)
                                .into(it)
                        }

                        // If only 2 videos than hide third unread view
                        if (filteredList.size == 2) {
                            holder.ivUnRead3?.visibility = View.GONE
                        } else {
                            // Show 3rd latest thumbnail
                            holder.ivUnRead3?.visibility = View.VISIBLE
                            holder.ivUnRead3?.let {
                                Glide.with(mContext).asDrawable().load(filteredList[2].thumbnailUrl)
                                    .placeholder(R.color.color_E7E7E7)
                                    .error(R.drawable.ic_no_preivew)
                                    .into(it)
                            }
                        }
                    }

                }

                // Set the card background base on read/unread loop chats
                if (unreadMessageCount > 0) {
                    holder.ivPost?.visibility = View.GONE
                    holder.cardMain?.setCardBackgroundColor(
                        mContext.resources.getColor(
                            R.color.splash_background_opacity5,
                            null
                        )
                    )
                    holder.cardMain?.strokeColor =
                        mContext.resources.getColor(R.color.splash_background_opacity40, null)
                    holder.rlLowerBg?.setBackgroundColor(
                        mContext.resources.getColor(
                            R.color.splash_background_opacity5,
                            null
                        )
                    )
                } else {
                    // If loop is not created yet than hide post button
                    if (model.chatId == "-101" || (!isFromInbox)) { //TODO: Removed condition && (model.memberInfo == null)
                        // || communityRole == CommunityMemberRole.NONE.value
                        holder.ivPost?.visibility = View.GONE
                    } else {
                        holder.ivPost?.visibility = View.VISIBLE
                    }
                    holder.cardMain?.setCardBackgroundColor(
                        mContext.resources.getColor(
                            R.color.colorWhite,
                            null
                        )
                    )
                    holder.cardMain?.strokeColor =
                        mContext.resources.getColor(R.color.color_E7E7E7, null)
                    holder.rlLowerBg?.setBackgroundColor(
                        mContext.resources.getColor(
                            R.color.color_F9F9F9,
                            null
                        )
                    )
                }

                // Set the loop details in card
                if (model.group != null) {
                    holder.tvLoopDesc?.visibility = View.VISIBLE
                    val group = model.group
                    if (group != null) {
                        holder.tvLoopName?.text = group.name
                        holder.tvLoopDesc?.text = group.description
                        /*if (TextUtils.isEmpty(group.smallDp)) {
                            holder.llLoopDp?.setDpWithInitials(
                                group.name,
                                group.colorCode,
                                group.textColorCode
                            )
                        } else {
                            holder.llLoopDp?.setDpWithImage(
                                mContext,
                                false,
                                group.dp,
                                group.smallDp,
                                false
                            )
                        }*/
                        if (!isFromInbox) {
                            // Set the loop noOfSubscribers
                            if (!TextUtils.isEmpty(group.noOfSubscribers)) {
                                val noOfSubscribers =
                                    Utility.formatNumber(group.noOfSubscribers!!.toLong())
                                holder.tvNoOfSubscribers?.text = noOfSubscribers
                                if (noOfSubscribers == "1") {
                                    holder.tvSubscribers?.text =
                                        mContext.resources.getString(R.string.no_of_subscriber)
                                } else {
                                    holder.tvSubscribers?.text =
                                        mContext.resources.getString(R.string.no_of_subscribers)
                                }
                            } else {
                                holder.tvNoOfSubscribers?.text = "0"
                                holder.tvSubscribers?.text =
                                    mContext.resources.getString(R.string.no_of_subscribers)
                            }

                            // Set the loop noOfViews
                            if (!TextUtils.isEmpty(group.noOfViews)) {
                                val noOfViews = Utility.formatNumber(group.noOfViews!!.toLong())
                                holder.tvNoOfViews?.text = noOfViews
                                if (noOfViews == "1") {
                                    holder.tvViews?.text =
                                        mContext.resources.getString(R.string.no_of_loop_view)
                                } else {
                                    holder.tvViews?.text =
                                        mContext.resources.getString(R.string.no_of_loop_views)
                                }
                            } else {
                                holder.tvNoOfViews?.text = "0"
                                holder.tvViews?.text =
                                    mContext.resources.getString(R.string.no_of_loop_views)
                            }
                        }
                    }
                    // Up to 3 members will show in card where 1st will always be owner of loop.
                    if (model.group!!.members.size > 0) {
                        holder.llOwnerCoHostDetails?.visibility = View.VISIBLE
                        val members = model.group!!.members
                        if (members.size >= 1) {

                            // Show 1st member which will be owner of loop
                            val member1 = members[0]
                            //if (isLoggedInUser(member1.userId)) {
                            holder.tvOwner?.text = mContext.resources.getString(R.string.you)
//                            } else {
//                                holder.tvOwner?.text = String.format("@%s", member1.userName)
//                            }
                            holder.ivMember1?.visibility = View.VISIBLE
                            holder.ivMember1?.setDpWithImage(
                                mContext,
                                member1.isAvatar,
                                member1.profileImage,
                                member1.profileImageS,
                                false
                            )
                        } else {
                            holder.ivMember1?.visibility = View.GONE
                            holder.tvOwner?.text = ""
                        }

                        if (members.size >= 2) {
                            val member2 = members[1]

                            // Show second member of the loop
                            if (members.size == 2) {
                                //if (isLoggedInUser(member2.userId)) {
                                holder.tvOtherCoHosts?.text =
                                    String.format(
                                        " + %s",
                                        mContext.resources.getString(R.string.you)
                                    )
//                                } else {
//                                    holder.tvOtherCoHosts?.text =
//                                        String.format(" + @%s", member2.userName)
//                                }
                            }
                            holder.ivMember2?.visibility = View.VISIBLE
                            holder.ivMember2?.setDpWithImage(
                                mContext,
                                member2.isAvatar,
                                member2.profileImage,
                                member2.profileImageS,
                                false
                            )
                        } else {
                            holder.ivMember2?.visibility = View.GONE
                            holder.tvOtherCoHosts?.text = ""
                        }

                        if (members.size >= 3) {
                            val member3 = members[2]

                            // Show no of co-hosts to the user
                            if (!TextUtils.isEmpty(group?.noOfMembers)) {
                                holder.tvOtherCoHosts?.text = String.format(
                                    " + %d %s",
                                    group?.noOfMembers!!.toLong() - 1,
                                    mContext.resources.getString(R.string.co_hosts)
                                )
                            } else {
                                holder.tvOtherCoHosts?.text = ""
                            }
                            holder.ivMember3?.visibility = View.VISIBLE
                            holder.ivMember3?.setDpWithImage(
                                mContext,
                                member3.isAvatar,
                                member3.profileImage,
                                member3.profileImageS,
                                false
                            )
                        } else {
                            holder.ivMember3?.visibility = View.GONE
                        }
                    } else {
                        holder.llOwnerCoHostDetails?.visibility = View.GONE
                    }
                } else {
                    holder.llOwnerCoHostDetails?.visibility = View.GONE
                    holder.tvLoopDesc?.visibility = View.GONE
                    holder.tvLoopName?.text = ""
                }

                // check for any chats are pending to upload and show upload list based on that.
                if (model.pendingUploadList != null && model.pendingUploadList!!.isNotEmpty()) {
                    holder.elUploadContainer?.visibility = View.VISIBLE
                    holder.llUploadContainer?.visibility = View.VISIBLE

                    //Following block of code handles expand + fade in animation in consecutive order.
                    if (!model.isExpanded) {
                        holder.llUploadContainer?.alpha = 0f
                        model.isExpanded = true
                        holder.elUploadContainer?.expand(true)
                        holder.elUploadContainer?.setOnExpansionUpdateListener { _, state ->
                            if (state == ExpandableLayout.State.EXPANDED) {
                                holder.llUploadContainer?.animate()?.alpha(1f)?.setDuration(500)
                                    ?.start()
                                holder.elUploadContainer?.setOnExpansionUpdateListener(null)
                            }
                        }
                    } else {
                        holder.elUploadContainer?.setExpanded(true, false)
                    }

                    //Set Adapter to show pending upload list
                    val uploadChatAdapter =
                        UploadVideosAdapter(mContext, model, model.pendingUploadList!!, object :
                            VideoUploadInterface {
                            override fun onRetryClicked(messageModel: MessageModel) {
                                if (Utility.isNetworkAvailable(mContext)) {
                                    messageModel.isRetry = false
                                    if (Utility.getDBHelper() != null) {
                                        // Update retry status for particular loop video in DB by local path
                                        Utility.getDBHelper()!!
                                            .updateRetryStatusForLoopVideo(
                                                messageModel.localVideoPath, false
                                            )
                                    }
                                    notifyDataSetChanged()
                                    if (messageModel.isVideoAndImageUploaded()) {
                                        VideoAPIManager.retryAPILoopVideo(mContext, messageModel)
                                    } else {
                                        UploadQueueManager.getInstance()
                                            .uploadLoopVideo(mContext, messageModel)
                                    }
                                }
                            }

                            override fun onRetryLoopClicked(loopsModel: LoopsModel) {
                                if (Utility.isNetworkAvailable(mContext)) {
                                    if (loopsModel.latestMessages != null && loopsModel.latestMessages!!.isNotEmpty()) {
                                        val messageModel = loopsModel.latestMessages!![0]
                                        messageModel.isRetry = false
                                        if (Utility.getDBHelper() != null) {
                                            // Update retry status for particular loop video in DB by local path
                                            Utility.getDBHelper()!!
                                                .updateRetryStatusForLoopVideo(
                                                    messageModel.localVideoPath, false
                                                )
                                        }
                                        notifyDataSetChanged()
                                        if (loopsModel.latestMessages!![0].isVideoAndImageUploaded()) {
                                            //TODO: Need to uncomment and test
                                            VideoAPIManager.retryAPILoop(mContext, loopsModel)
                                        } else {
                                            UploadQueueManager.getInstance()
                                                .uploadLoop(mContext, loopsModel)
                                        }
                                    }
                                }
                            }

                            override fun onDeleteClicked(messageModel: MessageModel) {
                                val isNewLoop =
                                    messageModel.chatId.equals("-101", ignoreCase = true)
                                showVideoDeleteAlert(isNewLoop, messageModel)
                            }
                        })
                    holder.rvUploadList?.adapter = uploadChatAdapter
                    holder.tvUploadingVideos?.let { setUploadText(model, it) }
                    val failedVideoSize = model.pendingUploadList!!.filter { it.isRetry }
                    if (failedVideoSize.size > 1) {
                        holder.llUploadOptions?.visibility = View.VISIBLE
                    } else {
                        holder.llUploadOptions?.visibility = View.GONE
                    }
                } else {
                    //Following block of code handles fade out + collapse anim in consecutive order.
                    if (model.isExpanded) {
                        model.isExpanded = false
                        val anim = AlphaAnimation(1f, 0f)
                        anim.duration = 500
                        anim.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {
                            }

                            override fun onAnimationEnd(p0: Animation?) {
                                holder.llUploadContainer?.visibility = View.INVISIBLE
                                holder.elUploadContainer?.setOnExpansionUpdateListener { _, state ->
                                    if (state == ExpandableLayout.State.COLLAPSED) {
                                        holder.llUploadContainer?.visibility = View.VISIBLE
                                        holder.llUploadContainer?.alpha = 0f
                                        holder.elUploadContainer?.setOnExpansionUpdateListener(null)
                                        holder.elUploadContainer?.visibility = View.GONE
                                    }
                                }
                                holder.elUploadContainer?.collapse(true)
                            }

                            override fun onAnimationRepeat(p0: Animation?) {
                            }
                        })
                        holder.llUploadContainer?.startAnimation(anim)
                    } else {
                        holder.elUploadContainer?.setExpanded(false, false)
                        holder.elUploadContainer?.visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemViewType(position: Int): Int {
            return loopList.size
        }

    // Show dialog to confirm failed video should be clear or not.
    private fun showVideoDeleteAlert(
        isNewLoop: Boolean,
        message: MessageModel
    ) {
        val mVideoDeleteDialog = Dialog(mContext)
        mVideoDeleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mVideoDeleteDialog.setContentView(R.layout.common_simple_dialog_new)
        mVideoDeleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mVideoDeleteDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mVideoDeleteDialog.show()
        val tvTitle = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_title)
        val tvMsg = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_message)
        val btnCancel = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_btn_cancel)
        val btnYes = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_btn_yes)
        btnYes.text = mContext.resources.getString(R.string.txt_clear)
        tvTitle.text = mContext.resources.getString(R.string.txt_clear_video_header)
        tvMsg.text = mContext.resources.getString(R.string.txt_clear_video_sub)
        btnCancel.setOnClickListener { mVideoDeleteDialog.dismiss() }
        btnYes.setOnClickListener {
            mVideoDeleteDialog.dismiss()
            if (isNewLoop) {
                    // This will delete loop from DB as in this case loop creation is failed
                    Utility.getDBHelper()?.deleteLoopByLocalPath(message.localVideoPath)
            } else {
                // This will delete failed loop video from DB
                if (Utility.getDBHelper() != null) {
                    Utility.getDBHelper()!!.deleteLoopVideoByLocalPath(message.localVideoPath)
                    Utility.getDBHelper()!!.updateLatestMessageAt(message.chatId)
                }
            }
            EventBus.getDefault().post(ConversationUpdateEvent(true))
        }
    }

    // Show dialog to confirm all failed videos should be clear or not
    private fun showAllVideoDeleteAlert(
        model: LoopsModel
    ) {
        // Below condition is used to check if given model is a new loop or not
        val inNewLoop = model.chatId.equals("-101", ignoreCase = true)
        val mVideoDeleteDialog = Dialog(mContext)
        mVideoDeleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mVideoDeleteDialog.setContentView(R.layout.common_simple_dialog_new)
        mVideoDeleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mVideoDeleteDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mVideoDeleteDialog.show()
        val tvTitle = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_title)
        val tvMsg = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_message)
        val btnCancel = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_btn_cancel)
        val btnYes = mVideoDeleteDialog.findViewById<CustomTextView>(R.id.dialog_btn_yes)
        btnYes.text = mContext.resources.getString(R.string.txt_clear_all)
        tvTitle.text = mContext.resources.getString(R.string.txt_clear_all_video_header)
        tvMsg.text = mContext.resources.getString(R.string.txt_clear_all_video_sub)
        btnCancel.setOnClickListener { v: View? -> mVideoDeleteDialog.dismiss() }
        btnYes.setOnClickListener { v: View? ->
            mVideoDeleteDialog.dismiss()
            if (inNewLoop) {
                // This will delete loop from DB as in this case loop creation is failed
                if (Utility.getDBHelper() != null) {
                    Utility.getDBHelper()!!
                        .deleteLoopByLocalPath(model.latestMessages!![0].localVideoPath)
                }
            } else {
                // This will delete all failed loop videos from DB
                if (Utility.getDBHelper() != null) {
                    Utility.getDBHelper()!!
                        .deleteAllFailedReplyMessages(model.chatId)
                    Utility.getDBHelper()!!.updateLatestMessageAt(model.chatId)
                }
            }
            EventBus.getDefault().post(ConversationUpdateEvent(true))
        }
    }

    // Show popup window for retry all/cancel all
    fun retryAllManagement(loopModel: LoopsModel, llUploadOptions: LinearLayout) {
        val failedVideoSize = loopModel.pendingUploadList!!.filter { it.isRetry } as ArrayList
        if (failedVideoSize.isNotEmpty()) {
            failedVideoSize.reverse()
        }
//        @SuppressLint("NonConstantResourceId")
//        val popupMenu = PopupMenuCustomLayout(
//            mContext, R.layout.retry_custom_menu) {itemId: Int ->
//
//        }
//        { itemId: Int ->
//            when (itemId) {
//                R.id.llRetry -> {
//                    if (Utility.isNetworkAvailable(mContext)) {
//                        if (Utility.getDBHelper() != null) {
//                            // This function will change the retry status to false for all failed videos
//                            Utility.getDBHelper()!!
//                                .updateAllMessagesRetryStatus(
//                                    loopModel.chatId
//                                )
//                        }
//
//                        for (i in failedVideoSize.indices) {
//                            val messageModel = failedVideoSize[i]
//                            messageModel.isRetry = false
//                            // If video & audio files are upload than we just need to call api to update the data on server
//                            if (messageModel.isVideoAndImageUploaded()) {
//                                VideoAPIManager.retryAPILoopVideo(mContext, messageModel)
//                            } else {
//                                // We need to start retry uploading if video or image failed to upload
//                                UploadQueueManager.getInstance()
//                                    .uploadLoopVideo(mContext, messageModel)
//                            }
//                        }
//                        //Update the UI
//                        notifyDataSetChanged()
//                    }
//                }
//                R.id.llDelete -> {
//                    // Show confirmation dialog
//                    showAllVideoDeleteAlert(loopModel)
//                }
//            }
//        }
//        popupMenu.setRetryCountsForDialog(failedVideoSize.size)
//        popupMenu.show(llUploadOptions)
    }

    private fun SpannableStringBuilder.spansAppend(
        text: CharSequence,
        flags: Int,
        vararg spans: Any
    ): SpannableStringBuilder {
        val start = length
        append(text)

        spans.forEach { span ->
            setSpan(span, start, length, flags)
        }

        return this
    }

    // Update list data and refresh recyclerview
    fun updateData(listData: List<LoopsModel>) {
        loopList = listData
        notifyDataSetChanged()
    }

    // Filter chats which are not in uploading or failure state
    private fun getFilteredList(model: LoopsModel): List<MessageModel> {
        return model.latestMessages!!.filter { it.videoUploadStatus == 3 }
    }

    // Set upload text for different scenarios
    private fun setUploadText(loopModel: LoopsModel, textView: TextView) {
        val builder = SpannableStringBuilder()
        val failedVideoSize = loopModel.pendingUploadList!!.filter { it.isRetry }
        val uploadingSize = loopModel.pendingUploadList!!.size - failedVideoSize.size
        if (failedVideoSize.isNotEmpty() && uploadingSize == 0) {
            if (failedVideoSize.size == 1) {
                builder.spansAppend(
                    text = mContext.resources.getString(R.string.loop_upload_failed),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    ForegroundColorSpan(mContext.resources.getColor(R.color.red_F2545B, null)),
                )
            } else {
                builder.spansAppend(
                    text = mContext.resources.getString(
                        R.string.no_of_uploads_failed,
                        failedVideoSize.size
                    ),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    ForegroundColorSpan(mContext.resources.getColor(R.color.red_F2545B, null)),
                )
            }
        } else if (failedVideoSize.isNotEmpty()) {
            builder.spansAppend(
                text = mContext.resources.getString(
                    R.string.no_of_videos_failed,
                    failedVideoSize.size
                ),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                ForegroundColorSpan(mContext.resources.getColor(R.color.red_F2545B, null)),
            )
            builder.append(" ")
        }
        if (uploadingSize > 0) {
            if (uploadingSize == 1) {
                builder.append(
                    mContext.resources.getString(
                        R.string.uploading_one_videos
                    )
                )
            } else {
                builder.append(
                    mContext.resources.getString(
                        R.string.uploading_videos,
                        uploadingSize
                    )
                )
            }
        }
        textView.setText(builder, TextView.BufferType.SPANNABLE)
    }

    override fun getItemCount(): Int {
        return loopList.size
    }

    inner class LoopsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rlLowerBg: RelativeLayout? = null
        //var llLoopDp: DisplayPictureView? = null
        var ivMember1: DisplayPictureView? = null
        var ivMember2: DisplayPictureView? = null
        var ivMember3: DisplayPictureView? = null
        var tvLoopName: TextView? = null
        var tvOwner: TextView? = null
        var tvOtherCoHosts: TextView? = null
        var llOwnerCoHostDetails: LinearLayout? = null
        var tvLoopDesc: TextView? = null
        var ivPrivacyTypeRT: ImageView? = null
        var tvPrivacyOptionRT: TextView? = null
        var ivLatestThumbnail: ShapeableImageView? = null
        var rlUnReadContainer: RelativeLayout? = null
        var ivUnRead1: ShapeableImageView? = null
        var ivUnRead2: ShapeableImageView? = null
        var ivUnRead3: ShapeableImageView? = null
        var tvNewVideos: TextView? = null
        var tvPosted: TextView? = null
        var tvUserName: TextView? = null
        var tvConvTime: TextView? = null
        var cardMain: MaterialCardView? = null
        var ivPost: ImageView? = null
        var llUploadContainer: LinearLayout? = null
        var tvUploadingVideos: TextView? = null
        var rvUploadList: RecyclerView? = null
        var llUploadOptions: LinearLayout? = null
        var elUploadContainer: ExpandableLayout? = null
        var llPrivacySettings: LinearLayout? = null
        var llSubscribersViews: LinearLayout? = null
        var tvNoOfSubscribers: TextView? = null
        var tvSubscribers: TextView? = null
        var tvNoOfViews: TextView? = null
        var tvViews: TextView? = null
        var rlImagesContainer: RelativeLayout? = null

        init {
            rlLowerBg = itemView.findViewById(R.id.rlLowerBg)
            //llLoopDp = itemView.findViewById(R.id.llLoopDp)
            tvLoopName = itemView.findViewById(R.id.tvLoopName)
            ivMember1 = itemView.findViewById(R.id.ivMember1)
            ivMember2 = itemView.findViewById(R.id.ivMember2)
            ivMember3 = itemView.findViewById(R.id.ivMember3)
            tvOwner = itemView.findViewById(R.id.tvOwner)
            tvOtherCoHosts = itemView.findViewById(R.id.tvOtherCoHosts)
            llOwnerCoHostDetails = itemView.findViewById(R.id.llOwnerCoHostDetails)
            tvLoopDesc = itemView.findViewById(R.id.tvLoopDesc)
            ivPrivacyTypeRT = itemView.findViewById(R.id.ivPrivacyTypeRT)
            tvPrivacyOptionRT = itemView.findViewById(R.id.tvPrivacyOptionRT)
            ivLatestThumbnail = itemView.findViewById(R.id.ivLatestThumbnail)
            rlUnReadContainer = itemView.findViewById(R.id.rlUnReadContainer)
            ivUnRead1 = itemView.findViewById(R.id.ivUnRead1)
            ivUnRead2 = itemView.findViewById(R.id.ivUnRead2)
            ivUnRead3 = itemView.findViewById(R.id.ivUnRead3)
            tvNewVideos = itemView.findViewById(R.id.tvNewVideos)
            tvPosted = itemView.findViewById(R.id.tvPosted)
            tvUserName = itemView.findViewById(R.id.tvUserName)
            tvConvTime = itemView.findViewById(R.id.tvConvTime)
            cardMain = itemView.findViewById(R.id.cardMain)
            ivPost = itemView.findViewById(R.id.ivPost)
            llUploadContainer = itemView.findViewById(R.id.llUploadContainer)
            tvUploadingVideos = itemView.findViewById(R.id.tvUploadingVideos)
            rvUploadList = itemView.findViewById(R.id.rvUploadList)
            llUploadOptions = itemView.findViewById(R.id.llUploadOptions)
            elUploadContainer = itemView.findViewById(R.id.elUploadContainer)
            llPrivacySettings = itemView.findViewById(R.id.llPrivacySettings)
            llSubscribersViews = itemView.findViewById(R.id.llSubscribersViews)
            tvNoOfSubscribers = itemView.findViewById(R.id.tvNoOfSubscribers)
            tvSubscribers = itemView.findViewById(R.id.tvSubscribers)
            tvNoOfViews = itemView.findViewById(R.id.tvNoOfViews)
            tvViews = itemView.findViewById(R.id.tvViews)
            rlImagesContainer = itemView.findViewById(R.id.rlImagesContainer)

            // Open camera for posting a video to the loop
            ivPost?.setOnClickListener {
                // Replaced conversation model to loop model
                val model: LoopsModel = it.tag as LoopsModel
//                Utility.goToCameraForReply(
//                    mContext,
//                    model.chatId,
//                    model.convType!!,
//                    model.group,
//                    false,
//                    model.settings
//                )
            }


            // Open camera with timer for posting a video to the loop
            ivPost?.setOnLongClickListener {
//                if (Utility.isCameraPermissionsGranted(mContext)) {
//
//                    // Replaced conversation model to loop model
//                    val model: LoopsModel = it.tag as LoopsModel
//                    Utility.vibrateDevice(mContext)
//                    Utility.goToCameraForReply(
//                        mContext,
//                        model.chatId,
//                        model.convType,
//                        model.group,
//                        true,
//                        model.settings
//                    )
//                }
                true
            }

            llUploadOptions?.setOnClickListener {
                val model: LoopsModel = it.tag as LoopsModel
                retryAllManagement(model, llUploadOptions!!)
            }

            cardMain?.setOnClickListener {
                val model: LoopsModel = it.tag as LoopsModel
                loopsAdapterListener.onLoopClicked(model)
            }

            rlImagesContainer?.setOnClickListener {
                val model = it.tag as LoopsModel
                loopsAdapterListener.onThumbnailStackClicked(model)
            }
        }
    }
}

