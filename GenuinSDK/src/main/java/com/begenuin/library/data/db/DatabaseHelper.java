package com.begenuin.library.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {

    static final String TABLE_PUBLIC_VIDEO = "PublicVideos";
    static final String TABLE_QUEUE = "Queue";
    static final String TABLE_SAVED_VIDEO = "SavedVideo";
    static final String TABLE_CHAT_MASTER = "Conversations";
    static final String TABLE_CONTACTS = "Contacts";
    static final String TABLE_TEMP_CONTACTS = "TempContacts";
    static final String TABLE_VIDEO_CACHE = "VideoCache";
    static final String TABLE_CONVERSATION_MEMBERS = "ConversationMembers";
    static final String TABLE_GROUP = "Groups";
    static final String TABLE_SUBSCRIBERS = "Subscribers";
    static final String TABLE_PENDING_REQUESTS = "PendingRequests";
    static final String TABLE_COMMENTS = "Comments";
    static final String TABLE_UTM_SOURCE = "UTMSource";

    // ConversationMaster table which will store master data of conversation.
    static final String TABLE_CONVERSATIONS_MASTER = "ConversationMaster";

    // User table which will store user data
    static final String TABLE_USER_MASTER = "Users";

    // Loop Group master which will store group details of loop
    static final String TABLE_LOOP_GROUP_MASTER = "LoopGroup";

    // GroupMembers table which will store group member's data
    static final String TABLE_GROUP_MEMBERS = "GroupMembers";

    // Messages table which will store user's messages(chats)
    static final String TABLE_MESSAGE_MASTER = "Messages";

    // LoopSubscribers table which will store loop's subscribers
    static final String TABLE_LOOP_SUBSCRIBERS = "LoopSubscribers";

    // LoopPendingRequests table which will store loop's pending requests
    static final String TABLE_LOOP_PENDING_REQUESTS = "LoopPendingRequests";

    static final String COLUMN_ID = "_id";
    static final String COLUMN_VIDEO_ID = "video_id";
    static final String COLUMN_VIDEO_CACHE_URL = "video_cache_url";
    static final String COLUMN_VIDEO_PATH = "video_path";
    static final String COLUMN_DOWNLOADED_DATE = "downloaded_date";
    static final String COLUMN_LAST_PLAYED_DATE = "last_played_date";

    static final String COLUMN_CONTACTS_FIRST_NAME = "contacts_first_name";
    static final String COLUMN_CONTACTS_MIDDLE_NAME = "contacts_middle_name";
    static final String COLUMN_CONTACTS_LAST_NAME = "contacts_last_name";
    static final String COLUMN_CONTACTS_PHONE = "contacts_phone";
    static final String COLUMN_CONTACTS_PHOTO = "contacts_uri";
    static final String COLUMN_CONTACTS_GENUIN = "contact_genuin";
    static final String COLUMN_IS_BLOCKED = "is_blocked";
    static final String COLUMN_CAN_PRIVATE_CHAT = "can_private_chat";
    static final String COLUMN_CONTACTS_BIO = "contacts_bio";

    static final String COLUMN_VIDEO_URL = "video_url";
    static final String COLUMN_VIDEO_URL_M3U8 = "video_url_m3u8";
    static final String COLUMN_FILE_URL = "file_url";
    static final String COLUMN_VIDEO_LOCAL_PATH = "video_local_path";
    static final String COLUMN_FILE_LOCAL_PATH = "file_local_path";
    static final String COLUMN_IMAGE_LOCAL_PATH = "image_local_path";
    static final String COLUMN_FIRST_VIDEO_LOCAL_PATH = "first_video_local_path";
    static final String COLUMN_THUMBNAIL = "video_thumbnail";
    static final String COLUMN_VIDEO_THUMBNAIL_S = "video_thumbnail_s";
    static final String COLUMN_VIDEO_THUMBNAIL_L = "video_thumbnail_l";
    static final String COLUMN_NO_OF_VIEWS = "no_of_views";
    static final String COLUMN_NO_OF_COMMENTS = "no_of_comments";
    static final String COLUMN_NO_OF_WAITING = "no_of_waiting";
    static final String COLUMN_NO_OF_VIDEOS = "no_of_videos";
    static final String COLUMN_NO_OF_MEMBERS = "no_of_members";
    static final String COLUMN_NO_OF_SUBSCRIBERS = "no_of_subscribers";
    static final String COLUMN_NO_OF_CONVERSATIONS = "no_of_conversations";
    static final String COLUMN_IS_FLAG = "is_flag";
    static final String COLUMN_ASPECT_RATIO = "aspect_ratio";
    static final String COLUMN_DESCRIPTION = "description";
    static final String COLUMN_DURATION = "duration";
    static final String COLUMN_LINK = "link";
    static final String COLUMN_RESOLUTION = "resolution";
    static final String COLUMN_SIZE = "size";
    static final String COLUMN_META_DATA = "meta_data";
    static final String COLUMN_QR_CODE = "qr_code";
    static final String COLUMN_VIDEO_UPLOAD_STATUS = "video_upload_status";
    static final String COLUMN_IMAGE_UPLOAD_STATUS = "image_upload_status";
    static final String COLUMN_FILE_UPLOAD_STATUS = "file_upload_status";
    static final String COLUMN_API_STATUS = "api_status";
    static final String COLUMN_COMPRESSION_STATUS = "compression_status";
    static final String COLUMN_DP_UPLOAD_STATUS = "dp_upload_status";
    static final String COLUMN_FFMPEG_COMMAND = "ffmpeg_command";
    static final String COLUMN_SETTINGS = "settings";
    static final String COLUMN_REPOST = "repost";

    static final String COLUMN_SHARE_URL = "video_share_url";
    static final String COLUMN_POSTED_DATE = "video_posted_date";
    static final String COLUMN_IS_DISCOVER_ENABLE = "isDiscoverEnable";

    static final String COLUMN_CHAT_ID = "chat_id";
    static final String COLUMN_COMMENT_ID = "comment_id";
    static final String COLUMN_FILE_TYPE = "file_type";
    static final String COLUMN_QUEUE_COUNT = "queue_count";
    static final String COLUMN_QUEUE_TYPE = "queue_type";
    static final String COLUMN_SAVED_VIDEO = "saved_video";
    static final String COLUMN_IS_REPLY_RECEIVED = "is_reply_received";
    static final String COLUMN_IS_REPLY_OR_REACTION = "is_reply_or_reaction";

    static final String COLUMN_USER_ID = "user_id";
    static final String COLUMN_SAVED_AT = "saved_at";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_NICKNAME = "nickname";

    static final String COLUMN_CONVERSATION_ID = "conversation_id";
    static final String COLUMN_CONVERSATION_AT = "conversation_at";
    static final String COLUMN_CREATED_AT = "created_at";
    static final String COLUMN_IS_READ = "is_read";
    static final String COLUMN_IS_REPLY = "is_reply";
    static final String COLUMN_IS_RETRY = "is_retry";
    static final String COLUMN_FROM_STATUS = "from_status";
    static final String COLUMN_IS_FRONT = "is_front";
    static final String COLUMN_IS_AVATAR = "is_avatar";
    static final String COLUMN_PROFILE_IMAGE = "profile_image";
    static final String COLUMN_PROFILE_IMAGE_S = "profile_image_s";
    static final String COLUMN_PROFILE_IMAGE_M = "profile_image_m";
    static final String COLUMN_PROFILE_IMAGE_L = "profile_image_l";
    static final String COLUMN_IS_PRIVATE = "is_private";
    static final String COLUMN_CONTACT_USERS = "contact_users";
    static final String COLUMN_GROUP_NAME = "group_name";
    static final String COLUMN_GROUP_DESC = "group_desc";
    static final String COLUMN_GROUP_ID = "group_id";
    static final String COLUMN_MEMBER_STATUS = "member_status";
    static final String COLUMN_MEMBER_ROLE = "member_role";
    static final String COLUMN_CONVERSATION_TYPE = "conversation_type";
    static final String COLUMN_CONVERSATION_SHARE_URL = "conversation_share_url";
    static final String COLUMN_CONVERSATION_NO_OF_VIEWS = "conversation_no_of_views";
    static final String COLUMN_QUESTIONS = "questions";
    static final String COLUMN_DP = "display_picture";
    static final String COLUMN_DP_S = "display_picture_s";
    static final String COLUMN_DP_COLOR = "display_picture_color";
    static final String COLUMN_DP_TEXT_COLOR = "dp_text_color";
    static final String COLUMN_UTM_SOURCE = "utm_source";
    static final String COLUMN_UTM_MEDIUM = "utm_medium";
    static final String COLUMN_UTM_CAMPAIGN = "utm_campaign";
    static final String COLUMN_ACTION = "utm_action";
    static final String COLUMN_FROM_USERNAME = "from_username";
    static final String COLUMN_SOURCE_ID = "source_id";
    static final String COLUMN_PARENT_ID = "parent_id";
    static final String COLUMN_CONTENT_TYPE = "content_type";
    static final String COLUMN_DEEP_LINK = "deep_link";
    static final String COLUMN_IS_DUMPED = "is_dumped";

    static final String COLUMN_TEMPLATE_ID = "template_id";

    static final String COLUMN_IS_WELCOME_LOOP = "is_welcome_loop";

    // To store unread message count
    static final String COLUMN_UNREAD_MESSAGE_COUNT = "unread_message_count";

    // To store latest message time
    static final String COLUMN_LATEST_MESSAGE_AT = "latest_message_at";

    // To store message time
    static final String COLUMN_MESSAGE_AT = "message_at";

    // To store message id
    static final String COLUMN_MESSAGE_ID = "message_id";

    // To store media url
    static final String COLUMN_MEDIA_URL = "media_url";

    // To store media url m3u8
    static final String COLUMN_MEDIA_URL_M3U8 = "media_url_m3u8";

    // To store message summary
    static final String COLUMN_MESSAGE_SUMMARY = "message_summary";

    static final String COLUMN_COMMUNITY_ID = "community_id";

    static final String COLUMN_COMMENT_TEXT = "comment_text";

    static final String COLUMN_COMMENT_DATA = "comment_data";

    private static final String DATABASE_NAME = "begenuin_library.db";
    // changed db version to incorporate new changes
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement for public videos
    private final String CREATE_PUBLIC_VIDEO_TABLE = "create table "
            + TABLE_PUBLIC_VIDEO + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_VIDEO_ID
            + " text, " + COLUMN_VIDEO_URL
            + " text, " + COLUMN_THUMBNAIL
            + " text, " + COLUMN_VIDEO_LOCAL_PATH
            + " text, " + COLUMN_NO_OF_VIEWS
            + " integer, " + COLUMN_NO_OF_WAITING
            + " integer, " + COLUMN_NO_OF_CONVERSATIONS
            + " integer, " + COLUMN_IS_FLAG
            + " integer, " + COLUMN_DESCRIPTION
            + " text, " + COLUMN_DURATION
            + " text, " + COLUMN_LINK
            + " text, " + COLUMN_ASPECT_RATIO
            + " text, " + COLUMN_RESOLUTION
            + " text, " + COLUMN_SIZE
            + " text, " + COLUMN_VIDEO_UPLOAD_STATUS
            + " integer, " + COLUMN_SHARE_URL
            + " text, " + COLUMN_POSTED_DATE
            + " text, " + COLUMN_IS_RETRY
            + " integer, " + COLUMN_IS_PRIVATE
            + " integer, " + COLUMN_CONTACT_USERS
            + " text, " + COLUMN_IMAGE_LOCAL_PATH
            + " text, " + COLUMN_IMAGE_UPLOAD_STATUS
            + " integer, " + COLUMN_API_STATUS
            + " integer, " + COLUMN_IS_DISCOVER_ENABLE
            + " integer, " + COLUMN_QUESTIONS
            + " text, " + COLUMN_META_DATA
            + " text DEFAULT '', " + COLUMN_QR_CODE
            + " text DEFAULT '', " + COLUMN_FFMPEG_COMMAND
            + " text DEFAULT '', " + COLUMN_COMPRESSION_STATUS
            + " integer DEFAULT 0);";

    private final String CREATE_VIDEO_CACHE = "create table "
            + TABLE_VIDEO_CACHE + "( " + COLUMN_DOWNLOADED_DATE
            + " text, " + COLUMN_LAST_PLAYED_DATE
            + " text, " + COLUMN_VIDEO_PATH
            + " text, " + COLUMN_VIDEO_CACHE_URL
            + " text, " + COLUMN_CONVERSATION_ID
            + " text);";

    private final String CREATE_GROUP_TABLE = "create table "
            + TABLE_GROUP + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_GROUP_NAME
            + " text, " + COLUMN_GROUP_ID
            + " text, " + COLUMN_GROUP_DESC
            + " text, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_IS_REPLY_OR_REACTION
            + " integer, " + COLUMN_CONVERSATION_TYPE
            + " integer, " + COLUMN_VIDEO_URL
            + " text DEFAULT '', " + COLUMN_DP
            + " text DEFAULT '', " + COLUMN_DP_S
            + " text DEFAULT '', " + COLUMN_DP_COLOR
            + " text DEFAULT '', " + COLUMN_DP_TEXT_COLOR
            + " text DEFAULT '', " + COLUMN_NO_OF_VIEWS
            + " text DEFAULT '', " + COLUMN_NO_OF_VIDEOS
            + " text DEFAULT '', " + COLUMN_NO_OF_MEMBERS
            + " text DEFAULT '', " + COLUMN_NO_OF_SUBSCRIBERS
            + " text DEFAULT '' );";

    private final String CREATE_CONVERSATION_MEMBERS_TABLE = "create table "
            + TABLE_CONVERSATION_MEMBERS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_NAME
            + " text , " + COLUMN_NICKNAME
            + " text, " + COLUMN_IS_AVATAR
            + " integer, " + COLUMN_PROFILE_IMAGE
            + " text, " + COLUMN_PROFILE_IMAGE_L
            + " text, " + COLUMN_PROFILE_IMAGE_M
            + " text, " + COLUMN_PROFILE_IMAGE_S
            + " text, " + COLUMN_MEMBER_STATUS
            + " integer, " + COLUMN_MEMBER_ROLE
            + " integer, " + COLUMN_IS_REPLY_OR_REACTION
            + " integer, " + COLUMN_CONVERSATION_TYPE
            + " integer, " + COLUMN_CONVERSATION_AT
            + " integer, " + COLUMN_CONTACTS_PHONE
            + " text, " + COLUMN_VIDEO_URL
            + " text DEFAULT '', " + COLUMN_CONTACTS_BIO
            + " text DEFAULT '' );";

    private final String CREATE_SUBSCRIBERS_TABLE = "create table "
            + TABLE_SUBSCRIBERS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_NAME
            + " text , " + COLUMN_NICKNAME
            + " text, " + COLUMN_IS_AVATAR
            + " integer, " + COLUMN_PROFILE_IMAGE
            + " text, " + COLUMN_PROFILE_IMAGE_L
            + " text, " + COLUMN_PROFILE_IMAGE_M
            + " text, " + COLUMN_PROFILE_IMAGE_S
            + " text, " + COLUMN_CONTACTS_PHONE
            + " text, " + COLUMN_CONTACTS_BIO
            + " text DEFAULT '' );";

    private final String CREATE_PENDING_REQUESTS_TABLE = "create table "
            + TABLE_PENDING_REQUESTS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_NAME
            + " text , " + COLUMN_NICKNAME
            + " text, " + COLUMN_IS_AVATAR
            + " integer, " + COLUMN_PROFILE_IMAGE
            + " text, " + COLUMN_PROFILE_IMAGE_L
            + " text, " + COLUMN_PROFILE_IMAGE_M
            + " text, " + COLUMN_PROFILE_IMAGE_S
            + " text, " + COLUMN_CONTACTS_PHONE
            + " text, " + COLUMN_CONTACTS_BIO
            + " text DEFAULT '' );";

    private final String CREATE_UTM_SOURCE_TABLE = "create table "
            + TABLE_UTM_SOURCE + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_UTM_SOURCE
            + " text, " + COLUMN_UTM_MEDIUM
            + " text, " + COLUMN_UTM_CAMPAIGN
            + " text, " + COLUMN_ACTION
            + " text, " + COLUMN_FROM_USERNAME
            + " text, " + COLUMN_SOURCE_ID
            + " text, " + COLUMN_PARENT_ID
            + " text, " + COLUMN_CONTENT_TYPE
            + " text, " + COLUMN_DEEP_LINK
            + " text, " + COLUMN_CREATED_AT
            + " integer, " + COLUMN_IS_DUMPED
            + " integer );";

    // Database creation sql statement for queue
    private final String CREATE_QUEUE_TABLE = "create table "
            + TABLE_QUEUE + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_THUMBNAIL
            + " text, " + COLUMN_QUEUE_COUNT
            + " integer , " + COLUMN_QUEUE_TYPE
            + " integer, " + COLUMN_VIDEO_THUMBNAIL_L
            + " text, " + COLUMN_VIDEO_THUMBNAIL_S
            + " text, " + COLUMN_CHAT_ID
            + " text DEFAULT '' );";

    private final String CREATE_CONTACTS_TABLE = "create table "
            + TABLE_CONTACTS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CONTACTS_FIRST_NAME
            + " text, " + COLUMN_CONTACTS_MIDDLE_NAME
            + " text, " + COLUMN_CONTACTS_LAST_NAME
            + " text, " + COLUMN_IS_BLOCKED
            + " integer, " + COLUMN_CAN_PRIVATE_CHAT
            + " integer, " + COLUMN_CONTACTS_PHONE
            + " text UNIQUE, " + COLUMN_CONTACTS_GENUIN
            + " text DEFAULT '', " + COLUMN_CONTACTS_PHOTO
            + " text );";

    private final String CREATE_TEMP_CONTACTS_TABLE = "create table "
            + TABLE_TEMP_CONTACTS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CONTACTS_FIRST_NAME
            + " text, " + COLUMN_CONTACTS_MIDDLE_NAME
            + " text, " + COLUMN_CONTACTS_LAST_NAME
            + " text, " + COLUMN_IS_BLOCKED
            + " integer, " + COLUMN_CAN_PRIVATE_CHAT
            + " integer, " + COLUMN_CONTACTS_PHONE
            + " text UNIQUE, " + COLUMN_CONTACTS_GENUIN
            + " text DEFAULT '', " + COLUMN_CONTACTS_PHOTO
            + " text );";

    // Database creation sql statement for saved videos
    private final String CREATE_SAVE_VIDEO_TABLE = "create table "
            + TABLE_SAVED_VIDEO + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text, " + COLUMN_VIDEO_ID
            + " text, " + COLUMN_VIDEO_URL
            + " text, " + COLUMN_THUMBNAIL
            + " text, " + COLUMN_DURATION
            + " text, " + COLUMN_LINK
            + " text, " + COLUMN_DESCRIPTION
            + " text, " + COLUMN_ASPECT_RATIO
            + " text, " + COLUMN_RESOLUTION
            + " text, " + COLUMN_SIZE
            + " text, " + COLUMN_SAVED_AT
            + " integer, " + COLUMN_SHARE_URL
            + " text, " + COLUMN_VIDEO_THUMBNAIL_L
            + " text, " + COLUMN_VIDEO_THUMBNAIL_S
            + " text, " + COLUMN_IS_AVATAR
            + " integer, " + COLUMN_PROFILE_IMAGE
            + " text, " + COLUMN_PROFILE_IMAGE_S
            + " text, " + COLUMN_PROFILE_IMAGE_M
            + " text, " + COLUMN_PROFILE_IMAGE_L
            + " text );";

    // Database creation sql statement for conversation
    private final String CREATE_CONVERSATION_TABLE = "create table "
            + TABLE_CHAT_MASTER + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_THUMBNAIL
            + " text, " + COLUMN_VIDEO_URL
            + " text, " + COLUMN_VIDEO_LOCAL_PATH
            + " text, " + COLUMN_IMAGE_LOCAL_PATH
            + " text, " + COLUMN_CONVERSATION_ID
            + " text, " + COLUMN_CONVERSATION_AT
            + " integer, " + COLUMN_DURATION
            + " text, " + COLUMN_LINK
            + " text, " + COLUMN_ASPECT_RATIO
            + " text, " + COLUMN_RESOLUTION
            + " text, " + COLUMN_SIZE
            + " text, " + COLUMN_IS_READ
            + " text, " + COLUMN_FROM_STATUS
            + " text, " + COLUMN_IS_REPLY
            + " integer, " + COLUMN_SAVED_VIDEO
            + " integer, " + COLUMN_VIDEO_UPLOAD_STATUS
            + " integer, " + COLUMN_IMAGE_UPLOAD_STATUS
            + " integer, " + COLUMN_IS_REPLY_RECEIVED
            + " integer, " + COLUMN_IS_REPLY_OR_REACTION
            + " integer, " + COLUMN_IS_RETRY
            + " integer, " + COLUMN_FIRST_VIDEO_LOCAL_PATH
            + " text, " + COLUMN_IS_FRONT
            + " integer, " + COLUMN_VIDEO_THUMBNAIL_L
            + " text, " + COLUMN_VIDEO_THUMBNAIL_S
            + " text, " + COLUMN_IS_PRIVATE
            + " integer, " + COLUMN_USER_ID
            + " text, " + COLUMN_CONVERSATION_TYPE
            + " integer DEFAULT 0, " + COLUMN_DESCRIPTION
            + " text DEFAULT '', " + COLUMN_NAME
            + " text DEFAULT '', " + COLUMN_NICKNAME
            + " text DEFAULT '', " + COLUMN_IS_AVATAR
            + " integer DEFAULT 0, " + COLUMN_PROFILE_IMAGE
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_L
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_M
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_S
            + " text DEFAULT '', " + COLUMN_CONTACTS_BIO
            + " text DEFAULT '', " + COLUMN_SHARE_URL
            + " text DEFAULT '', " + COLUMN_NO_OF_VIEWS
            + " text DEFAULT '', " + COLUMN_NO_OF_COMMENTS
            + " text DEFAULT '', " + COLUMN_CONVERSATION_SHARE_URL
            + " text DEFAULT '', " + COLUMN_CONVERSATION_NO_OF_VIEWS
            + " text DEFAULT '', " + COLUMN_QUESTIONS
            + " text DEFAULT '', " + COLUMN_META_DATA
            + " text DEFAULT '', " + COLUMN_VIDEO_URL_M3U8
            + " text DEFAULT '', " + COLUMN_FFMPEG_COMMAND
            + " text DEFAULT '', " + COLUMN_COMPRESSION_STATUS
            + " integer DEFAULT 0, " + COLUMN_DP_UPLOAD_STATUS
            + " integer DEFAULT 2 ," + COLUMN_SETTINGS
            + " text DEFAULT '', " + COLUMN_REPOST
            + " text DEFAULT '');";

    private final String CREATE_COMMENTS_TABLE = "create table "
            + TABLE_COMMENTS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_CONVERSATION_ID
            + " text, " + COLUMN_COMMENT_ID
            + " text, " + COLUMN_THUMBNAIL
            + " text, " + COLUMN_FILE_URL
            + " text, " + COLUMN_FILE_LOCAL_PATH
            + " text, " + COLUMN_IMAGE_LOCAL_PATH
            + " text, " + COLUMN_CREATED_AT
            + " integer, " + COLUMN_DURATION
            + " text, " + COLUMN_LINK
            + " text, " + COLUMN_META_DATA
            + " text, " + COLUMN_FILE_TYPE
            + " integer, " + COLUMN_IS_READ
            + " integer, " + COLUMN_FILE_UPLOAD_STATUS
            + " integer, " + COLUMN_IMAGE_UPLOAD_STATUS
            + " integer, " + COLUMN_API_STATUS
            + " integer, " + COLUMN_IS_RETRY
            + " integer, " + COLUMN_USER_ID
            + " text, " + COLUMN_NAME
            + " text DEFAULT '', " + COLUMN_NICKNAME
            + " text DEFAULT '', " + COLUMN_IS_AVATAR
            + " integer DEFAULT 0, " + COLUMN_PROFILE_IMAGE
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_L
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_M
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_S
            + " text DEFAULT '', " + COLUMN_CONTACTS_BIO
            + " text DEFAULT '', " + COLUMN_SHARE_URL
            + " text DEFAULT '', " + COLUMN_NO_OF_VIEWS
            + " text DEFAULT '', " + COLUMN_QUESTIONS
            + " text DEFAULT '', " + COLUMN_FFMPEG_COMMAND
            + " text DEFAULT '', " + COLUMN_COMPRESSION_STATUS
            + " integer DEFAULT 0, " + COLUMN_COMMENT_TEXT
            + " text DEFAULT '', " + COLUMN_COMMENT_DATA
            + " text DEFAULT '');";

    // Database creation sql statement for conversation master
    private final String CREATE_CONVERSATION_MASTER_TABLE = "create table "
            + TABLE_CONVERSATIONS_MASTER + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_CONVERSATION_TYPE
            + " integer DEFAULT 0, " + COLUMN_SHARE_URL
            + " text DEFAULT '', " + COLUMN_VIDEO_LOCAL_PATH
            + " text DEFAULT '', " + COLUMN_SETTINGS
            + " text DEFAULT '', " + COLUMN_NO_OF_VIEWS
            + " text DEFAULT '', " + COLUMN_LATEST_MESSAGE_AT
            + " integer, " + COLUMN_UNREAD_MESSAGE_COUNT
            + " integer, " + COLUMN_COMMUNITY_ID
            + " text DEFAULT '', " + COLUMN_TEMPLATE_ID
            + " integer DEFAULT 0, " + COLUMN_IS_WELCOME_LOOP
            + " integer DEFAULT 0, UNIQUE(chat_id, video_local_path));";

    // Database creation sql statement for user master
    private final String CREATE_USERS_TABLE = "create table "
            + TABLE_USER_MASTER + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text UNIQUE, " + COLUMN_NAME
            + " text DEFAULT '', " + COLUMN_NICKNAME
            + " text DEFAULT '', " + COLUMN_IS_AVATAR
            + " integer DEFAULT 0, " + COLUMN_PROFILE_IMAGE
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_L
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_M
            + " text DEFAULT '', " + COLUMN_PROFILE_IMAGE_S
            + " text DEFAULT '', " + COLUMN_CONTACTS_BIO
            + " text DEFAULT '', " + COLUMN_CONTACTS_PHONE
            + " text DEFAULT '');";

    // Database creation sql statement for group master
    private final String CREATE_LOOP_GROUP_TABLE = "create table "
            + TABLE_LOOP_GROUP_MASTER + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_GROUP_NAME
            + " text, " + COLUMN_GROUP_ID
            + " text, " + COLUMN_GROUP_DESC
            + " text, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_CONVERSATION_TYPE
            + " integer DEFAULT 0, " + COLUMN_VIDEO_LOCAL_PATH
            + " text DEFAULT '', " + COLUMN_DP
            + " text DEFAULT '', " + COLUMN_DP_S
            + " text DEFAULT '', " + COLUMN_DP_COLOR
            + " text DEFAULT '', " + COLUMN_DP_TEXT_COLOR
            + " text DEFAULT '', " + COLUMN_NO_OF_VIEWS
            + " text DEFAULT '', " + COLUMN_NO_OF_VIDEOS
            + " text DEFAULT '', " + COLUMN_NO_OF_MEMBERS
            + " text DEFAULT '', " + COLUMN_NO_OF_SUBSCRIBERS
            + " text DEFAULT '', UNIQUE(chat_id, video_local_path));";

    // Database creation sql statement for group members
    private final String CREATE_GROUP_MEMBERS_TABLE = "create table "
            + TABLE_GROUP_MEMBERS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_MEMBER_STATUS
            + " integer DEFAULT 0, " + COLUMN_MEMBER_ROLE
            + " integer DEFAULT 0, " + COLUMN_CONVERSATION_TYPE
            + " integer DEFAULT 0, " + COLUMN_VIDEO_LOCAL_PATH
            + " text DEFAULT '', UNIQUE(user_id, chat_id));";

    // Database creation sql statement for loop subscribers
    private final String CREATE_LOOP_SUBSCRIBERS_TABLE = "create table "
            + TABLE_LOOP_SUBSCRIBERS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text, " + COLUMN_CHAT_ID
            + " text, UNIQUE(user_id, chat_id));";


    // Database creation sql statement for loop pending requests
    private final String CREATE_LOOP_PENDING_REQUESTS_TABLE = "create table "
            + TABLE_LOOP_PENDING_REQUESTS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_USER_ID
            + " text, " + COLUMN_CHAT_ID
            + " text, UNIQUE(user_id, chat_id));";

    // Database creation sql statement for messages
    private final String CREATE_MESSAGES_TABLE = "create table "
            + TABLE_MESSAGE_MASTER + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CHAT_ID
            + " text, " + COLUMN_MEDIA_URL
            + " text, " + COLUMN_THUMBNAIL
            + " text, " + COLUMN_VIDEO_LOCAL_PATH
            + " text DEFAULT '', " + COLUMN_IMAGE_LOCAL_PATH
            + " text DEFAULT '', " + COLUMN_MESSAGE_ID
            + " text, " + COLUMN_USER_ID
            + " text DEFAULT '', " + COLUMN_MESSAGE_AT
            + " integer DEFAULT 0, " + COLUMN_LINK
            + " text DEFAULT '', " + COLUMN_IS_READ
            + " integer DEFAULT 0, " + COLUMN_VIDEO_UPLOAD_STATUS
            + " integer DEFAULT 0, " + COLUMN_IMAGE_UPLOAD_STATUS
            + " integer DEFAULT 0, " + COLUMN_IS_RETRY
            + " integer DEFAULT 0, " + COLUMN_VIDEO_THUMBNAIL_L
            + " text DEFAULT '', " + COLUMN_VIDEO_THUMBNAIL_S
            + " text DEFAULT '', " + COLUMN_CONVERSATION_TYPE
            + " integer DEFAULT 0, " + COLUMN_MESSAGE_SUMMARY
            + " text DEFAULT '', " + COLUMN_SHARE_URL
            + " text DEFAULT '', " + COLUMN_NO_OF_VIEWS
            + " text DEFAULT '', " + COLUMN_NO_OF_COMMENTS
            + " text DEFAULT '', " + COLUMN_QUESTIONS
            + " text DEFAULT '', " + COLUMN_META_DATA
            + " text DEFAULT '', " + COLUMN_MEDIA_URL_M3U8
            + " text DEFAULT '', " + COLUMN_FFMPEG_COMMAND
            + " text DEFAULT '', " + COLUMN_COMPRESSION_STATUS
            + " integer DEFAULT 0, " + COLUMN_DP_UPLOAD_STATUS
            + " integer DEFAULT 2 ," + COLUMN_SETTINGS
            + " text DEFAULT '', " + COLUMN_REPOST
            + " text DEFAULT '', UNIQUE(message_id, video_local_path));";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_PUBLIC_VIDEO_TABLE);
        database.execSQL(CREATE_QUEUE_TABLE);
        database.execSQL(CREATE_SAVE_VIDEO_TABLE);
        database.execSQL(CREATE_CONVERSATION_TABLE);
        database.execSQL(CREATE_VIDEO_CACHE);
        database.execSQL(CREATE_CONTACTS_TABLE);
        database.execSQL(CREATE_CONVERSATION_MEMBERS_TABLE);
        database.execSQL(CREATE_GROUP_TABLE);
        database.execSQL(CREATE_SUBSCRIBERS_TABLE);
        database.execSQL(CREATE_COMMENTS_TABLE);
        database.execSQL(CREATE_TEMP_CONTACTS_TABLE);
        database.execSQL(CREATE_PENDING_REQUESTS_TABLE);
        database.execSQL(CREATE_UTM_SOURCE_TABLE);

        // This will create tables in db
        database.execSQL(CREATE_CONVERSATION_MASTER_TABLE);
        database.execSQL(CREATE_USERS_TABLE);
        database.execSQL(CREATE_LOOP_GROUP_TABLE);
        database.execSQL(CREATE_GROUP_MEMBERS_TABLE);
        database.execSQL(CREATE_MESSAGES_TABLE);
        database.execSQL(CREATE_LOOP_SUBSCRIBERS_TABLE);
        database.execSQL(CREATE_LOOP_PENDING_REQUESTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_SHARE_URL);
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_POSTED_DATE);
        }

        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_IS_RETRY + " DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_FROM_STATUS + " DEFAULT ''");
        }

        if (oldVersion < 4 && newVersion >= 4) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_FIRST_VIDEO_LOCAL_PATH + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_IS_FRONT + " DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_SHARE_URL + " DEFAULT ''");
        }

        if (oldVersion < 5 && newVersion >= 5) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_VIDEO_THUMBNAIL_L + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_VIDEO_THUMBNAIL_S + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_VIDEO_THUMBNAIL_L + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_VIDEO_THUMBNAIL_S + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_QUEUE + " ADD COLUMN " + COLUMN_VIDEO_THUMBNAIL_L + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_QUEUE + " ADD COLUMN " + COLUMN_VIDEO_THUMBNAIL_S + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_IS_RETRY + " DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_IS_AVATAR + " DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_PROFILE_IMAGE + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_PROFILE_IMAGE_S + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_PROFILE_IMAGE_M + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_SAVED_VIDEO + " ADD COLUMN " + COLUMN_PROFILE_IMAGE_L + " DEFAULT ''");
        }

        if (oldVersion < 6 && newVersion >= 6) {
            db.execSQL(CREATE_VIDEO_CACHE);
        }

        if (oldVersion < 7 && newVersion >= 7) {
            if (oldVersion == 6) {
                db.execSQL("ALTER TABLE " + TABLE_VIDEO_CACHE + " ADD COLUMN " + COLUMN_CONVERSATION_ID + " DEFAULT ''");
            }
        }

        if (oldVersion < 8 && newVersion >= 8) {
            db.execSQL(CREATE_CONTACTS_TABLE);

            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_IMAGE_UPLOAD_STATUS + " DEFAULT 2");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_IMAGE_LOCAL_PATH + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_CONVERSATION_TYPE + " DEFAULT 0");

            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_CONVERSATION_TYPE + " DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_CONTACT_USERS + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_IMAGE_UPLOAD_STATUS + " DEFAULT 2");
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_IMAGE_LOCAL_PATH + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_API_STATUS + " DEFAULT 1");
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_IS_DISCOVER_ENABLE + " DEFAULT 1");
        }

        if (oldVersion < 9 && newVersion >= 9) {
            db.execSQL(CREATE_CONVERSATION_MEMBERS_TABLE);
            db.execSQL(CREATE_GROUP_TABLE);

            if (oldVersion == 8) {
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_IS_BLOCKED + " DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_CAN_PRIVATE_CHAT + " DEFAULT 1");
            }

            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_USER_ID + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_DESCRIPTION + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_QUEUE + " ADD COLUMN " + COLUMN_CHAT_ID + " DEFAULT ''");
        }

        if (oldVersion < 10 && newVersion >= 10) {
            if (oldVersion == 9) {
                db.execSQL("ALTER TABLE " + TABLE_CONVERSATION_MEMBERS + " ADD COLUMN " + COLUMN_CONVERSATION_TYPE);
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_GROUP_DESC);
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_CONVERSATION_TYPE);
            }
            db.execSQL(CREATE_SUBSCRIBERS_TABLE);
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_NAME + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_NICKNAME + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_IS_AVATAR + " DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_PROFILE_IMAGE + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_PROFILE_IMAGE_L + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_PROFILE_IMAGE_M + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_PROFILE_IMAGE_S + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_CONTACTS_BIO + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_SHARE_URL + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_NO_OF_VIEWS + " DEFAULT ''");
        }

        if (oldVersion < 11 && newVersion >= 11) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_CONVERSATION_SHARE_URL + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_CONVERSATION_NO_OF_VIEWS + " DEFAULT ''");
        }

        if (oldVersion < 12 && newVersion >= 12) {
            db.execSQL(CREATE_COMMENTS_TABLE);
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_NO_OF_COMMENTS + " DEFAULT ''");
        }

        if (oldVersion < 13 && newVersion >= 13) {
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_QUESTIONS + " DEFAULT ''");
        }

        if (oldVersion < 14 && newVersion >= 14) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_QUESTIONS + " DEFAULT ''");
            if (oldVersion >= 12) {
                db.execSQL("ALTER TABLE " + TABLE_COMMENTS + " ADD COLUMN " + COLUMN_QUESTIONS + " DEFAULT ''");
            }
        }

        if (oldVersion < 15 && newVersion >= 15) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_VIDEO_URL_M3U8 + " DEFAULT ''");
        }

        if (oldVersion < 16 && newVersion >= 16) {
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_META_DATA + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_META_DATA + " DEFAULT ''");
        }

        if (oldVersion < 17 && newVersion >= 17) {
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_QR_CODE + " DEFAULT ''");
        }

        if (oldVersion < 18 && newVersion >= 18) {
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_FFMPEG_COMMAND + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_PUBLIC_VIDEO + " ADD COLUMN " + COLUMN_COMPRESSION_STATUS + " DEFAULT 0");

            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_FFMPEG_COMMAND + " DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_COMPRESSION_STATUS + " DEFAULT 0");

            if (oldVersion >= 12) {
                db.execSQL("ALTER TABLE " + TABLE_COMMENTS + " ADD COLUMN " + COLUMN_FFMPEG_COMMAND + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_COMMENTS + " ADD COLUMN " + COLUMN_COMPRESSION_STATUS + " DEFAULT 1");
            }
        }

        if (oldVersion < 19 && newVersion >= 19) {
            if (oldVersion >= 9) {
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_DP + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_DP_S + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_DP_COLOR + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_DP_TEXT_COLOR + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_NO_OF_VIEWS + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_NO_OF_VIDEOS + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_NO_OF_MEMBERS + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_GROUP + " ADD COLUMN " + COLUMN_NO_OF_SUBSCRIBERS + " DEFAULT ''");
            }
        }

        if (oldVersion < 20 && newVersion >= 20) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_DP_UPLOAD_STATUS + " DEFAULT 2");
        }

        if (oldVersion < 21 && newVersion >= 21) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_SETTINGS + " DEFAULT ''");
        }

        if (oldVersion < 22 && newVersion >= 22) {
            db.execSQL(CREATE_TEMP_CONTACTS_TABLE);
        }

        if (oldVersion < 23 && newVersion >= 23) {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MASTER + " ADD COLUMN " + COLUMN_REPOST + " DEFAULT ''");
        }

        if (oldVersion < 24 && newVersion >= 24) {
            db.execSQL(CREATE_PENDING_REQUESTS_TABLE);
        }

        if (oldVersion < 25 && newVersion >= 25) {
            db.execSQL(CREATE_UTM_SOURCE_TABLE);
        }

        // This will create new tables in db as part to db upgrade for existing users
        if (oldVersion < 26 && newVersion >= 26) {
            db.execSQL(CREATE_CONVERSATION_MASTER_TABLE);
            db.execSQL(CREATE_LOOP_GROUP_TABLE);
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_GROUP_MEMBERS_TABLE);
            db.execSQL(CREATE_MESSAGES_TABLE);
            db.execSQL(CREATE_LOOP_SUBSCRIBERS_TABLE);
            db.execSQL(CREATE_LOOP_PENDING_REQUESTS_TABLE);
        }

        if (oldVersion < 27 && newVersion >= 27) {
            if (oldVersion == 26) {
                db.execSQL("ALTER TABLE " + TABLE_CONVERSATIONS_MASTER + " ADD COLUMN " + COLUMN_COMMUNITY_ID + " DEFAULT ''");
            }
        }

        if (oldVersion < 28 && newVersion >= 28) {
            if (oldVersion >= 12) {
                db.execSQL("ALTER TABLE " + TABLE_COMMENTS + " ADD COLUMN " + COLUMN_COMMENT_TEXT + " DEFAULT ''");
                db.execSQL("ALTER TABLE " + TABLE_COMMENTS + " ADD COLUMN " + COLUMN_COMMENT_DATA + " DEFAULT ''");
            }
        }

        if (oldVersion < 29 && newVersion >= 29) {
            if (oldVersion >= 26) {
                db.execSQL("ALTER TABLE " + TABLE_CONVERSATIONS_MASTER + " ADD COLUMN " + COLUMN_TEMPLATE_ID + " DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_CONVERSATIONS_MASTER + " ADD COLUMN " + COLUMN_IS_WELCOME_LOOP + " DEFAULT 0");
            }
        }
    }
}
