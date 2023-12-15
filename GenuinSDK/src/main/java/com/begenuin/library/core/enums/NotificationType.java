package com.begenuin.library.core.enums;

public enum NotificationType {
    REPLY_RT("reply_rt"),
    LEAVE_RT("leave_rt"),
    INVITE_RT("invite_rt"),
    REMOVE_RT("remove_rt"),
    FLAG_PUBLIC_VIDEO("flag_public_video"),
    FLAG_VIDEO_REVIEWED("flag_video_reviewed"),
    REPLY_CHAT("reply_chat"),
    DELETE_CHAT("delete_chat"),
    LEAVE_CHAT("leave_chat"),
    FLAG_CHAT("flag_chat"),
    EXPIRE_CHAT("expire_chat"),
    EXPIRE_CHAT_FIRST("expire_chat_first"),
    EXPIRE_CHAT_SECOND("expire_chat_second"),
    EXPIRE_CHAT_THIRD("expire_chat_third"),
    INVITE_CHAT("invite_chat"),
    FEED_VIDEO("feed_video"),
    QUEUE("queue"),
    DELETE_VIDEO_RT("delete_video_rt"),
    DELETE_VIDEO_RT_BY_OWNER("delete_video_rt_by_owner"),
    DELETE_RT("delete_rt"),
    RT_COMMENT("rt_comment"),
    RT_COMMENT_TO_OTHER_USERS("rt_comment_to_other_users"),
    QUESTION_REDIRECT("question_redirect"),
    PROFILE_REDIRECT("profile_redirect"),
    QR_CODE_REDIRECT("qr_code_redirect"),
    RECORDED_PUBLIC_VIDEO("recorded_public_video"),
    NOTIFICATION_REMINDER("chat_reminder"),
    DM_COMMENT_TO_VIDEO_OWNER("dm_comment_to_video_owner"),
    DM_COMMENT_TO_OTHER_USERS("dm_comment_to_other_users"),
    LOCAL_PROFILE_VIDEO("local_profile_video"),
    LOCAL_INBOX_VIDEO("local_inbox_video"),
    LOCAL_LOOP_VIDEO("local_loop_video"),
    LOCAL_REPLY_SENT_VIDEO("local_reply_sent_video"),
    LOCAL_COMMENT_VIDEO("local_comment_video"),
    LOCAL_LOOP_COMMENT_VIDEO("local_loop_comment_video"),
    REPOSTED_PV_TO_RT("reposted_pv_to_rt"),
    REPOSTED_RT_TO_RT("reposted_rt_to_rt"),
    RT_PREVIEW_AVAILABLE("rt_preview_available"),
    RT_PARTICIPATION_REQUEST("rt_participation_request"),
    NEW_MATCHING_PV("new_matching_pv"),
    NEW_RT_QUESTION("new_rt_question"),
    SUBSCRIBE("subscribe"),
    SUBSCRIBE_DETAILS("subscribe_details"),
    REPOST("repost"),
    DM("dm"),
    SAVED("saved"),
    REPLY("reply"),
    TERMS("terms"),
    PRIVACY("privacy"),
    COMMUNITY_DETAIL("community_detail"),
    COMMUNITY_JOIN("community_join"),
    COMMUNITY_CREATE("community_create"),
    LOOP_DETAIL("loop_detail"),
    UNDEFINED("undefined");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationType getNotificationType(final String notificationValue) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationValue.equals(notificationType.getValue())) {
                return notificationType;
            }
        }
        return UNDEFINED;
    }
}


