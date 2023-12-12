package com.begenuin.library.data.model

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.begenuin.library.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.core.interfaces.ResponseListener
import com.begenuin.library.data.remote.BaseAPIService
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

/**
 *  Provides information about message of loops i.e media_url, thumbnail_url
 *  message_id, message_at, message_owner and other required info
 */
class MessageModel : java.io.Serializable{
    @SerializedName("thumbnail_url")
    @Expose
    var thumbnailUrl: String? = null

    @SerializedName("media_url")
    @Expose
    var mediaUrl: String? = null

    @SerializedName("media_url_m3u8")
    @Expose
    var mediaUrlM3U8: String? = null

    @SerializedName("chat_id")
    @Expose
    var chatId: String? = null

    @SerializedName("message_id")
    @Expose
    var messageId: String? = null

    @SerializedName("message_at")
    @Expose
    var messageAt: String? = null

    @SerializedName("is_read")
    @Expose
    var isRead = false

    @SerializedName("attached_link")
    @Expose
    var link: String? = null

    @SerializedName("thumbnail_url_s")
    @Expose
    var videoThumbnailSmall: String? = null

    @SerializedName("thumbnail_url_l")
    @Expose
    var videoThumbnailLarge: String? = null

    @SerializedName("owner")
    @Expose
    var owner: MembersModel? = null

    @SerializedName("share_url")
    @Expose
    var shareURL: String? = null

    @SerializedName("no_of_views")
    @Expose
    var noOfViews: String? = null

    @SerializedName("no_of_comments")
    @Expose
    var noOfComments: String? = null

    @SerializedName("questions")
    @Expose
    var questions: List<QuestionModel>? = null

    @SerializedName("meta_data")
    @Expose
    var metaData: MetaDataModel? = null

    @SerializedName("repost")
    @Expose
    var repostModel: RepostModel? = null

    @SerializedName("message_summary")
    @Expose
    var messageSummary: String? = null

    @SerializedName("is_pinned")
    @Expose
    var isPinned: Boolean = false

    @SerializedName("no_of_sparks")
    @Expose
    var sparkCount: String = "0"

    @SerializedName("is_sparked")
    @Expose
    var isSparked = false

    /**
    For Loop Video ::
    videoUploadStatus => 0 - Pending, 1 - In Progress or Failed, 2 - Completed(video upload), 3 - API call completed.
    imageUploadStatus => 0 - Pending, 1 - In Progress or Failed, 2 - Completed
    dpUploadStatus => 0 - Pending, 1 - In Progress or Failed, 2 - Completed
    compressionStatus => 0 - Pending or In Progress or Failed, 1 - Completed
     * */

    var convType = 0
    var videoUploadStatus = 0
    var imageUploadStatus = 0
    var dpUploadStatus = 0
    var compressionStatus = 0

    // Store the uploading progress of message in case of uploading state
    var uploadProgress = 0

    // isRetry will be true in case of message being uploading is failed
    var isRetry = false

    // Store the local Video path of message in case of uploading state
    var localVideoPath: String? = null

    // Store the local Image path of message in case of uploading state
    var localImagePath: String? = null

    // Store the ffMpeg Command of message in case of uploading statue
    var ffMpegCommand: String? = null

    // Below params is used for analytics
    var isEventLogged = false
    var isViewCountUpdated = false

    var isDeleted = false

    fun setSparkStatus(spark: Boolean) {
        isSparked = spark
        if (!TextUtils.isEmpty(sparkCount)) {
            var noOfSparks = sparkCount.toLong()
            if (spark) {
                noOfSparks++
            } else {
                noOfSparks--
            }
            sparkCount = noOfSparks.toString()
        }
    }

    fun getRecordedByText(): String {
        return if (metaData != null && metaData?.containsExternalVideos == true) {
            "/ From camera roll"
        } else {
            ""
        }
    }

    fun getVideoURL(): String? {
        return if (!TextUtils.isEmpty(localVideoPath)) {
            localVideoPath
        } else if (!TextUtils.isEmpty(mediaUrlM3U8)) {
            mediaUrlM3U8
        } else {
            mediaUrl
        }
    }

    fun getVideoThumbnailURL(): String? {
        return if (!TextUtils.isEmpty(localImagePath)) {
            localImagePath
        } else if (!TextUtils.isEmpty(videoThumbnailLarge)) {
            videoThumbnailLarge
        } else {
            thumbnailUrl
        }
    }

    fun isVideoDeleted(): Boolean {
        return (isDeleted || (repostModel != null && repostModel!!.isDeleted))
    }

    fun isVideoAndImageUploaded(): Boolean {
        return (imageUploadStatus == 2 && videoUploadStatus == 2 && dpUploadStatus == 2)
    }

    fun readVideo(context: Activity?, loggedInUserId: String?, chatId: String?) {
        if (owner == null || owner!!.userId.equals(loggedInUserId, ignoreCase = true) || isRead) {
            return
        }
        if (Utility.isNetworkAvailable(context)) {
            var isNeedToUpdateCount = false
//            try {
//                if (Utility.getDBHelper() != null) {
//                    isNeedToUpdateCount =
//                        Utility.getDBHelper().updateMessageReadStatus(messageId, chatId)
//                }
//                isRead = true
//                val module = Constants.READ + messageId
//                val jsonObject = JSONObject()
//                BaseAPIService(
//                    context,
//                    module,
//                    Utility.getRequestBody(jsonObject.toString()),
//                    true,
//                    object : ResponseListener {
//                        override fun onSuccess(response: String) {
//                            if (isNeedToUpdateCount) {
//                                EventBus.getDefault().post(RefreshUnReadMessageCountEvent())
//                            }
//                        }
//
//                        override fun onFailure(error: String) {
//
//                        }
//                    },
//                    "POST",
//                    false
//                )
//            } catch (e: Exception) {
//                Utility.showLogException(e)
//            }
        }
    }

    fun viewVideo(activity: Context?, convType: Int, screenName: String?) {
        try {
            var afTopic: String? = ""
            var from = ""
            if (metaData != null && metaData!!.topic != null) {
                afTopic = Utility.getAFTopic(activity, metaData!!.topic)
            }
            val isQuestion = questions != null && questions!!.isNotEmpty()
            when (convType) {
                VideoConvType.ROUND_TABLE.value -> {
                    from = Constants.FROM_ROUND_TABLE
                }
                VideoConvType.GROUP.value -> {
                    from = Constants.FROM_GROUP
                }
                VideoConvType.DIRECT.value -> {
                    from = Constants.FROM_DIRECT
                }
                VideoConvType.REACTION.value -> {
                    from = Constants.FROM_REACTION
                }
            }
            //Utility.logAFVideoWatched(chatId, from, afTopic, isQuestion)
           // val loggedInUserId = SharedPrefUtils.getStringPreference(activity, Constants.PREF_USER)
            val loggedInUserId = ""
            if (owner != null && owner!!.userId.equals(loggedInUserId, ignoreCase = true)) {
                return
            }
            isViewCountUpdated = true
            val module = Constants.VIEW_VIDEO
            val jsonObject = JSONObject()
            jsonObject.put("video_id", messageId)
            jsonObject.put("type", 2)
            jsonObject.put("screen_name", screenName)
            BaseAPIService(
                activity,
                module,
                Utility.getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        isViewCountUpdated = false
                    }

                    override fun onFailure(error: String) {
                        isViewCountUpdated = false
                    }
                },
                "PUT",
                false
            )
        } catch (e: java.lang.Exception) {
            Utility.showLogException(e)
        }
    }
}