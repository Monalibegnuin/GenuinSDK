package com.begenuin.library.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
Provides information about loops i.e chat_id, conversation_type,
share_url, settings, group, latest_messages and other required info
 */
class LoopsModel {
    @SerializedName("chat_id")
    @Expose
    var chatId: String? = null

    @SerializedName("type")
    @Expose
    var convType: Int = 0

    @SerializedName("share_url")
    @Expose
    var shareUrl: String? = null

    @SerializedName("settings")
    @Expose
    var settings: SettingsModel? = null

    @SerializedName("latest_message_at")
    @Expose
    var latestMessageAt: String? = null

    @SerializedName("group")
    @Expose
    var group: GroupModel? = null

    @SerializedName("unread_message_count")
    @Expose
    var unreadMessageCount: String? = null

    @SerializedName("latest_messages")
    @Expose
    var latestMessages: ArrayList<MessageModel>? = null

    @SerializedName("no_of_views")
    @Expose
    var noOfViews: String? = null

    @SerializedName("member_info")
    @Expose
    var memberInfo: MemberInfoModel? = null

//    @SerializedName("latest_activities")
//    @Expose
//    var latestActivities: LatestActivitiesModel? = null

    @SerializedName("is_subscriber")
    @Expose
    var isSubscriber: Boolean = false

    @SerializedName("community")
    @Expose
    var community: CommunityModel? = null

    @SerializedName("owner")
    @Expose
    var owner: MembersModel? = null

    // Store local video path in case of loop is in uploading mode
    var localVideoPath: String? = null

    // Store messages which are in currently uploading or failure state
    var pendingUploadList: List<MessageModel>? = null

    var isExpanded: Boolean = false

    var communityId: String? = null

    var templateId: Int? = 0

    var isWelcomeLoop: Boolean = false

}