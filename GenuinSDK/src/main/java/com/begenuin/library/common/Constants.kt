package com.begenuin.library.common

import android.graphics.Color

class Constants {

    companion object {
        const val SHARE_VIDEO = "share_video"
        private const val VIDEO_FILTER = "users/video/"
        const val DELETE: String = VIDEO_FILTER + "delete/"
        const val SAVE_VIDEO: String = VIDEO_FILTER + "save/"
        const val UNSAVE_VIDEO: String = VIDEO_FILTER + "unsave/"
        const val RECORD_PREVIEW_DOWNLOAD_CLICKED = "Record Preview Download Clicked"
        const val PREF_LOGO_WIDTH = "logo_width"
        const val FROM_PUBLIC_VIDEO = "public_video"
        const val VIEW_VIDEO = "video_view"
        const val MERGE_DIRECTORY = "merge_video"
        const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val DOWNLOAD_DIRECTORY = "download_video"
        // QA build private const val SOCKET: String = "https://nodejs.qa.begenuin.com"
        private const val SOCKET: String = "https://nodejs.qa.begenuin.com"
        @JvmField var BASE_URL: String = "$SOCKET/api/v3/"
        const val BASE_URL_v4: String = "$SOCKET/api/v4/"
        const val NO_NETWORK =
            "Uh-oh. We can't connect to your internet. Please check your internet and try again."
        const val GET_COMMUNITIES = "communities"
        const val MY_COMMUNITY_VIDEOS = "community/videos"
        const val JSON_DATA = "data"
        const val FROM_DIRECT = "direct"
        const val FROM_GROUP = "group"
        const val FROM_ROUND_TABLE = "round_table"
        const val FROM_REACTION = "reaction"
        const val MY_LOOP_VIDEOS = "rt_videos"
        const val SEARCH_API = "search"
        const val HOME = "home"
        const val VIDEO_ALREADY_DELETED_CODE = "5078"
        const val LOOP_RECENT_UPDATES = "get_recent_updates"
        const val DELETE_SEARCH_RECENTS = "global_search/recent"
        const val COMPLETE_PROFILE = "users/complete_profile"
        const val GET_COMMUNITY = "community"
        const val LL_LOADER = "ll_loader"
        const val REPOST_CLICKED = "Repost Clicked"
        const val SCREEN_FEED = "feed"
        const val SCREEN_EXPLORE = "explore"
        const val CATEGORY_PUBLIC_VIDEO = "genuin_video"
        const val CATEGORY_RT = "rt"
        const val VIDEO = "video"
        const val IMAGE = "image"
        const val SAVE_VIDEO_LL = "save_video_ll"
        const val LL_REPOST = "ll_repost"
        const val FAKE_TAG = "fake_tag"
        const val LL_SPARK = "ll_spark"
        const val TV_SPARK = "tv_spark"
        const val TV_DESC_SINGLE = "tv_desc_single"
        const val LINK = "link"
        const val MORE_OPTIONS_LAYOUT = "more_options_layout"
        const val LL_BOTTOM = "ll_bottom"
        const val WHO_CAN_SEE_LL = "who_can_see_ll"
        const val TV_DESC = "tv_desc"
        const val RL_DESC = "rl_desc"
        const val OVERLAY = "overlay"
        const val IV_MUTE = "iv_mute"
        const val LL_SUBSCRIBE = "ll_subscribe"
        const val LL_ASK_QUESTION = "ll_ask_question"
        const val TV_ASK_QUESTION = "tv_ask_question"
        const val SUBSCRIBE_RT = "conversation/subscription"
        const val REQUEST_TO_JOIN = "conversation/participation_request"
        const val EMPTY_FEED_ROUNDTABLES = "users/empty_feed_roundtables"
        const val CONVERSATIONS_NEW = "conversations"
        const val REACTION_SPARK = "spark"
        const val SWIPES_TO_COMPLETE_PROFILE_PROMPT = 20
        const val WRITE_STORAGE_PERMISSION = 25
        const val CLICK_COUNT = "users/video/click_count/"
        const val DEEP_LINK_UTM_SOURCE = "utm_source=app_android"
        const val DEEP_LINK_FROM_USERNAME = "from_username="
        const val SHARE_COUNT = "users/video/share_count/"
        const val SCREEN_HOME = "home"
        private const val USERS_CONVERSATION = "users/conversation"
        const val READ: String = "$USERS_CONVERSATION/read/"
        const val SCREEN_COMMENT = "comment"
        const val GET_PROFILE = "users/get_profile"
        const val PROFILE_VIDEOS = "profile_videos"
        const val GET_COMMUNITY_LOOPS = "community/loops"
        const val FROM_RECORD_FOR_OTHER = "record_for_other"
        const val SYNC_QUESTIONS = "video/questions"
        const val ADD_UPDATE_CUSTOM_QUESTION = "question"
        const val QUESTION_FONT_MAX_DEFAULT_SIZE = 100f
        const val ANIMATION_DURATION: Long = 300
        const val IS_SPEED_ENABLE = false
        @JvmField var IS_FEED_REFRESH = false
        @JvmField var IS_REACTION_GIVEN = false
        const val VIDEO_FORMAT = ".mp4"
        const val AUDIO_FORMAT = ".wav"
        const val IMAGE_FORMAT = ".jpeg"
        const val VIDEO_DIRECTORY = "profile_video"
        const val DUMMY_MODEL_ID = "-101"
        @JvmField var START_MILLIS_POST: Long = 0
        const val QUESTION_FONT_MIN_DEFAULT_SIZE = 5f
        const val TEXT_EDITOR_FONT_DEFAULT_SIZE = 25f
        val textBackgroundColorArray = intArrayOf(Color.TRANSPARENT, Color.WHITE, Color.BLACK)
        const val MAX_SEEKBAR_VALUE = 40f
        const val MIN_SEEKBAR_VALUE = 20f
        const val TEXT_BACKGROUND_PADDING = 14
        const val TEXT_BACKGROUND_RADIUS = 8
        @JvmField var GO_TO_INBOX = false
        const val CHECK_VIDEO: String = "$USERS_CONVERSATION/check/"
        const val CODE_5095 = "5095"
        const val CODE_5096 = "5096"
        @JvmField var START_MILLIS_REPLY: Long = 0
        const val VALID_URL: String = VIDEO_FILTER + "validate_url"
        const val QUESTION_VIEW_MAX_HEIGHT_PERCENTAGE = 0.33f
        const val FROM_CHAT = "chat"
        const val FROM_COMMENT = "comment"
        const val AUTO_SUGGESTIONS = "auto-suggestion"
        const val POSTS_IMAGES_DIRECTORY = "posts_images"
        const val GALLERY_DIRECTORY = "gallery_video"
        const val STICKER_IMAGES_DIRECTORY = "sticker_images"
        const val SYNC_LOOP_QUESTIONS = "questions"
        const val GET_UPLOAD_URL = "users/video/upload/create_upload_url"
        const val CREATE_PUBLIC_VIDEO = "video/create"
        const val CODE_5156 = "5156"
        const val VIDEO_CACHE_MINUTES: Long = 1440
        const val FROM_PROFILE_PHOTO = "profile_photo"
        const val FROM_CHANGE_COVER = "change_cover"
        const val SESSION_MERGE = "session_merge"
        const val SESSION_DOWNLOAD = "session_download"
        const val CREATE_VIDEO = "conversation/create"
        const val CODE_5061 = "5061"
        const val SESSION_IMAGE = "session_image"
        const val CODE_5057 = "5057"
        const val SEND_REPLY: String = "$USERS_CONVERSATION/reply"
    }

}