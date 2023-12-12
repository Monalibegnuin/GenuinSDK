package com.begenuin.library.views

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.begenuin.library.R
import com.begenuin.library.common.Utility
import com.begenuin.library.common.customViews.tooltip.SimpleTooltip
import com.begenuin.library.core.enums.ExploreVideoType
import com.begenuin.library.core.enums.PeekSource
import com.begenuin.library.data.model.ChatModel
import com.begenuin.library.data.model.DiscoverModel
import com.begenuin.library.data.model.MessageModel
import com.begenuin.library.data.viewmodel.ExploreViewModel
import com.begenuin.library.peekandpop.PeekAndPop
import com.begenuin.library.peekandpop.model.HoldAndReleaseView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.util.Properties

class PeekViewRecyclerAdapter(
    val peekAndPop: PeekAndPop,
    private val optionsData: ArrayList<String>,
    val context: Context,
    val isMessageModel: Boolean,
    val messageModel: MessageModel?,
    val isExploreViewModel: Boolean,
    val exploreViewModel: ExploreViewModel<*>?,
    val chatModel: ChatModel?,
    val isMyProfile: Boolean,
    val videoPosition: Int,
    val source: PeekSource,
) : RecyclerView.Adapter<PeekViewRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.peek_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = optionsData[position]
        if (optionsData[position] == context.getString(R.string.report)) {
            holder.textView.setTextColor(context.getColor(R.color.red_F2545B))
        } else {
            holder.textView.setTextColor(context.getColor(R.color.black_111111))
        }
        if (position == optionsData.size - 1) {
            holder.viewDivider.visibility = View.GONE
        } else {
            holder.viewDivider.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return optionsData.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView),
        PeekAndPop.OnHoldAndReleaseListener {
        val textView: TextView = ItemView.findViewById(R.id.tvOption)
        val viewDivider: View = ItemView.findViewById(R.id.viewDivider)

        init {
            peekAndPop.setOnHoldAndReleaseListener(this)
            //peekAndPop.addHoldAndReleaseView(item.id)
            peekAndPop.holdAndReleaseViews.add(HoldAndReleaseView(ItemView))
        }

        private lateinit var simpleTooltip: SimpleTooltip
        private lateinit var bottomSheetDialogFlaggedVideo: BottomSheetDialog
        override fun onHold(view: View?, position: Int) {
            //Utility.showToast(context,position.toString())
//            val tvOption: TextView? = view?.findViewById(R.id.tvOption)
//            simpleTooltip = SimpleTooltip.Builder(context)
//                .anchorView(tvOption)
//                .text(tvOption?.text)
//                .gravity(Gravity.TOP)
//                .margin(context.resources.getDimension(R.dimen.dimen_5dp))
//                .animated(true)
//                .isCustomTextAppearance(true)
//                .arrowHeight(Utility.dpToPx(10f, context))
//                .arrowWidth(Utility.dpToPx(10f, context))
//                .arrowColor(context.resources.getColor(R.color.black_111111, null))
//                .backgroundColor(context.resources.getColor(R.color.black_111111, null))
//                .textColor(context.resources.getColor(R.color.colorWhite, null))
//                .ignoreOverlay(true)
//                .build()
//            simpleTooltip.show()
        }

        override fun onLeave(view: View?, position: Int) {
            if (this::simpleTooltip.isInitialized) {
                simpleTooltip.dismiss()
            }
        }

        override fun onRelease(view: View?, position: Int) {
            if (this::simpleTooltip.isInitialized) {
                simpleTooltip.dismiss()
            }
//            val tvOption: TextView? = view?.findViewById(R.id.tvOption)
//            val optionSelected = (tvOption as TextView).text.toString()
//            if (optionSelected == context.getString(R.string.view_profile)) {
//                val intent = Intent(context, ViewProfileActivity::class.java)
//                if (isExploreViewModel) {
//                    val userId = exploreViewModel?.userId
//                    intent.putExtra("userId", userId)
//                } else {
//                    val userId = chatModel?.owner?.userId
//                    intent.putExtra("userId", userId)
//                }
//                context.startActivity(intent)
//                (context as Activity).overridePendingTransition(
//                    R.anim.slide_in_right,
//                    R.anim.slide_out_left
//                )
//            } else if (optionSelected == context.getString(R.string.watch_roundtable)) {
//                if (isExploreViewModel) {
//                    val intentLoop = Intent(context, FeedLoopActivity::class.java)
//                    intentLoop.putExtra("chatId", exploreViewModel!!.feedId)
//                    intentLoop.putExtra("messageId", exploreViewModel.convId)
//                    context.startActivity(intentLoop)
//                }
//            } else if (optionSelected == context.getString(R.string.unsave)) {
//                if (isExploreViewModel) {
//                    val discoverModel = exploreViewModel?.obj as DiscoverModel
//                    discoverModel.unSaveVideo()
//                    discoverModel.saved = false
//                    val saveUnSaveEvent = SaveUnSaveEvent()
//                    saveUnSaveEvent.isSaved = false
//                    saveUnSaveEvent.discoverModel = exploreViewModel
//                    EventBus.getDefault().post(saveUnSaveEvent)
//                }
//            } else if (optionSelected == context.getString(R.string.why_flagged)) {
//                if (isExploreViewModel) {
//                    val discoverModel = exploreViewModel?.obj as DiscoverModel
//                    openBottomSheetDialogForFlagVideo(discoverModel)
//                }
//            } else if (optionSelected == context.getString(R.string.share)) {
//                if (isMessageModel) {
//                    Utility.shareVideoLink(
//                        context,
//                        messageModel?.shareURL,
//                        messageModel?.messageId,
//                        Constants.FROM_ROUND_TABLE
//                    )
//                } else if (isExploreViewModel) {
//                    if (exploreViewModel?.type == ExploreVideoType.PUBLIC_VIDEO && !isMyProfile) {
//                        callShareCountApi()
//                    }
//                    val shareURL: String? = exploreViewModel?.shareURL
//                    if (!TextUtils.isEmpty(shareURL)) {
//                        var from = ""
//                        if (exploreViewModel?.type == ExploreVideoType.PUBLIC_VIDEO) {
//                            from = Constants.FROM_PUBLIC_VIDEO
//                        } else if (exploreViewModel?.type == ExploreVideoType.RT) {
//                            from = Constants.FROM_ROUND_TABLE
//                        }
//                        Utility.shareVideoLink(
//                            context,
//                            shareURL,
//                            exploreViewModel?.feedId,
//                            from
//                        )
//                    }
//                } else {
//                    Utility.shareVideoLink(
//                        context,
//                        chatModel?.shareURL,
//                        chatModel?.conversationId,
//                        Constants.FROM_ROUND_TABLE
//                    )
//                }
//            } else if (optionSelected == context.getString(R.string.download)) {
//                if (isExploreViewModel && exploreViewModel?.type == ExploreVideoType.PUBLIC_VIDEO) {
//                    val inflater =
//                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                    val downloadComponentsView =
//                        inflater.inflate(R.layout.download_components, null)
//                    val model = exploreViewModel.obj as DiscoverModel
//                    Utility.printErrorLog("status: " + model.imageUploadStatus + " videoStatus: " + model.videoUploadStatus)
//                    val downloadVideo = DownloadVideo(context as Activity)
//                    val textViewArrayList = java.util.ArrayList<TextView>()
//                    textViewArrayList.add(downloadComponentsView.findViewById(R.id.tvGenuinLogo))
//                    textViewArrayList.add(downloadComponentsView.findViewById(R.id.tvFromCameraRoll))
//                    textViewArrayList.add(downloadComponentsView.findViewById(R.id.tvName))
//                    textViewArrayList.add(downloadComponentsView.findViewById(R.id.tvUserNameWaterMark))
//                    textViewArrayList.add(downloadComponentsView.findViewById(R.id.tvFromUserName))
//                    textViewArrayList.add(downloadComponentsView.findViewById(R.id.tvFullNameWaterMark))
//                    textViewArrayList.add(downloadComponentsView.findViewById(R.id.tvBioWaterMark))
//                    downloadVideo.initDownload(
//                        model,
//                        textViewArrayList,
//                        downloadComponentsView.findViewById(R.id.ivWaterMarkProfile)
//                    )
//                    downloadVideo.setDownloadListener(object : OnVideoDownload {
//                        override fun onSuccessfullyDownloadVideo() {
//                            Utility.printErrorLog("download successfully")
//                            Utility.showToast(
//                                context.getApplicationContext(),
//                                context.resources.getString(R.string.video_save_to_gallery)
//                            )
//                        }
//
//                        override fun onDownloadVideoFailure(code: Int) {
//                        }
//
//                    })
//                    if (!downloadVideo.isPermissionGranted) {
//                        (context).requestPermissions(
//                            arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                            Constants.WRITE_STORAGE_PERMISSION
//                        )
//                    } else {
//                        downloadVideo.saveDownloadedVideoToGallery(context)
//                    }
//                }
//            } else if (optionSelected == context.getString(R.string.reply)) {
//                longPressRecyclerActionInterface?.onReply(videoPosition)
//            } else if (optionSelected == context.getString(R.string.comment)) {
//                longPressRecyclerActionInterface?.onComment(videoPosition)
//            } else if (optionSelected == context.getString(R.string.report)) {
//                openBottomSheetDialogForReportLoopVideo()
//            } else if (optionSelected == context.getString(R.string.pin_to_loop)) {
//                longPressRecyclerActionInterface?.onLoopVideoPin(true)
//            } else if (optionSelected == context.getString(R.string.unpin_from_loop)) {
//                longPressRecyclerActionInterface?.onLoopVideoPin(false)
//            }
        }

        private fun openBottomSheetDialogForReportLoopVideo() {
//            val bottomSheetDialogReport =
//                BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
//            val bottomSheetView: View =
//                (context as Activity).layoutInflater.inflate(
//                    R.layout.bottom_sheet_report_video,
//                    null
//                )
//            val radioGroup = bottomSheetView.findViewById<RadioGroup>(R.id.radioGroup)
//            val btnContinue = bottomSheetView.findViewById<Button>(R.id.btnContinue)
//            btnContinue.setOnClickListener { v: View? ->
//                bottomSheetDialogReport.dismiss()
//                val selectedId = radioGroup.checkedRadioButtonId
//                val radioButton = bottomSheetView.findViewById<RadioButton>(selectedId)
//                val reason = radioButton.text.toString()
//                val properties =
//                    Properties()
//                properties[Constants.KEY_REASON] = reason
//                sendDataDogLogs(
//                    Constants.VIDEO_REPORTED,
//                    properties
//                )
//                showReportDialog()
//            }
//            // Bottom sheet dialog
//            bottomSheetDialogReport.setContentView(bottomSheetView)
//            bottomSheetDialogReport.setCancelable(true)
//            bottomSheetDialogReport.setOnCancelListener(DialogInterface.OnCancelListener {
//                sendDataDogLogs(
//                    Constants.REPORT_VIDEO_CLOSED,
//                    Properties(),
//                )
//            })
//            bottomSheetDialogReport.show()
        }

        private fun showReportDialog() {
//            val mDialog = Dialog(context)
//            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//            mDialog.setContentView(R.layout.dialog_common_new)
//            mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            mDialog.window!!.setLayout(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//            mDialog.setOnCancelListener { }
//            mDialog.show()
//            val tvTitle = mDialog.findViewById<CustomTextView>(R.id.dialog_title)
//            val tvMsg = mDialog.findViewById<CustomTextView>(R.id.dialog_message)
//            val btnOkay = mDialog.findViewById<MaterialButton>(R.id.btnOkay)
//            tvTitle.text = context.resources.getString(R.string.video_reported)
//            tvMsg.text = context.resources.getString(R.string.report_review)
//            btnOkay.text = context.resources.getString(R.string.txt_ok)
//            btnOkay.setOnClickListener { v: View? ->
//                mDialog.dismiss()
//            }
        }

        private fun sendDataDogLogs(key: String, properties: Properties) {
//            if (key == Constants.VIDEO_WATCHED || key == Constants.SWIPE_UP || key == Constants.SWIPE_DOWN) {
//                properties[Constants.KEY_CONTENT_ID] = messageModel?.messageId
//            } else {
//                properties[Constants.KEY_CONTENT_ID] = messageModel?.chatId
//            }
//            properties[Constants.KEY_CONTENT_CATEGORY] = Constants.CATEGORY_RT
//            properties[Constants.KEY_EVENT_RECORD_SCREEN] = Constants.SCREEN_FEED_LOOP
//            properties[Constants.KEY_EVENT_TARGET_SCREEN] = Constants.NONE
//            GenuInApplication.getInstance().sendEventLogs(key, properties)
        }

        private fun callShareCountApi() {
//            try {
//                var currentVideoId = ""
//                if (exploreViewModel?.type == ExploreVideoType.PUBLIC_VIDEO) {
//                    currentVideoId = (exploreViewModel.obj as DiscoverModel).videoId
//                }
//                val module = Constants.SHARE_COUNT
//                val jsonObject = JSONObject()
//                BaseAPIService(
//                    context,
//                    module + currentVideoId,
//                    Utility.getRequestBody(jsonObject.toString()),
//                    true,
//                    null,
//                    "POST",
//                    false
//                )
//            } catch (e: Exception) {
//                Utility.showLogException(e)
//            }
        }

        private fun openBottomSheetDialogForFlagVideo(discoverModel: DiscoverModel) {
//            val bottomSheetView: View =
//                (context as Activity).layoutInflater.inflate(R.layout.bottom_sheet_flag_video, null)
//            val rlUnlisted = bottomSheetView.findViewById<RelativeLayout>(R.id.rlUnlisted)
//            val tvContactUs = bottomSheetView.findViewById<TextView>(R.id.tvContactUs)
//            tvContactUs.setOnClickListener { v: View? ->
//                bottomSheetDialogFlaggedVideo.dismiss()
//                context.startActivity(
//                    Intent(context, ContactUsActivityNew::class.java)
//                        .putExtra("video_id", discoverModel.videoId)
//                )
//                context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//            }
//            rlUnlisted.setOnClickListener { v: View? -> bottomSheetDialogFlaggedVideo.dismiss() }
//            bottomSheetDialogFlaggedVideo =
//                BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
//            bottomSheetDialogFlaggedVideo.setContentView(bottomSheetView)
//            bottomSheetDialogFlaggedVideo.setCancelable(true)
//            //        bottomSheetDialogPrivacyOptions.setOnCancelListener(dialogInterface -> GenuInApplication.getInstance().sendSegmentLogs(Constants.CAMERA_QUICK_MENU_CLOSED, propertiesClose));
//            bottomSheetDialogFlaggedVideo.show()
        }
    }
}