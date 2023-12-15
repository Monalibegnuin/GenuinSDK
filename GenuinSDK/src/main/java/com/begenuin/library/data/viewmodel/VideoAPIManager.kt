package com.begenuin.library.data.viewmodel

import android.app.Activity
import android.text.TextUtils
import com.begenuin.library.SDKInitiate
import com.begenuin.library.common.Constants
import com.begenuin.library.common.Utility
import com.begenuin.library.core.enums.VideoConvType
import com.begenuin.library.core.interfaces.ResponseListener
import com.begenuin.library.data.eventbus.ConversationUpdateEvent
import com.begenuin.library.data.eventbus.LoopVideoAPICompleteEvent
import com.begenuin.library.data.model.ChatModel
import com.begenuin.library.data.model.ConversationModel
import com.begenuin.library.data.model.GroupModel
import com.begenuin.library.data.model.LoopsModel
import com.begenuin.library.data.model.MembersModel
import com.begenuin.library.data.model.MessageModel
import com.begenuin.library.data.model.MetaDataModel
import com.begenuin.library.data.model.SettingsModel
import com.begenuin.library.data.model.VideoParamsModel
import com.begenuin.library.data.remote.BaseAPIService
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern
import com.begenuin.library.data.eventbus.ConversationDeleteEvent

object VideoAPIManager {
    /*
     * This is a singleton object for managing the API calls to store the uploaded video information.
     */

    fun sendReplyAPI(context: Activity, chatId: String, extraParams: VideoParamsModel) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("aspect_ratio", extraParams.aspectRatio)
            jsonObject.put("duration", extraParams.duration)
            jsonObject.put("link", extraParams.link)
            jsonObject.put("resolution", extraParams.resolution)
            jsonObject.put("size", extraParams.size)
            jsonObject.put("chat_id", chatId)
            jsonObject.put("video_name", extraParams.videoFileName)
            jsonObject.put("thumbnail_name", extraParams.imageFileName)
            if (!TextUtils.isEmpty(extraParams.selectedQuestions)) {
                val jsonArray = JSONArray(extraParams.selectedQuestions)
                jsonObject.put("questions", jsonArray)
            }
            if (!TextUtils.isEmpty(extraParams.metaData)) {
                val jsonMetaData = JSONObject(extraParams.metaData)
                jsonObject.put("meta_data", jsonMetaData)
            }
            BaseAPIService(
                context,
                Constants.SEND_REPLY,
                Utility.getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        Utility.printResponseLog(response)
                        try {
                            //if (!FeedActivity.activity.isSocketConnected) {
                                val jObject = JSONObject(response)
                                val dataJson = jObject.getJSONObject(Constants.JSON_DATA)
                                manageReplySuccess(context, dataJson)
                            //}
                        } catch (e: java.lang.Exception) {
                            Utility.showLogException(e)
                        }
                    }

                    override fun onFailure(error: String) {
                        Utility.printErrorLog(error)
                        if (error.equals("404", ignoreCase = true)) {
                            if (!TextUtils.isEmpty(chatId) && Utility.getDBHelper() != null) {
                                Utility.getDBHelper()!!.deleteChat(chatId)
                            }
                            context.runOnUiThread {
                                // Conversation delete event broadcast with chatId
                                val deleteEvent = ConversationDeleteEvent()
                                deleteEvent.chatId = chatId
                                EventBus.getDefault().post(deleteEvent)
                            }
                        } else {
                            val jsonErrorObject: JSONObject
                            try {
                                jsonErrorObject = JSONObject(error)
                                val code = jsonErrorObject.optString("code", "")
                                var chatID2 = ""
                                val dataJson = jsonErrorObject.optJSONObject(Constants.JSON_DATA)
                                if (dataJson != null) {
                                    chatID2 = dataJson.optString("chat_id", "")
                                }
                                if (code.equals(Constants.CODE_5057, ignoreCase = true)) {
                                    if (!TextUtils.isEmpty(chatID2) && Utility.getDBHelper() != null) {
                                        Utility.getDBHelper()!!.deleteChatWithStatus(chatID2)
                                    }
                                    context.runOnUiThread {
                                        // Conversation delete event broadcast with chatId
                                        val deleteEvent = ConversationDeleteEvent()
                                        deleteEvent.chatId = chatID2
                                        EventBus.getDefault().post(deleteEvent)
                                    }
                                } else {
                                    if (Utility.getDBHelper() != null) {
                                        Utility.getDBHelper()!!
                                            .updateRetryStatus(extraParams.videoFile, true)
                                    }
                                    context.runOnUiThread {
                                        EventBus.getDefault().post(ConversationUpdateEvent(true))
                                        //EventBus.getDefault().post(ConversationUpdateEvent(false))
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }, "POST", false
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun sendVideoAPI(context: Activity, from: String, extraParams: VideoParamsModel) {
        val videoFile = File(extraParams.videoFile)
        var fileName = videoFile.name
        val imageFile = File(extraParams.imageFile)
        var imageFileName = imageFile.name
        val module = Constants.CREATE_VIDEO
        try {
            val jsonObject = JSONObject()
            if (from.equals(Constants.FROM_REACTION, ignoreCase = true)) {
                jsonObject.put("type", 1)
                jsonObject.put("public_video_id", extraParams.publicVideoId)
            } else if (from.equals(Constants.FROM_DIRECT, ignoreCase = true)) {
                jsonObject.put("type", 2)
            } else if (from.equals(Constants.FROM_GROUP, ignoreCase = true)) {
                jsonObject.put("type", 3)
            } else if (from.equals(Constants.FROM_ROUND_TABLE, ignoreCase = true)) {
                jsonObject.put("type", 4)
                if (extraParams.settingsModel != null) {
                    val settingsJson = Gson().toJson(
                        extraParams.settingsModel,
                        SettingsModel::class.java
                    )
                    jsonObject.put("settings", settingsJson)
                }
            }
            if (fileName.startsWith("_")) {
                val userId =
                    SDKInitiate.userId
                fileName = userId + fileName
            }
            if (imageFileName.startsWith("_")) {
                val userId =
//                    SharedPrefUtils.getStringPreference(
//                        context,
//                        Constants.PREF_USER
//                    )
                    SDKInitiate.userId
                imageFileName = userId + imageFileName
            }
            if (!TextUtils.isEmpty(extraParams.dpFileName) && extraParams.dpFileName.startsWith("_")) {
                val userId =
//                    SharedPrefUtils.getStringPreference(
//                        context,
//                        Constants.PREF_USER
//                    )
                    SDKInitiate.userId
                extraParams.dpFileName = userId + extraParams.dpFileName
            }
            val jsonRecordedVideo = JSONObject()
            jsonRecordedVideo.put("video_name", fileName)
            jsonRecordedVideo.put("thumbnail_name", imageFileName)
            jsonRecordedVideo.put(
                "link",
                if (TextUtils.isEmpty(extraParams.link)) "" else extraParams.link
            )
            jsonRecordedVideo.put("size", extraParams.size)
            jsonRecordedVideo.put("duration", extraParams.duration)
            jsonRecordedVideo.put("aspect_ratio", extraParams.aspectRatio)
            jsonRecordedVideo.put("resolution", extraParams.resolution)
            if (!TextUtils.isEmpty(extraParams.metaData)) {
                val jsonMetaData = JSONObject(extraParams.metaData)
                jsonRecordedVideo.put("meta_data", jsonMetaData)
            }
            jsonObject.put("recorded_video", jsonRecordedVideo)
            if (!TextUtils.isEmpty(extraParams.selectedQuestions)) {
                val jsonArray = JSONArray(extraParams.selectedQuestions)
                jsonObject.put("questions", jsonArray)
            }
            if (!TextUtils.isEmpty(extraParams.shareURL)) {
                jsonObject.put("share_string", extraParams.shareURL)
            }
            if (!TextUtils.isEmpty(extraParams.communityId)) {
                jsonObject.put("community_id", extraParams.communityId)
            }
            //if (!TextUtils.isEmpty(extraParams.selectedContacts)) {
                try {
                    //val userId = SharedPrefUtils.getStringPreference(context, Constants.PREF_USER)
                    val userId = SDKInitiate.userId
                    val gson = Gson()
                    val contactListType =
                        object : TypeToken<java.util.ArrayList<MembersModel?>?>() {}.type
                    val contactList =
                        gson.fromJson<List<MembersModel>>(
                            extraParams.selectedContacts,
                            contactListType
                        )
                    val groupObj = JSONObject()
                    if (from.equals(Constants.FROM_DIRECT, ignoreCase = true)) {
                        groupObj.put("name", null)
                        groupObj.put("description", null)
                        groupObj.put("tags", null)
                    } else if (from.equals(Constants.FROM_GROUP, ignoreCase = true)) {
                        groupObj.put(
                            "name",
                            if (TextUtils.isEmpty(extraParams.groupName)) null else extraParams.groupName
                        )
                        groupObj.put(
                            "description",
                            if (TextUtils.isEmpty(extraParams.groupDesc)) null else extraParams.groupDesc
                        )
                        groupObj.put("tags", null)
                    } else if (from.equals(Constants.FROM_ROUND_TABLE, ignoreCase = true)) {
                        groupObj.put(
                            "name",
                            if (TextUtils.isEmpty(extraParams.rtName)) "TestSDKLoop" else extraParams.rtName
                        )
                        groupObj.put(
                            "description",
                            if (TextUtils.isEmpty(extraParams.rtDesc)) "Added for testing" else extraParams.rtDesc
                        )
                        groupObj.put(
                            "tags",
                            if (TextUtils.isEmpty(extraParams.rtDesc)) "HAshtag added for testing" else getHashTagList(
                                extraParams.rtDesc
                            )
                        )
                    }
                    if (!TextUtils.isEmpty(extraParams.dpFileName)) {
                        groupObj.put("dp", extraParams.dpFileName)
                    }
                    val memberArray = JSONArray()
//                    for (contact in contactList) {
//                        val memberObj = JSONObject()
//                        if (!TextUtils.isEmpty(contact.nickname)) {
//                            if (!userId.equals(contact.userId, ignoreCase = true)) {
//                                memberObj.put("user_id", contact.userId)
//                                memberArray.put(memberObj)
//                            }
//                        } else {
//                            memberObj.put("phone_number", contact.phone)
//                            if (!TextUtils.isEmpty(contact.name)) {
//                                memberObj.put("name", contact.name)
//                            }
//                            memberArray.put(memberObj)
//                        }
//                    }
                    groupObj.put("members", memberArray)
                    jsonObject.put("group", groupObj)
                    if (extraParams.templateId != 0) {
                        jsonObject.put("template_id", extraParams.templateId)
                    }
                    if (extraParams.isWelcomeLoop) {
                        jsonObject.put("welcome_loop", extraParams.isWelcomeLoop)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            //}
            BaseAPIService(
                context,
                module,
                Utility.getRequestBody(jsonObject.toString()),
                true,
                object : ResponseListener {
                    override fun onSuccess(response: String) {
                        // Video successfully updated to server.
                        context.runOnUiThread {
                            if (Utility.getDBHelper() != null) {
                                try {
                                    val jsonObject1 = JSONObject(response)
                                    val dataJson =
                                        jsonObject1.getJSONObject(Constants.JSON_DATA)
                                    val chatId = dataJson.optString("chat_id", "")
                                    val shareURL = dataJson.optString("share_url", "")
                                    val videoURL = dataJson.optString("video_url", "")
                                    val groupId = dataJson.optString("group_id", "")
                                    val conversationId = dataJson.optString("conversation_id", "")
                                    var memberList: List<MembersModel> = ArrayList()
                                    if (dataJson.has("members")) {
                                        val gson = Gson()
                                        val memberListType = object :
                                            com.google.gson.reflect.TypeToken<java.util.ArrayList<MembersModel?>?>() {}.type
                                        memberList =
                                            gson.fromJson(
                                                dataJson.getString("members"),
                                                memberListType
                                            )
                                    }
                                    if (from.equals(
                                            Constants.FROM_DIRECT,
                                            ignoreCase = true
                                        ) || from.equals(
                                            Constants.FROM_GROUP,
                                            ignoreCase = true
                                        )
                                    ) {
                                        if (!TextUtils.isEmpty(extraParams.selectedContacts)) {
//                                            Utility.checkMembersCanPrivateChat(
//                                                extraParams.selectedContacts
//                                            )
                                        }
                                        //if (!FeedActivity.activity.isSocketConnected) {
                                            if (!TextUtils.isEmpty(chatId) && !TextUtils.isEmpty(extraParams.videoURL)) {
                                                Utility.getDBHelper()!!
                                                    .updateChatIdInReaction(
                                                        extraParams.videoURL,
                                                        chatId
                                                    )
                                                EventBus.getDefault()
                                                    .post(ConversationUpdateEvent(false))
                                            }
//                                        } else {
//                                            Utility.getDBHelper()!!
//                                                .deleteReaction(extraParams.videoFile)
//                                        }
                                    } else
                                        if (from.equals(Constants.FROM_ROUND_TABLE, ignoreCase = true)) {
//                                        val map =
//                                            java.util.HashMap<String, Any>()
//                                        map[Constants.KEY_CONTENT_ID] = chatId
//                                        map[Constants.KEY_CONTENT_CATEGORY] =
//                                            Utility.getContentType(from)
//                                        val jsonMetaDataObj = JSONObject(extraParams.metaData)
//                                        val topic =
//                                            Utility.getAFTopic(
//                                                context,
//                                                jsonMetaDataObj.optString("topic", "")
//                                            )
//                                        if (!TextUtils.isEmpty(topic)) {
//                                            map[Constants.KEY_STICKER_LABEL] =
//                                                topic
//                                        }
//                                        map[Constants.KEY_QUESTION] =
//                                            !TextUtils.isEmpty(extraParams.selectedQuestions)
//                                        GenuInApplication.getInstance().sendEventLogs(
//                                            Constants.RT_VIDEO_PUBLISHED,
//                                            map,
//                                            LogType.EVENT
//                                        )

                                        //if (!FeedActivity.activity.isSocketConnected) {
                                            val loopsModel = LoopsModel()
                                            loopsModel.chatId = chatId
                                            val loopMessages = ArrayList<MessageModel>()
                                            val messageModel = MessageModel()
                                            messageModel.messageId = conversationId
                                            loopMessages.add(messageModel)
                                            loopsModel.latestMessages = loopMessages
                                            val groupModel = GroupModel()
                                            groupModel.groupId = groupId
                                            groupModel.members = memberList
                                            loopsModel.group = groupModel
                                            if (!TextUtils.isEmpty(videoURL) && !TextUtils.isEmpty(
                                                    chatId
                                                ) && !TextUtils.isEmpty(
                                                    extraParams.videoURL
                                                )
                                            ) {

                                                val videoFileName =
                                                    videoURL.substring(videoURL.lastIndexOf('/') + 1)
                                                val destinationLocation: File? =
                                                    context.getExternalFilesDir(Constants.MERGE_DIRECTORY)
                                                val cacheLocation: File = context.cacheDir
                                                val localPath =
                                                    destinationLocation?.absolutePath + File.separator + videoFileName
                                                val cachedPath =
                                                    cacheLocation.absolutePath + File.separator + videoFileName
                                                Utility.getDBHelper()!!
                                                    .updateLoopRelatedIdsByLocalPath(
                                                        loopsModel,
                                                        localPath,
                                                        cachedPath
                                                    )

                                                val event = LoopVideoAPICompleteEvent()
                                                event.localVideoPath = localPath
                                                event.createdLoopId = chatId
                                                event.newMessageId = conversationId
                                                EventBus.getDefault().post(event)
                                            }
                                        //}
//                                        val event = ShareURLRTEventEvent()
//                                        event.videoPath = extraParams.videoFile
//                                        event.shareURL = shareURL
//                                        event.chatId = chatId
//                                        EventBus.getDefault().post(event)
                                    } else if (from.equals(
                                            Constants.FROM_REACTION,
                                            ignoreCase = true
                                        )
                                    ) {
                                        try {
                                            Utility.showLog(
                                                "Tag",
                                                "Reaction success"
                                            )
//                                            if (!FeedActivity.activity.isSocketConnected) {
//                                                if (!TextUtils.isEmpty(chatId) && !TextUtils.isEmpty(
//                                                        extraParams.videoURL
//                                                    )
//                                                ) {
//                                                    Utility.getDBHelper()
//                                                        .updateChatIdInReaction(
//                                                            extraParams.videoURL,
//                                                            chatId
//                                                        )
//                                                    EventBus.getDefault()
//                                                        .post(ConversationUpdateEvent(false))
//                                                }
//                                            }
                                        } catch (e: java.lang.Exception) {
                                            Utility.showLogException(e)
                                        }
                                    }
                                } catch (e: java.lang.Exception) {
                                    Utility.showLogException(e)
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                        context.runOnUiThread {
                            if (from.equals(
                                    Constants.FROM_DIRECT,
                                    ignoreCase = true
                                ) || from.equals(
                                    Constants.FROM_GROUP,
                                    ignoreCase = true
                                )
                            ) {
                                if (Utility.getDBHelper() != null) {
                                    Utility.getDBHelper()!!
                                        .updateRetryStatus(
                                            extraParams.videoFile, true
                                        )
                                }
                                EventBus.getDefault()
                                    .post(ConversationUpdateEvent(false))

                            } else if (from.equals(
                                    Constants.FROM_ROUND_TABLE,
                                    ignoreCase = true
                                )
                            ) {
                                if (Utility.getDBHelper() != null) {
                                    Utility.getDBHelper()!!
                                        .updateRetryStatus(
                                            extraParams.videoFile, true
                                        )
                                }
                                EventBus.getDefault()
                                    .post(ConversationUpdateEvent(true))
                            } else if (from.equals(
                                    Constants.FROM_REACTION,
                                    ignoreCase = true
                                )
                            ) {
                                val jsonObject1: JSONObject
                                try {
                                    jsonObject1 = JSONObject(error)
                                    val code = jsonObject1.optString("code", "")
                                    if (code.equals(
                                            Constants.CODE_5061,
                                            ignoreCase = true
                                        )
                                    ) {
                                        if (!TextUtils.isEmpty(extraParams.videoURL) && Utility.getDBHelper() != null) {
                                            Utility.getDBHelper()!!
                                                .deleteReaction(extraParams.videoURL)
                                        }
                                    } else {
                                        if (Utility.getDBHelper() != null) {
                                            Utility.getDBHelper()!!
                                                .updateRetryStatus(
                                                    extraParams.videoFile, true
                                                )
                                        }
                                    }
                                    EventBus.getDefault()
                                        .post(ConversationUpdateEvent(false))
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                },
                "POST",
                false
            )
        } catch (e: java.lang.Exception) {
            Utility.showLogException(e)
        }
    }

//    fun sendCommentAPI(context: Activity, comment: CommentModel) {
//        val extraParams = VideoParamsModel()
//        extraParams.videoFile = comment.fileLocalVideoPath
//        extraParams.imageFile = comment.imageLocalVideoPath
//        extraParams.link = comment.link
//        extraParams.duration = comment.duration
//        extraParams.fileType = comment.fileType
//        if (comment.questions != null && comment.questions.size > 0) {
//            val jsonArray = JSONArray()
//            for (question in comment.questions) {
//                jsonArray.put(question.questionId)
//            }
//            extraParams.selectedQuestions = jsonArray.toString()
//        }
//        if (comment.metaData != null) {
//            extraParams.metaData = Gson().toJson(comment.metaData, MetaDataModel::class.java)
//        }
//        if (!TextUtils.isEmpty(comment.commentText)) {
//            extraParams.commentText = comment.commentText
//        }
//        if (!TextUtils.isEmpty(comment.commentData)) {
//            extraParams.commentData = comment.commentData
//        }
//        sendCommentAPI(context, comment.chatId, comment.videoId, extraParams)
//    }
//
//    fun sendCommentAPI(
//        context: Activity,
//        chatId: String,
//        videoId: String,
//        extraParams: VideoParamsModel
//    ) {
//        val module = Constants.CREATE_COMMENT
//        try {
//            val jsonObject = JSONObject()
//            if (extraParams.fileType != CommentFileType.TEXT.value) {
//                val videoFile = File(extraParams.videoFile)
//                var fileName = videoFile.name
//                val imageFile = File(extraParams.imageFile)
//                var imageFileName = imageFile.name
//                if (fileName.startsWith("_")) {
//                    val userId = SharedPrefUtils.getStringPreference(
//                        context,
//                        Constants.PREF_USER
//                    )
//                    fileName = userId + fileName
//                }
//                if (imageFileName.startsWith("_")) {
//                    val userId = SharedPrefUtils.getStringPreference(
//                        context,
//                        Constants.PREF_USER
//                    )
//                    imageFileName = userId + imageFileName
//                }
//                jsonObject.put("file_name", fileName)
//                jsonObject.put("thumbnail_name", imageFileName)
//            }
//            jsonObject.put("conversation_id", videoId)
//            jsonObject.put("chat_id", chatId)
//            jsonObject.put(
//                "link",
//                if (TextUtils.isEmpty(extraParams.link)) "" else extraParams.link
//            )
//            jsonObject.put("duration", extraParams.duration)
//            jsonObject.put("type", extraParams.fileType)
//            if (!TextUtils.isEmpty(extraParams.commentText)) {
//                jsonObject.put("comment_text", extraParams.commentText)
//            }
//            if (!TextUtils.isEmpty(extraParams.commentData)) {
//                jsonObject.put("comment_data", extraParams.commentData)
//            }
//
//            if (!TextUtils.isEmpty(extraParams.selectedQuestions)) {
//                val jsonArray = JSONArray(extraParams.selectedQuestions)
//                jsonObject.put("questions", jsonArray)
//            }
//            if (!TextUtils.isEmpty(extraParams.metaData)) {
//                val jsonMetaData = JSONObject(extraParams.metaData)
//                jsonMetaData.put("duration", extraParams.duration)
//                jsonObject.put("meta_data", jsonMetaData)
//            }
//            BaseAPIService(
//                context,
//                module,
//                Utility.getRequestBody(jsonObject.toString()),
//                true,
//                object : ResponseListener {
//                    override fun onSuccess(response: String) {
//                        // Video successfully updated to server.
//                        context.runOnUiThread {
//                            if (Utility.getDBHelper() != null) {
//                                try {
//                                    val jResponseObject = JSONObject(response)
//                                    val dataJson =
//                                        jResponseObject.getJSONObject(Constants.JSON_DATA)
//                                    val gson = Gson()
//                                    val commentListData = object :
//                                        TypeToken<CommentModel?>() {}.type
//                                    val comment =
//                                        gson.fromJson<CommentModel>(
//                                            dataJson.toString(),
//                                            commentListData
//                                        )
//                                    Utility.getDBHelper()
//                                        .updateComment(comment, extraParams.videoFile)
//                                    val map =
//                                        java.util.HashMap<String, Any>()
//                                    map[Constants.KEY_CONTENT_ID] = comment.commentId
//                                    map[Constants.KEY_CONTENT_CATEGORY] =
//                                        Utility.getContentType(Constants.FROM_COMMENT)
//                                    if (!TextUtils.isEmpty(extraParams.metaData)) {
//                                        val jsonMetaDataObj = JSONObject(extraParams.metaData)
//                                        val topic = Utility.getAFTopic(
//                                            context,
//                                            jsonMetaDataObj.optString("topic", "")
//                                        )
//                                        if (!TextUtils.isEmpty(topic)) {
//                                            map[Constants.KEY_STICKER_LABEL] = topic
//                                        }
//                                    }
//                                    map[Constants.KEY_QUESTION] =
//                                        !TextUtils.isEmpty(extraParams.selectedQuestions)
//                                    GenuInApplication.getInstance().sendEventLogs(
//                                        Constants.COMMENTED_ON_A_VIDEO,
//                                        map,
//                                        LogType.EVENT
//                                    )
//                                    val commentEvent = PostCommentEvent()
//                                    commentEvent.isRetry = false
//                                    commentEvent.localFilePath = extraParams.videoFile
//                                    commentEvent.commentModel = comment
//                                    EventBus.getDefault().post(commentEvent)
//                                } catch (e: java.lang.Exception) {
//                                    Utility.showLogException(e)
//                                }
//                            }
//                        }
//                    }
//
//                    override fun onFailure(error: String) {
//                        context.runOnUiThread {
//                            val jsonErrorObject: JSONObject
//                            try {
//                                jsonErrorObject = JSONObject(error)
//                                val code = jsonErrorObject.optString("code", "")
//                                if (code.equals(
//                                        Constants.CODE_5175,
//                                        ignoreCase = true
//                                    )
//                                ) {
//                                    // Already published video so delete from db.
//                                    if (Utility.getDBHelper() != null) {
//                                        Utility.getDBHelper()
//                                            .deleteCommentByPath(extraParams.videoFile)
//                                    }
//                                } else {
//                                    if (Utility.getDBHelper() != null) {
//                                        Utility.getDBHelper()
//                                            .updateCommentRetryStatus(extraParams.videoFile, true)
//                                    }
//                                    val commentEvent = PostCommentEvent()
//                                    commentEvent.isRetry = true
//                                    commentEvent.localFilePath = extraParams.videoFile
//                                    EventBus.getDefault().post(commentEvent)
//                                }
//                            } catch (e: java.lang.Exception) {
//                                e.printStackTrace()
//                                if (Utility.getDBHelper() != null) {
//                                    Utility.getDBHelper()
//                                        .updateCommentRetryStatus(extraParams.videoFile, true)
//                                }
//                                val commentEvent = PostCommentEvent()
//                                commentEvent.isRetry = true
//                                commentEvent.localFilePath = extraParams.videoFile
//                                EventBus.getDefault().post(commentEvent)
//                            }
//                        }
//                    }
//                },
//                "POST",
//                false
//            )
//        } catch (e: java.lang.Exception) {
//            Utility.showLogException(e)
//        }
//    }
//
//
//    fun retryAPIReaction(context: Activity, chat: ChatModel) {
//        val filePath = chat.localVideoPath
//        val imagePath = chat.imagePath
//        val videoName = filePath.substring(filePath.lastIndexOf('/') + 1)
//        val imageName = imagePath.substring(filePath.lastIndexOf('/') + 1)
//        val extraParams = VideoParamsModel()
//        extraParams.videoFile = filePath
//        extraParams.videoFileName = videoName
//        extraParams.imageFile = imagePath
//        extraParams.imageFileName = imageName
//        extraParams.link = chat.link
//        extraParams.duration = chat.duration
//        extraParams.resolution = chat.resolution
//        extraParams.aspectRatio = chat.aspectRatio
//        extraParams.size = chat.size
//        extraParams.metaData = Gson().toJson(chat.metaData, MetaDataModel::class.java)
//        if (chat.questions != null && chat.questions.size > 0) {
//            val jsonArray = JSONArray()
//            for (question in chat.questions) {
//                jsonArray.put(question.questionId)
//            }
//            extraParams.selectedQuestions = jsonArray.toString()
//        }
//        if (chat.convType == VideoConvType.REACTION.value) {
//            extraParams.publicVideoId = chat.conversationId
//            extraParams.videoURL = chat.videoUrl
//            sendVideoAPI(context, Constants.FROM_REACTION, extraParams)
//        } else {
//            if (chat.group != null && (chat.group.members != null) && chat.group.members.size > 0) {
//                val listType = object : TypeToken<List<MembersModel?>?>() {}.type
//                extraParams.selectedContacts = Gson().toJson(chat.group.members, listType)
//                if (chat.convType == VideoConvType.GROUP.value) {
//                    extraParams.groupName =
//                        if (TextUtils.isEmpty(chat.group.name)) "" else chat.group.name
//                    extraParams.groupDesc =
//                        if (TextUtils.isEmpty(chat.group.description)) "" else chat.group.description
//                } else if (chat.convType == VideoConvType.ROUND_TABLE.value) {
//                    extraParams.rtName =
//                        if (TextUtils.isEmpty(chat.group.name)) "" else chat.group.name
//                    extraParams.rtDesc =
//                        if (TextUtils.isEmpty(chat.group.description)) "" else chat.group.description
//                }
//                if (!TextUtils.isEmpty(chat.group.dp)) {
//                    val dpPath = chat.group.dp
//                    val dpName = dpPath.substring(filePath.lastIndexOf('/') + 1)
//                    extraParams.dpFile = dpPath
//                    extraParams.dpFileName = dpName
//                }
//            }
//            extraParams.videoURL = filePath
//            when (chat.convType) {
//                VideoConvType.DIRECT.value -> {
//                    sendVideoAPI(context, Constants.FROM_DIRECT, extraParams)
//                }
//                VideoConvType.GROUP.value -> {
//                    sendVideoAPI(context, Constants.FROM_GROUP, extraParams)
//                }
//                VideoConvType.ROUND_TABLE.value -> {
//                    extraParams.shareURL = chat.shareURL
//                    sendVideoAPI(context, Constants.FROM_ROUND_TABLE, extraParams)
//                }
//            }
//        }
//    }
//
    fun retryAPILoop(context: Activity, loopsModel: LoopsModel) {
        if (loopsModel.latestMessages != null && loopsModel.latestMessages!!.isNotEmpty()) {
            val messageModel = loopsModel.latestMessages!![0]
            val filePath = messageModel.localVideoPath
            val imagePath = messageModel.localImagePath
            val videoName = filePath?.substring(filePath.lastIndexOf('/') + 1)
            val imageName = imagePath?.substring(filePath!!.lastIndexOf('/') + 1)
            val extraParams = VideoParamsModel()
            extraParams.videoFile = filePath
            extraParams.videoFileName = videoName
            extraParams.imageFile = imagePath
            extraParams.imageFileName = imageName
            extraParams.link = messageModel.link
            if (messageModel.metaData != null) {
                extraParams.duration = messageModel.metaData?.duration
                extraParams.resolution = messageModel.metaData?.resolution
                extraParams.aspectRatio = messageModel.metaData?.aspectRatio
                extraParams.size = messageModel.metaData?.size
            }
            extraParams.metaData = Gson().toJson(messageModel.metaData, MetaDataModel::class.java)
            extraParams.settingsModel = loopsModel.settings

            if (messageModel.questions != null && messageModel.questions!!.isNotEmpty()) {
                val jsonArray = JSONArray()
                for (question in messageModel.questions!!) {
                    jsonArray.put(question.questionId)
                }
                extraParams.selectedQuestions = jsonArray.toString()
            }

            if (loopsModel.group != null && (loopsModel.group?.members != null) && loopsModel.group?.members!!.size > 0) {
                val listType = object : TypeToken<List<MembersModel?>?>() {}.type
                extraParams.selectedContacts =
                    Gson().toJson(loopsModel.group?.members, listType)
                extraParams.rtName =
                    if (TextUtils.isEmpty(loopsModel.group?.name)) "" else loopsModel.group?.name
                extraParams.rtDesc =
                    if (TextUtils.isEmpty(loopsModel.group?.description)) "" else loopsModel.group?.description
                if (!TextUtils.isEmpty(loopsModel.group?.dp)) {
                    val dpPath = loopsModel.group?.dp
                    val dpName = dpPath?.substring(filePath!!.lastIndexOf('/') + 1)
                    extraParams.dpFile = dpPath
                    extraParams.dpFileName = dpName
                }
            }
            extraParams.videoURL = filePath
            extraParams.shareURL = messageModel.shareURL
            if (!TextUtils.isEmpty(loopsModel.communityId)) {
                extraParams.communityId = loopsModel.communityId
            }
            sendVideoAPI(context, Constants.FROM_ROUND_TABLE, extraParams)
        }
    }

    fun retryAPIChat(context: Activity, chat: ChatModel) {
        val filePath = chat.localVideoPath
        val imagePath = chat.imagePath
        val videoName = filePath.substring(filePath.lastIndexOf('/') + 1)
        val imageName = imagePath.substring(filePath.lastIndexOf('/') + 1)
        val extraParams = VideoParamsModel()
        extraParams.videoFile = filePath
        extraParams.videoFileName = videoName
        extraParams.imageFile = imagePath
        extraParams.imageFileName = imageName
        extraParams.link = chat.link
        extraParams.duration = chat.duration
        extraParams.resolution = chat.duration
        extraParams.aspectRatio = chat.aspectRatio
        extraParams.size = chat.size
        extraParams.metaData = Gson().toJson(chat.metaData, MetaDataModel::class.java)
        if (chat.questions != null && chat.questions.size > 0) {
            val jsonArray = JSONArray()
            for (question in chat.questions) {
                jsonArray.put(question.questionId)
            }
            extraParams.selectedQuestions = jsonArray.toString()
        }
        sendReplyAPI(context, chat.chatId, extraParams)
    }


    fun retryAPILoopVideo(context: Activity, messageModel: MessageModel) {
        val filePath = messageModel.localVideoPath
        val imagePath = messageModel.localImagePath
        val videoName = filePath?.substring(filePath.lastIndexOf('/') + 1)
        val imageName = imagePath?.substring(filePath!!.lastIndexOf('/') + 1)
        val extraParams = VideoParamsModel()
        extraParams.videoFile = filePath
        extraParams.videoFileName = videoName
        extraParams.imageFile = imagePath
        extraParams.imageFileName = imageName
        extraParams.link = messageModel.link
        if (messageModel.metaData != null) {
            extraParams.duration = messageModel.metaData?.duration
            extraParams.resolution = messageModel.metaData?.resolution
            extraParams.aspectRatio = messageModel.metaData?.aspectRatio
            extraParams.size = messageModel.metaData?.size
        }
        extraParams.metaData = Gson().toJson(messageModel.metaData, MetaDataModel::class.java)

        if (messageModel.questions != null && messageModel.questions!!.isNotEmpty()) {
            val jsonArray = JSONArray()
            for (question in messageModel.questions!!) {
                jsonArray.put(question.questionId)
            }
            extraParams.selectedQuestions = jsonArray.toString()
        }
        sendReplyAPI(context, messageModel.chatId!!, extraParams)
    }

    fun manageReplySuccess(context: Activity, jsonObject: JSONObject) {
        try {
            val chatId = jsonObject.getString("chat_id")
            val convType = jsonObject.optInt("type", 0)
            val convShareURL = jsonObject.optString("share_url", "")
            val convNoOfViews = jsonObject.optString("no_of_views", "")
            context.runOnUiThread {
                if (Utility.getDBHelper() != null) {
                    try {
                        val gson = Gson()
                        val conversation = gson.fromJson(
                            jsonObject.toString(),
                            ConversationModel::class.java
                        )
                        if (conversation.chats != null && conversation.chats.size > 0) {
                            val chat = conversation.chats[0]
                            chat.chatId = chatId
                            chat.convShareURL = convShareURL
                            chat.convNoOfViews = convNoOfViews
                            val videoURL = chat.videoUrl
                            if (!TextUtils.isEmpty(videoURL)) {
                                val fileName =
                                    videoURL.substring(videoURL.lastIndexOf('/') + 1)
                                val destinationLocation: File? =
                                    context.getExternalFilesDir(Constants.MERGE_DIRECTORY)
                                val cacheLocation: File = context.cacheDir
                                val localPath =
                                    destinationLocation?.absolutePath + File.separator + fileName
                                val cachedPath =
                                    cacheLocation.absolutePath + File.separator + fileName
                                if (Utility.getDBHelper() != null) {
                                    if (convType == VideoConvType.ROUND_TABLE.value) {
                                        val message = Utility.convertChatIntoMessage(chat, chatId)
                                        Utility.getDBHelper()!!.updateMessageIdByLocalPath(
                                            message.messageId,
                                            localPath,
                                            cachedPath
                                        )
                                        Utility.getDBHelper()!!.insertLoopVideo(message)
                                        val event = LoopVideoAPICompleteEvent()
                                        event.localVideoPath = localPath
                                        event.newMessageId = message.messageId.toString()
                                        EventBus.getDefault().post(event)

                                        // To update the conversation in my profile
                                        val loopsModel = Utility.getDBHelper()!!
                                            .getLoopByChatIdAndMessageId(
                                                message.chatId,
                                                message.messageId
                                            )
                                        if (loopsModel != null) {
                                            val conversationModel =
                                                Utility.convertLoopModelIntoConversation(
                                                    context,
                                                    loopsModel
                                                )
                                            EventBus.getDefault().post(conversationModel)
                                        }
                                    } else {
                                        val isVideoUploadedForCurrentDevice =
                                            Utility.getDBHelper()!!
                                                .isVideoUploadedFromCurrentDevice(
                                                    localPath,
                                                    cachedPath
                                                )
                                        if (isVideoUploadedForCurrentDevice) {
                                            Utility.getDBHelper()!!
                                                .updateReply(chatId, chat, localPath, cachedPath)
                                        } else {
                                            Utility.getDBHelper()!!
                                                .insertReply(chatId, conversation.chats, convType)
                                        }
                                    }
                                }
                            }

                            val fromStr: String = when (convType) {
                                2 -> Constants.FROM_DIRECT
                                3 -> Constants.FROM_GROUP
                                4 -> Constants.FROM_ROUND_TABLE
                                else -> Constants.FROM_REACTION
                            }
//                            val map: HashMap<String, Any> = HashMap()
//                            map[Constants.KEY_CONTENT_ID] = chat.conversationId
//                            map[Constants.KEY_CONTENT_CATEGORY] = fromStr
//                            var topic = ""
//                            if (chat.metaData != null && chat.metaData.topic != null) {
//                                topic = Utility.getAFTopic(
//                                    context,
//                                    chat.metaData.topic
//                                )
//                            }
//                            if (!TextUtils.isEmpty(topic)) {
//                                map[Constants.KEY_STICKER_LABEL] = topic
//                            }
//                            map[Constants.KEY_QUESTION] =
//                                chat.questions != null && chat.questions.size > 0
//                            if (convType == VideoConvType.ROUND_TABLE.value) {
//                                GenuInApplication.getInstance().sendEventLogs(
//                                    Constants.RT_VIDEO_PUBLISHED,
//                                    map,
//                                    LogType.EVENT
//                                )
//                            } else {
//                                GenuInApplication.getInstance().sendEventLogs(
//                                    Constants.VIDEO_REPLIED,
//                                    map,
//                                    LogType.EVENT
//                                )
//                            }
                        }
                    } catch (e: java.lang.Exception) {
                        Utility.showLogException(e)
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getHashTagList(desc: String): String? {
        var hashTags = ""
        return try {
            val pattern = Pattern.compile("#(\\S+)")
            val matHash: Matcher = pattern.matcher(desc)
            val selectedHashList = ArrayList<String>() // Collect strings with #
            while (matHash.find()) {
                val dataToAdd = matHash.group(1)
                dataToAdd?.let { selectedHashList.add(it) }
            }

            //make comma separated # text
            if (selectedHashList.size > 0) {
                val stringIdBuilderHash = StringBuilder()
                for (i in selectedHashList.indices) {
                    stringIdBuilderHash.append(selectedHashList[i])
                    stringIdBuilderHash.append(",")
                }
                val sbBuilderStringHash = stringIdBuilderHash.toString()
                if (!TextUtils.isEmpty(sbBuilderStringHash)) {
                    //Remove last comma
                    hashTags = sbBuilderStringHash.substring(0, sbBuilderStringHash.length - 1)
                }
            }
            hashTags
        } catch (e: Exception) {
            e.printStackTrace()
            hashTags
        }
    }
}