package com.begenuin.library.data.db;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.core.enums.VideoConvType;
import com.begenuin.library.data.model.ChatModel;
import com.begenuin.library.data.model.CommentModel;
import com.begenuin.library.data.model.ConversationModel;
import com.begenuin.library.data.model.DiscoverModel;
import com.begenuin.library.data.model.GroupModel;
import com.begenuin.library.data.model.LoopsModel;
import com.begenuin.library.data.model.MembersModel;
import com.begenuin.library.data.model.MessageModel;
import com.begenuin.library.data.model.MetaDataModel;
import com.begenuin.library.data.model.QuestionModel;
import com.begenuin.library.data.model.RepostModel;
import com.begenuin.library.data.model.SettingsModel;
import com.begenuin.library.views.activities.CommunityDetailsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class QueryDataHelper {

    private SQLiteDatabase mDatabase;
    private final DatabaseHelper dbHelper;

    /**
     * Constructor to init database.
     *
     * @param context -App {@link Context}
     */
    public QueryDataHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Open database connection to read/write values.
     *
     * @throws SQLException -Throws exception if database is locked
     */
    public synchronized void open() throws SQLException {
        mDatabase = dbHelper.getWritableDatabase();
    }

    public synchronized boolean isOpen() {
        return mDatabase != null && mDatabase.isOpen();
    }

    public void openDatabase() {
        if (!isOpen())
            open();
    }

    public void closeDatabase() {
        if (isOpen())
            close();
    }

    public synchronized void close() {
        dbHelper.close();
    }

    public void deleteAllTables() {
        try {
            mDatabase.delete(DatabaseHelper.TABLE_PUBLIC_VIDEO, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_QUEUE, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_SAVED_VIDEO, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_VIDEO_CACHE, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_SUBSCRIBERS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_COMMENTS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_PENDING_REQUESTS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_UTM_SOURCE, null, null);

            // Delete all tables after logout
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP_MEMBERS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_SUBSCRIBERS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_PENDING_REQUESTS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_USER_MASTER, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, null, null);
            updateBlockCanPrivateStatus();
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    public void updateBlockCanPrivateStatus() {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CAN_PRIVATE_CHAT, 1);
            values.put(DatabaseHelper.COLUMN_IS_BLOCKED, 0);
            mDatabase.update(DatabaseHelper.TABLE_CONTACTS, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    /**
     * Update profile video status to flagged
     *
     * @param videoIds -List of video ids.
     * @param status   -Video status
     */
    public void updatePublicVideoStatus(String[] videoIds, int status) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_FLAG, status);
            for (String videoId : videoIds) {
                mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_ID + " = '" + videoId + "'", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void insertOrUpdateVideoCache(String filepath, String downloadTime, String lastPlayedTime, String url, String conversationChatId) {
        try {
            ContentValues values = new ContentValues();
            if (!lastPlayedTime.isEmpty() && downloadTime.isEmpty()) {
                // alter table insert value
                Utility.showLog("VideoA " + "lastPlayed", lastPlayedTime);
                values.put(DatabaseHelper.COLUMN_LAST_PLAYED_DATE, lastPlayedTime);
                mDatabase.update(DatabaseHelper.TABLE_VIDEO_CACHE, values, DatabaseHelper.COLUMN_VIDEO_PATH +
                        "='" + filepath + "'", null);
            } else {
                values.put(DatabaseHelper.COLUMN_DOWNLOADED_DATE, downloadTime);
                values.put(DatabaseHelper.COLUMN_LAST_PLAYED_DATE, downloadTime);
                values.put(DatabaseHelper.COLUMN_VIDEO_CACHE_URL, url);
                values.put(DatabaseHelper.COLUMN_VIDEO_PATH, filepath);
                if (!TextUtils.isEmpty(conversationChatId))
                    values.put(DatabaseHelper.COLUMN_CONVERSATION_ID, conversationChatId);
                else
                    values.put(DatabaseHelper.COLUMN_CONVERSATION_ID, "");

                mDatabase.insert(DatabaseHelper.TABLE_VIDEO_CACHE, null, values);
            }
        } catch (Exception e) {
            Utility.showLog("db", "Unable to insert");
        }
    }

    @SuppressLint("Range")
    public void deleteCacheAfterOneDay(String currentTime, Activity activity) {
        String lastPlayedDate;
        long difference;
        String finalFileUrl;
        try {
            mDatabase.beginTransaction();
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_VIDEO_CACHE
                    + " WHERE " + DatabaseHelper.COLUMN_LAST_PLAYED_DATE + " IS NOT NULL";
            Cursor mCursor =
                    mDatabase.rawQuery(query, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        String conversationId = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_ID));
                        if (TextUtils.isEmpty(conversationId)) {
                            lastPlayedDate = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_LAST_PLAYED_DATE));
                            finalFileUrl = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_PATH));
                            difference = getDifferenceBetweenDates(lastPlayedDate, currentTime);
                            if (difference > Constants.VIDEO_CACHE_MINUTES) {
                                mDatabase.execSQL("DELETE FROM " + DatabaseHelper.TABLE_VIDEO_CACHE + " WHERE " + DatabaseHelper.COLUMN_VIDEO_PATH + "='" + finalFileUrl + "'");
                                File file = new File(activity.getCacheDir(), finalFileUrl);
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                        }
                    }
                    while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public boolean checkForPublicVideo(String localVideoPath) {
        boolean isInserted = false;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.getCount() > 0) {
                    isInserted = true;
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return isInserted;
    }

    public DiscoverModel getCompressedPublicVideo(String localVideoPath) {
        DiscoverModel discoverModel = null;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        discoverModel = getVideoObjectWithCursor(mCursor);
                    }
                    while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return discoverModel;
    }

    public ChatModel getCompressedChatVideo(String localVideoPath) {
        ChatModel chatModel = null;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        chatModel = getChatObjectWithCursor(mCursor);
                        chatModel.setGroup(getGroupDetails(chatModel.getChatId(), chatModel.getVideoUrl()));
                    }
                    while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return chatModel;
    }

    // This function will get loops which are failed/pending to upload
    public List<LoopsModel> getPendingLoops() {
        List<LoopsModel> pendingLoops = new ArrayList<>();
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving compression failed loops from loop master
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '-101'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        LoopsModel loopsModel = getLoopObjectWithCursor(mCursor);
                        pendingLoops.add(loopsModel);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

            // for loop to add message and group info to loop object
            for (int i = 0; i < pendingLoops.size(); i++) {
                LoopsModel loopsModel = pendingLoops.get(i);
                if (!TextUtils.isEmpty(loopsModel.getChatId())) {
                    // Set LatestMessages for loop
                    loopsModel.setLatestMessages(getMessagesForLoop(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
                    // Set Group for loop
                    loopsModel.setGroup(getLoopGroupDetails(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        // return pendingLoops list
        return pendingLoops;
    }

    // This function will get messages which are failed/pending to upload
    public List<MessageModel> getPendingMessages() {
        List<MessageModel> pendingMessages = new ArrayList<>();
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving failed messages
            String selectMessageQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " mm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON mm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE mm." + DatabaseHelper.COLUMN_CHAT_ID + " != '-101' AND mm." + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " != 3";

            Cursor mCursor = mDatabase.rawQuery(selectMessageQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        // Retrieve message object from cursor
                        MessageModel message = getMessageObjectWithCursor(mCursor);
                        pendingMessages.add(message);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        // return pendingLoops list
        return pendingMessages;
    }

    // This function will get messages which are failed/pending to upload for given chat_id
    public List<MessageModel> getPendingMessagesForChatId(String chatId) {
        List<MessageModel> pendingMessages = new ArrayList<>();
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving failed messages
            String selectMessageQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " mm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON mm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE mm." + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " != 3 AND mm." + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            Cursor mCursor = mDatabase.rawQuery(selectMessageQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        // Retrieve message object from cursor
                        MessageModel message = getMessageObjectWithCursor(mCursor);
                        pendingMessages.add(message);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        // return pendingLoops list
        return pendingMessages;
    }

    // This function will get loops which are failed/pending to upload for particular community
    public List<LoopsModel> getPendingCommunityLoops(String communityId) {
        List<LoopsModel> pendingLoops = new ArrayList<>();
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving compression failed loops from loop master
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " +
                    DatabaseHelper.COLUMN_CHAT_ID + " = '-101' AND " + DatabaseHelper.COLUMN_COMMUNITY_ID + " = '" + communityId + "'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                Utility.showLog("Database pending loops", mCursor.toString());
                if (mCursor.moveToFirst()) {
                    do {
                        Utility.showLog("Getting pending loop data", mCursor.toString());
                        LoopsModel loopsModel = getLoopObjectWithCursor(mCursor);
                        Utility.showLog("Getting pending loop data", loopsModel.getCommunityId());
                        pendingLoops.add(loopsModel);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

            // for loop to add message and group info to loop object
            for (int i = 0; i < pendingLoops.size(); i++) {
                LoopsModel loopsModel = pendingLoops.get(i);
                if (!TextUtils.isEmpty(loopsModel.getChatId())) {
                    // Set LatestMessages for loop
                    loopsModel.setLatestMessages(getMessagesForLoop(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
                    // Set Group for loop
                    loopsModel.setGroup(getLoopGroupDetails(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        // return pendingLoops list
        return pendingLoops;
    }

    // This function will get loop from given localVideoPath
    public LoopsModel getLoopByLocalVideoPath(String localVideoPath) {
        LoopsModel loopsModel = null;
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving loop from loop master for given localVideoPath
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            // Execute query to retrieve loop
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    // Retrieve loop object from cursor
                    loopsModel = getLoopObjectWithCursor(mCursor);
                }
                mCursor.close();
            }

            if (loopsModel != null) {
                // Set LatestMessages for loop
                loopsModel.setLatestMessages(getMessagesForLoop(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
                // Set Group for loop
                loopsModel.setGroup(getLoopGroupDetails(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return loopsModel;
    }

    // This function will get loop and load particular message from given messageId in loop
    public LoopsModel getLoopByChatIdAndMessageId(String chatId, String messageId) {
        LoopsModel loopsModel = null;
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving loop from loop master for given chatId
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Execute query to retrieve loop
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {

                    // Retrieve loop object from cursor
                    loopsModel = getLoopObjectWithCursor(mCursor);
                }
                mCursor.close();
            }

            if (loopsModel != null) {
                ArrayList<MessageModel> messageList = new ArrayList<>();

                // Get message from given messageId
                messageList.add(getMessageByMessageId(messageId));

                // Set LatestMessages for loop
                loopsModel.setLatestMessages(messageList);

                // Set Group for loop
                loopsModel.setGroup(getLoopGroupDetails(chatId, loopsModel.getLocalVideoPath()));
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // return loop object
        return loopsModel;
    }

    // This function will get the particular loop message by gicen localVideoPath
    public MessageModel getLoopVideoByLocalPath(String localVideoPath) {
        MessageModel messageModel = null;
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving message for loop
            String selectMessageQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " mm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON mm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE mm." + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "' ORDER BY mm." + DatabaseHelper.COLUMN_MESSAGE_AT + " DESC";

            // Execute query to retrieve loop
            Cursor mCursor = mDatabase.rawQuery(selectMessageQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {

                    // Retrieve loop object from cursor
                    messageModel = getMessageObjectWithCursor(mCursor);
                }
                mCursor.close();
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // return message object
        return messageModel;
    }

    public CommentModel getCompressedCommentVideo(String localVideoPath) {
        CommentModel commentModel = null;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_COMMENTS +
                    " WHERE " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localVideoPath + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        commentModel = getCommentObjectWithCursor(mCursor);
                    }
                    while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return commentModel;
    }

    @SuppressLint("Range")
    public String getShareURLPublicVideo(String localVideoPath) {
        String shareURL = "";
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        shareURL = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL));
                    }
                    while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return shareURL;
    }

    public void updateShareURLForPublicVideo(String shareURL, String localVideoPath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_SHARE_URL, shareURL);
            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + localVideoPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    @SuppressLint("Range")
    public String getShareURLForRT(String localVideoPath) {
        String shareURL = "";
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        shareURL = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL));
                    }
                    while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return shareURL;
    }

    public void updateShareURLForRT(String shareURL, String localVideoPath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_SHARE_URL, shareURL);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + localVideoPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public boolean checkForPublicVideoID(String videoId) {
        boolean isInserted = false;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_ID + " = '" + videoId + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.getCount() > 0) {
                    isInserted = true;
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return isInserted;
    }

//    public long insertPublicVideoRecord(PublicVideoModel publicVideoVO) {
//        try {
//            mDatabase.beginTransaction();
//            ContentValues values = new ContentValues();
//            values.put(DatabaseHelper.COLUMN_VIDEO_ID, publicVideoVO.getVideoId());
//            values.put(DatabaseHelper.COLUMN_VIDEO_URL, publicVideoVO.getVideoUrl());
//            values.put(DatabaseHelper.COLUMN_THUMBNAIL, publicVideoVO.getVideoThumbnail());
//            values.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, publicVideoVO.getLocalVideoPath());
//            values.put(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH, publicVideoVO.getImagePath());
//            values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, publicVideoVO.getNoOfViews());
//            values.put(DatabaseHelper.COLUMN_NO_OF_WAITING, publicVideoVO.getNoOfWaiting());
//            values.put(DatabaseHelper.COLUMN_NO_OF_CONVERSATIONS, publicVideoVO.getNoOfConversation());
//            values.put(DatabaseHelper.COLUMN_IS_FLAG, publicVideoVO.getIsFlag());
//            values.put(DatabaseHelper.COLUMN_DESCRIPTION, publicVideoVO.getDescription());
//            values.put(DatabaseHelper.COLUMN_DURATION, publicVideoVO.getDuration());
//            values.put(DatabaseHelper.COLUMN_LINK, publicVideoVO.getLink());
//            values.put(DatabaseHelper.COLUMN_ASPECT_RATIO, publicVideoVO.getAspectRatio());
//            values.put(DatabaseHelper.COLUMN_RESOLUTION, publicVideoVO.getResolution());
//            values.put(DatabaseHelper.COLUMN_SIZE, publicVideoVO.getSize());
//            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, publicVideoVO.getVideoUploadStatus());
//            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, publicVideoVO.getImageUploadStatus());
//            values.put(DatabaseHelper.COLUMN_API_STATUS, publicVideoVO.getApiStatus());
//            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);
//            values.put(DatabaseHelper.COLUMN_SHARE_URL, publicVideoVO.getShareUrl());
//            values.put(DatabaseHelper.COLUMN_IS_DISCOVER_ENABLE, publicVideoVO.getDiscoverEnabled());
//            values.put(DatabaseHelper.COLUMN_QUESTIONS, publicVideoVO.getSelectedQuestions());
//            String metadata = new Gson().toJson(publicVideoVO.getMetaData(), MetaDataModel.class);
//            values.put(DatabaseHelper.COLUMN_META_DATA, metadata);
//            values.put(DatabaseHelper.COLUMN_QR_CODE, publicVideoVO.getQrCode());
//            values.put(DatabaseHelper.COLUMN_FFMPEG_COMMAND, publicVideoVO.getFfMpegCommand());
//            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, publicVideoVO.getCompressionStatus());
//            return mDatabase.insert(DatabaseHelper.TABLE_PUBLIC_VIDEO, null, values);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return -1;
//    }

    public void deletePublicVideoRecord(String videoId) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_PUBLIC_VIDEO, DatabaseHelper.COLUMN_VIDEO_ID + " =  '" + videoId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    private long getDifferenceBetweenDates(String datetime, String lastPlayedTime) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
        Date date1;
        long diff = -1;
        try {
            date1 = simpleDateFormat.parse(datetime);
            Date date2 = simpleDateFormat.parse(lastPlayedTime);
            diff = (date2.getTime() / 60) / 1000 - (date1.getTime() / 60) / 1000;
            return diff;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diff;
    }

    @SuppressLint("Range")
    public boolean getVideoCachePresent(String url) {
        boolean isVideoPresent = false;
        String videoPath;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_VIDEO_CACHE + " WHERE " + DatabaseHelper.COLUMN_VIDEO_PATH + " = '" + url + "'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);

            if (mCursor != null) {
                if (mCursor.moveToFirst()) {

                    do {
                        videoPath = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_PATH));
                        isVideoPresent = !videoPath.isEmpty();
                    }
                    while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return isVideoPresent;
    }

//    public List<PublicVideoModel> getPublicVideosWithLocalPath() {
//        List<PublicVideoModel> publicVideoVOList = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO + " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " != '' AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + "= 2 AND " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + "= 2 AND " + DatabaseHelper.COLUMN_API_STATUS + "= 1 ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC";
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                if (mCursor.moveToFirst()) {
//                    do {
//                        PublicVideoModel mPublicVideoVO = new PublicVideoModel();
//                        mPublicVideoVO.setVideoId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_ID)));
//                        mPublicVideoVO.setLocalVideoPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH)));
//                        mPublicVideoVO.setImagePath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH)));
//                        publicVideoVOList.add(mPublicVideoVO);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return publicVideoVOList;
//    }

    public List<DiscoverModel> getPendingPublicVideos() {
        List<DiscoverModel> publicVideoVOList = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO + " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " != '' AND (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + "!= 2 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + "!= 2 OR " + DatabaseHelper.COLUMN_API_STATUS + "!= 1) ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        DiscoverModel mPublicVideoVO = getVideoObjectWithCursor(mCursor);
                        publicVideoVOList.add(mPublicVideoVO);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return publicVideoVOList;
    }

    public List<CommentModel> getPendingComments(String videoId) {
        List<CommentModel> commentList = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery;
            if (!TextUtils.isEmpty(videoId)) {
                selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_COMMENTS + " WHERE " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " != '' AND " + DatabaseHelper.COLUMN_CONVERSATION_ID + " = '" + videoId + "' AND (" + DatabaseHelper.COLUMN_FILE_UPLOAD_STATUS + " != 2 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " != 2 OR " + DatabaseHelper.COLUMN_API_STATUS +
                        " != 2 OR " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " != 1) ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC ";
            } else {
                selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_COMMENTS + " WHERE " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " != '' AND (" + DatabaseHelper.COLUMN_FILE_UPLOAD_STATUS + "!= 2 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + "!= 2 OR " + DatabaseHelper.COLUMN_API_STATUS + "!= 2 OR " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " != 1) ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC";
            }
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        CommentModel comment = getCommentObjectWithCursor(mCursor);
                        commentList.add(comment);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return commentList;
    }

    @SuppressLint("Range")
    private CommentModel getCommentObjectWithCursor(Cursor mCursor) {
        CommentModel comment = new CommentModel();
        comment.setCommentId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMMENT_ID)));
        comment.setChatId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CHAT_ID)));
        comment.setVideoId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_ID)));
        comment.setThumbnail(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL)));
        comment.setFileURL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_URL)));
        comment.setFileLocalVideoPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_LOCAL_PATH)));
        comment.setImageLocalVideoPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH)));
        comment.setCreatedAt(String.valueOf(mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT))));
        comment.setDuration(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION)));
        comment.setLink(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_LINK)));
        comment.setRead(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_READ)) == 1);
        comment.setRetry(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_RETRY)) == 1);
        comment.setFileUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_UPLOAD_STATUS)));
        comment.setImageUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS)));
        comment.setApiStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_API_STATUS)));
        comment.setShareURL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL)));
        comment.setNoOfViews(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIEWS)));
        comment.setFileType(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_TYPE)));
        comment.setFfMpegCommand(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FFMPEG_COMMAND)));
        comment.setCompressionStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMPRESSION_STATUS)));
        comment.setCommentText(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMMENT_TEXT)));
        comment.setCommentData(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMMENT_DATA)));
        if (!TextUtils.isEmpty(comment.getCommentData())) {
            comment.prepareCommentDataList();
        }
        MembersModel owner = new MembersModel();
        owner.setUserId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
        owner.setName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
        owner.setNickname(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
        owner.setAvatar(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
        owner.setProfileImage(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
        owner.setProfileImageL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
        owner.setProfileImageM(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
        owner.setProfileImageS(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
        owner.setBio(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
        comment.setOwner(owner);
        Type questionType = new TypeToken<List<QuestionModel>>() {
        }.getType();
        String selectedQuestions = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_QUESTIONS));
        if (!TextUtils.isEmpty(selectedQuestions)) {
            Gson gson = new Gson();
            List<QuestionModel> questions = gson.fromJson(selectedQuestions, questionType);
            comment.setQuestions(questions);
        }
        String metaData = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_META_DATA));
        if (!TextUtils.isEmpty(metaData)) {
            MetaDataModel metaDataModel = new Gson().fromJson(metaData, MetaDataModel.class);
            comment.setMetaData(metaDataModel);
        }
        return comment;
    }

    public List<DiscoverModel> getFailedToUploadVideos() {
        List<DiscoverModel> publicVideoVOList = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO + " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " != '' AND (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + "!= 2 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + "!= 2 OR " + DatabaseHelper.COLUMN_API_STATUS + "!= 1 OR " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " != 1) ORDER BY " + DatabaseHelper.COLUMN_ID + " DESC";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        DiscoverModel mPublicVideoVO = getVideoObjectWithCursor(mCursor);
                        publicVideoVOList.add(mPublicVideoVO);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return publicVideoVOList;
    }

    public void updatePublicVideo(String localVideoPath, String videoId, String videoPath, String thumbnailUrl, String shareUrl) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_VIDEO_ID, videoId);
            values.put(DatabaseHelper.COLUMN_VIDEO_URL, videoPath);
            values.put(DatabaseHelper.COLUMN_THUMBNAIL, thumbnailUrl);
            values.put(DatabaseHelper.COLUMN_SHARE_URL, shareUrl);
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 2);
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);
            values.put(DatabaseHelper.COLUMN_API_STATUS, 1);
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);

            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + localVideoPath + "'", null);
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateCanPrivateChat(String phone) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CAN_PRIVATE_CHAT, 0);
            mDatabase.update(DatabaseHelper.TABLE_CONTACTS, values, DatabaseHelper.COLUMN_CONTACTS_PHONE + " =  '" + phone + "'", null);
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public boolean checkPublicVideoStatus(String localVideoPath) {
        boolean isUploaded = false;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'" +
                    " AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 2";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.getCount() > 0) {
                    isUploaded = true;
                }
                mCursor.close();
            }

        } catch (IllegalStateException e) {
            Utility.showLogException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return isUploaded;
    }

    public boolean checkPublicImageStatus(String localVideoPath) {
        boolean isUploaded = false;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'" +
                    " AND " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 2";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.getCount() > 0) {
                    isUploaded = true;
                }
                mCursor.close();
            }

        } catch (IllegalStateException e) {
            Utility.showLogException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return isUploaded;
    }

    public void updatePublicCoverPhoto(String videoPath, String imagePath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH, imagePath);
            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + videoPath + "'", null);
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updatePublicVideoStatus(String videoPath, int video_upload_status) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, video_upload_status);
            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + videoPath + "'", null);
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updatePublicImageStatus(String videoPath, int image_upload_status) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, image_upload_status);
            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + videoPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateDpInConversation(String chatid, String dp) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_DP, dp);
            values.put(DatabaseHelper.COLUMN_DP_S, dp);
            mDatabase.update(DatabaseHelper.TABLE_GROUP, values, DatabaseHelper.COLUMN_CHAT_ID + " =  '" + chatid + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updatePublicCompressionStatus(String videoPath, int compression_status) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, compression_status);
            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + videoPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void deletePublicVideoByPath(String videoPath) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_PUBLIC_VIDEO, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + videoPath + "'" + " AND ", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateProfileRetryStatus(String videoPath, boolean isRetry) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, isRetry ? 1 : 0);
            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + videoPath + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public boolean isVideoIdExist(String videoId) {
        String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO + " WHERE " + DatabaseHelper.COLUMN_VIDEO_ID + " =  '" + videoId + "'";
        Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
        return mCursor != null && mCursor.getCount() > 0;
    }

    public String isLocalPathExist(String videoId) {
        String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_PUBLIC_VIDEO + " WHERE " + DatabaseHelper.COLUMN_VIDEO_ID + " =  '" + videoId + "'";
        Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            if (mCursor.moveToFirst()) {
                @SuppressLint("Range") String localVideoPath = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH));
                if (localVideoPath != null && localVideoPath.length() > 0) {
                    return localVideoPath;
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public List<ChatModel> deleteChatsExceptFailed(List<ConversationModel> conversationList, boolean isReaction) {
        List<ChatModel> listChats = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery;
            if (isReaction) {
                selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '-101' AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4";
            } else {
                selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 2 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 0) AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101' AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4";
            }
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails(chat.getChatId(), chat.getVideoUrl()));
                        if (!chat.getChatId().equalsIgnoreCase("-101")) {
                            if (!isDuplicateReplyEntry(conversationList, chat)) {
                                if (chat.getIsReplyReceived() == 1) {
                                    if (conversationList != null) {
                                        listChats.add(chat);
                                    }
                                } else {
                                    listChats.add(chat);
                                }
                            }
                        } else {
                            listChats.add(chat);
                        }
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

            String whereClause;
            if (isReaction) {
                whereClause = DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION + "= 1 AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4";
            } else {
                whereClause = DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION + "= 2 AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4";
            }

            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, whereClause, null);
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listChats;
    }

    public List<ChatModel> deleteRTsExceptFailed() {
        List<ChatModel> listChats = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 2 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 1) AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4";

            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails(chat.getChatId(), chat.getVideoUrl()));
                        listChats.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

            String whereClause = DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4";
            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, whereClause, null);
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listChats;
    }

    // This function will delete all loops except failed or in progress to uploads
    public void deleteLoopsExceptFailed() {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Delete all loops from required tables
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'", null);
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 3", null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'", null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP_MEMBERS, DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'", null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_SUBSCRIBERS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_PENDING_REQUESTS, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will delete all messages of loops from given chatId
    public void deleteMessagesForLoop(String chatId) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare where condition to delete message from message master
            String whereClause = DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Delete message from message master
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, whereClause, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    private boolean isDuplicateReplyEntry(List<ConversationModel> conversationList, ChatModel mChat) {
        boolean isDuplicateEntry = false;
        try {
            for (int i = 0; i < conversationList.size(); i++) {
                if (conversationList.get(i).getChatId().equalsIgnoreCase(mChat.getChatId())) {
                    List<ChatModel> chatList = conversationList.get(i).getChats();
                    if (chatList != null && chatList.size() > 0) {
                        for (int j = 0; j < chatList.size(); j++) {
                            String videoURL = chatList.get(j).getVideoUrl();
                            int slashPos = videoURL.lastIndexOf('/');
                            String fileName = videoURL.substring(slashPos);
                            String localPath = mChat.getLocalVideoPath();
                            int localSlashPos = localPath.lastIndexOf('/');
                            String localFileName = localPath.substring(localSlashPos);
                            if (fileName.equalsIgnoreCase(localFileName)) {
                                isDuplicateEntry = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        }
        return isDuplicateEntry;
    }

    public void insertConversations(List<ConversationModel> conversationList, boolean isReaction) {
        try {
            List<ChatModel> failedChats = deleteChatsExceptFailed(conversationList, isReaction);
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_GROUP, DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION + "=" + (isReaction ? 1 : 2) + " AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101' AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4", null);
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION + "=" + (isReaction ? 1 : 2) + " AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101' AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4", null);
            insertConversationsWithoutTransaction(conversationList, failedChats, isReaction);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void insertRoundTables(List<ConversationModel> conversationList) {
        try {
            List<ChatModel> failedChats = deleteRTsExceptFailed();
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_GROUP, DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'", null);
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'", null);
            mDatabase.delete(DatabaseHelper.TABLE_SUBSCRIBERS, null, null);
            mDatabase.delete(DatabaseHelper.TABLE_PENDING_REQUESTS, null, null);
            insertConversationsWithoutTransaction(conversationList, failedChats, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will insert/update loops in DB
    public void insertORUpdateLoops(List<LoopsModel> loopList) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Insert/update loops in DB
            insertLoopsWithoutTransaction(loopList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will insert/update loops in DB
    private void insertLoopsWithoutTransaction(List<LoopsModel> loopList) {
        if (loopList != null && loopList.size() > 0) {
            for (int i = 0; i < loopList.size(); i++) {
                try {
                    LoopsModel loop = loopList.get(i);

                    // Get the messages for Loop
                    List<MessageModel> messageList = loop.getLatestMessages();

                    // Insert or update loop master
                    insertORUpdateLoopMaster(loop);

                    if (messageList != null && messageList.size() > 0) {

                        // Prepare and add missing parameters for message object
                        for (int j = 0; j < messageList.size(); j++) {
                            MessageModel message = messageList.get(j);
                            message.setLocalVideoPath("");
                            message.setLocalImagePath("");
                            message.setVideoUploadStatus(3);
                            message.setImageUploadStatus(2);
                            message.setDpUploadStatus(2);
                            message.setCompressionStatus(1);
                            message.setFfMpegCommand("");
                            message.setRetry(false);
                            message.setConvType(loop.getConvType());
                        }

                        // Insert or update loop messages based on chatId
                        insertORUpdateLoopVideoList(messageList, loop.getChatId());
                    }

                    if (loop.getGroup() != null) {
                        GroupModel group = loop.getGroup();

                        // Insert or update loop group members
                        insertLoopMembersWithoutDBTransition(loop.getChatId(), group, loop.getConvType());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // This function will insert or update loop in loop master
    private void insertORUpdateLoopMaster(LoopsModel loop) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CHAT_ID, loop.getChatId());
        values.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, loop.getConvType());
        values.put(DatabaseHelper.COLUMN_SHARE_URL, loop.getShareUrl());

        // Check for null for local video path as if null is inserted in db than UNIQUE constraint will not work for DB
        if (!TextUtils.isEmpty(loop.getLocalVideoPath())) {
            values.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, loop.getLocalVideoPath());
        }

        // Add settings of loop
        if (loop.getSettings() != null) {
            String settings = new Gson().toJson(loop.getSettings(), SettingsModel.class);
            values.put(DatabaseHelper.COLUMN_SETTINGS, settings);
        }
        try {
            values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, loop.getNoOfViews());
            values.put(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT, Long.parseLong(loop.getUnreadMessageCount()));
            values.put(DatabaseHelper.COLUMN_LATEST_MESSAGE_AT, Long.parseLong(loop.getLatestMessageAt()));
            if (!TextUtils.isEmpty(loop.getCommunityId())) {
                values.put(DatabaseHelper.COLUMN_COMMUNITY_ID, loop.getCommunityId());
            }

            if (loop.getTemplateId() != null && loop.getTemplateId() != 0) {
                values.put(DatabaseHelper.COLUMN_TEMPLATE_ID, loop.getTemplateId());
            }

            values.put(DatabaseHelper.COLUMN_IS_WELCOME_LOOP, loop.isWelcomeLoop() ? 1 : 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert or update loop in loop master
        long id = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + loop.getChatId() + "'", null);
        }
    }

    // This function will update loop settings in loop master
    public void updateSettingsInLoopMaster(String chatId, String settingsJson) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update settings
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_SETTINGS, settingsJson);

            // Update settings in loop master
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update loop group in group master
    public void updateGroupInLoopGroupMaster(GroupModel group) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare contentValues object for group
            ContentValues groupValues = new ContentValues();
            groupValues.put(DatabaseHelper.COLUMN_GROUP_NAME, group.getName());
            groupValues.put(DatabaseHelper.COLUMN_GROUP_ID, group.getGroupId());
            groupValues.put(DatabaseHelper.COLUMN_GROUP_DESC, group.getDescription());
            groupValues.put(DatabaseHelper.COLUMN_DP, group.getDp());
            groupValues.put(DatabaseHelper.COLUMN_DP_S, group.getSmallDp());
            groupValues.put(DatabaseHelper.COLUMN_DP_COLOR, group.getColorCode());
            groupValues.put(DatabaseHelper.COLUMN_DP_TEXT_COLOR, group.getTextColorCode());

            // Update group in loop group master
            mDatabase.update(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, groupValues, DatabaseHelper.COLUMN_GROUP_ID + " = '" + group.getGroupId() + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will insert/update group members by given chatId
    public void insertORUpdateGroupMembers(String chatId, List<MembersModel> membersList) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            if (membersList != null && membersList.size() > 0) {

                // For each loop to iterate member list
                for (int k = 0; k < membersList.size(); k++) {
                    MembersModel member = membersList.get(k);

                    // Insert or update member in group
                    insertORUpdateGroupMember(chatId, member, VideoConvType.ROUND_TABLE.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will remove group members by given chatId
    public void removeGroupMembers(String chatId, List<String> memberIds) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Make comma separated string to pass in 'IN' query
            String commaSeparatedMemberIds = TextUtils.join("','", memberIds);

            // Prepare where condition to delete members from group members
            String whereClause = DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND " + DatabaseHelper.COLUMN_USER_ID + " IN ('" + commaSeparatedMemberIds + "')";

            // Delete members from group members
            mDatabase.delete(DatabaseHelper.TABLE_GROUP_MEMBERS, whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will retrieve chatId based on given groupId
    @SuppressLint("Range")
    public String getChatIdByGroupId(String groupId) {
        String chatId = "";
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare select query to get chatId
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_CHAT_ID +
                    " FROM " + DatabaseHelper.TABLE_LOOP_GROUP_MASTER + " WHERE " + DatabaseHelper.COLUMN_GROUP_ID + " = '" + groupId + "'";

            // Execute query and get chatId
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    chatId = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CHAT_ID));
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return chatId;
    }


    public void insertNewConversation(List<ConversationModel> conversationList) {
        try {
            mDatabase.beginTransaction();
            insertConversationsWithoutTransaction(conversationList, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    private void insertConversationsWithoutTransaction(List<ConversationModel> conversationList, List<ChatModel> failedChats, boolean isReaction) {
        if (conversationList != null && conversationList.size() > 0) {
            for (int i = 0; i < conversationList.size(); i++) {
                try {
                    ConversationModel conversation = conversationList.get(i);
                    List<ChatModel> chatList = conversation.getChats();
                    if (chatList != null && chatList.size() > 0) {
                        for (int j = 0; j < chatList.size(); j++) {
                            ChatModel chat = chatList.get(j);
                            chat.setLocalVideoPath("");
                            chat.setImagePath("");
                            chat.setFirstVideoLocalPath("");
                            chat.setVideoUploadStatus(3);
                            chat.setImageUploadStatus(2);
                            chat.setDpUploadStatus(2);
                            chat.setCompressionStatus(1);
                            chat.setFfMpegCommand("");
                            chat.setIsReplyReceived(1);
                            chat.setIsReplyOrReaction(isReaction ? 1 : 2);
                            chat.setRetry(false);
                            chat.setFromStatus(TextUtils.isEmpty(conversation.getFromStatus()) ? "" : conversation.getFromStatus());
                            chat.setConvType(conversation.getConvType());
                            chat.setConvShareURL(conversation.getShareURL());
                            chat.setConvNoOfViews(conversation.getNoOfViews());
                            chat.setSettings(conversation.getSettings());
                        }
                        insertChatList(chatList, conversation.getChatId());
                    }

                    if (conversation.getGroup() != null) {
                        GroupModel group = conversation.getGroup();
                        if (conversation.getChats() != null && conversation.getChats().size() > 0) {
                            long timeStamp = Long.parseLong(conversation.getChats().get(0).getConversationAt());
                            insertMembersWithoutDBTransition(conversation.getChatId(), group, timeStamp, conversation.getConvType(), isReaction ? 1 : 2);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (failedChats != null && failedChats.size() > 0) {
            for (int j = 0; j < failedChats.size(); j++) {
                ChatModel chat = failedChats.get(j);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_CHAT_ID, chat.getChatId());
                values.put(DatabaseHelper.COLUMN_THUMBNAIL, chat.getThumbnailUrl());
                values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L, chat.getVideoThumbnailLarge());
                values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S, chat.getVideoThumbnailSmall());
                values.put(DatabaseHelper.COLUMN_VIDEO_URL, chat.getVideoUrl());
                values.put(DatabaseHelper.COLUMN_VIDEO_URL_M3U8, chat.getVideoUrlM3U8());
                values.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, chat.getLocalVideoPath());
                values.put(DatabaseHelper.COLUMN_FIRST_VIDEO_LOCAL_PATH, chat.getFirstVideoLocalPath());
                values.put(DatabaseHelper.COLUMN_CONVERSATION_ID, chat.getConversationId());
                values.put(DatabaseHelper.COLUMN_CONVERSATION_AT, Long.parseLong(chat.getConversationAt()));
                values.put(DatabaseHelper.COLUMN_DURATION, chat.getDuration());
                values.put(DatabaseHelper.COLUMN_LINK, chat.getLink());
                values.put(DatabaseHelper.COLUMN_ASPECT_RATIO, chat.getAspectRatio());
                values.put(DatabaseHelper.COLUMN_RESOLUTION, chat.getResolution());
                values.put(DatabaseHelper.COLUMN_SIZE, chat.getSize());
                try {
                    values.put(DatabaseHelper.COLUMN_IS_READ, chat.getRead() ? 1 : 0);
                } catch (Exception e) {
                    values.put(DatabaseHelper.COLUMN_IS_READ, 0);
                }
                values.put(DatabaseHelper.COLUMN_IS_REPLY, chat.getReply() ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, chat.getVideoUploadStatus());
                values.put(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH, chat.getImagePath());
                values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, chat.getImageUploadStatus());
                values.put(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS, chat.getDpUploadStatus());
                values.put(DatabaseHelper.COLUMN_IS_REPLY_RECEIVED, chat.getIsReplyReceived());
                values.put(DatabaseHelper.COLUMN_IS_RETRY, chat.isRetry() ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION, chat.getIsReplyOrReaction());
                values.put(DatabaseHelper.COLUMN_FROM_STATUS, chat.getFromStatus());
                values.put(DatabaseHelper.COLUMN_IS_FRONT, chat.isFront() ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, chat.getConvType());
                values.put(DatabaseHelper.COLUMN_DESCRIPTION, chat.getDescription());
                values.put(DatabaseHelper.COLUMN_SHARE_URL, chat.getShareURL());
                values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, chat.getNoOfViews());
                values.put(DatabaseHelper.COLUMN_NO_OF_COMMENTS, chat.getNoOfComments());
                values.put(DatabaseHelper.COLUMN_CONVERSATION_SHARE_URL, chat.getConvShareURL());
                values.put(DatabaseHelper.COLUMN_CONVERSATION_NO_OF_VIEWS, chat.getConvNoOfViews());
                values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, chat.getCompressionStatus());
                values.put(DatabaseHelper.COLUMN_FFMPEG_COMMAND, chat.getFfMpegCommand());
                if (chat.getOwner() != null) {
                    MembersModel member = chat.getOwner();
                    values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
                    values.put(DatabaseHelper.COLUMN_NAME, member.getName());
                    values.put(DatabaseHelper.COLUMN_NICKNAME, member.getNickname());
                    values.put(DatabaseHelper.COLUMN_IS_AVATAR, member.isAvatar() ? 1 : 0);
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, member.getProfileImage());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, member.getProfileImageS());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, member.getProfileImageM());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, member.getProfileImageL());
                    values.put(DatabaseHelper.COLUMN_CONTACTS_BIO, member.getBio());
                }
                if (chat.getQuestions() != null && chat.getQuestions().size() > 0) {
                    Type questionType = new TypeToken<List<QuestionModel>>() {
                    }.getType();
                    Gson gson = new Gson();
                    String selectedQuestions = gson.toJson(chat.getQuestions(), questionType);
                    values.put(DatabaseHelper.COLUMN_QUESTIONS, selectedQuestions);
                }
                if (chat.getMetaData() != null) {
                    String metaData = new Gson().toJson(chat.getMetaData(), MetaDataModel.class);
                    values.put(DatabaseHelper.COLUMN_META_DATA, metaData);
                }
                if (chat.getSettings() != null) {
                    String settings = new Gson().toJson(chat.getSettings(), SettingsModel.class);
                    values.put(DatabaseHelper.COLUMN_SETTINGS, settings);
                }
                if (chat.getRepostModel() != null) {
                    String repostData = new Gson().toJson(chat.getRepostModel(), RepostModel.class);
                    values.put(DatabaseHelper.COLUMN_REPOST, repostData);
                }
                mDatabase.insert(DatabaseHelper.TABLE_CHAT_MASTER, null, values);
            }
        }
    }

    public void insertSavedVideos(List<DiscoverModel> savedVideoList) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_SAVED_VIDEO, null, null);
            if (savedVideoList != null && savedVideoList.size() > 0) {
                for (int i = 0; i < savedVideoList.size(); i++) {
                    try {
                        DiscoverModel savedVideo = savedVideoList.get(i);
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_USER_ID, savedVideo.getUserId());
                        values.put(DatabaseHelper.COLUMN_VIDEO_ID, savedVideo.getVideoId());
                        values.put(DatabaseHelper.COLUMN_VIDEO_URL, savedVideo.getVideoUrl());
                        values.put(DatabaseHelper.COLUMN_THUMBNAIL, savedVideo.getVideoThumbnail());
                        values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S, savedVideo.getVideoThumbnailSmall());
                        values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L, savedVideo.getVideoThumbnailLarge());
                        values.put(DatabaseHelper.COLUMN_DURATION, savedVideo.getDuration());
                        values.put(DatabaseHelper.COLUMN_LINK, savedVideo.getLink());
                        values.put(DatabaseHelper.COLUMN_DESCRIPTION, savedVideo.getDescription());
                        values.put(DatabaseHelper.COLUMN_ASPECT_RATIO, savedVideo.getAspectRatio());
                        values.put(DatabaseHelper.COLUMN_RESOLUTION, savedVideo.getResolution());
                        values.put(DatabaseHelper.COLUMN_SIZE, savedVideo.getSize());
                        values.put(DatabaseHelper.COLUMN_SHARE_URL, savedVideo.getShareURL());
                        values.put(DatabaseHelper.COLUMN_SAVED_AT, Long.parseLong(savedVideo.getSavedAt()));
                        values.put(DatabaseHelper.COLUMN_IS_AVATAR, savedVideo.getAvatar() ? 1 : 0);
                        values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, savedVideo.getProfileImage());
                        values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, savedVideo.getProfileImageL());
                        values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, savedVideo.getProfileImageM());
                        values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, savedVideo.getProfileImageS());
                        mDatabase.insert(DatabaseHelper.TABLE_SAVED_VIDEO, null, values);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

//    public void insertQueue(List<QueueModel> queueList) {
//        try {
//            mDatabase.beginTransaction();
//            mDatabase.delete(DatabaseHelper.TABLE_QUEUE, null, null);
//            if (queueList != null && queueList.size() > 0) {
//                for (int i = 0; i < queueList.size(); i++) {
//                    try {
//                        QueueModel queue = queueList.get(i);
//                        ContentValues values = new ContentValues();
//                        values.put(DatabaseHelper.COLUMN_CHAT_ID, queue.chat_id);
//                        values.put(DatabaseHelper.COLUMN_THUMBNAIL, queue.thumbnail_url);
//                        values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L, queue.video_thumbnail_l);
//                        values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S, queue.video_thumbnail_s);
//                        values.put(DatabaseHelper.COLUMN_QUEUE_COUNT, queue.queue_count);
//                        values.put(DatabaseHelper.COLUMN_QUEUE_TYPE, queue.queue_type);
//                        mDatabase.insert(DatabaseHelper.TABLE_QUEUE, null, values);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//    }

    @SuppressLint("Range")
    private DiscoverModel getVideoObjectWithCursor(Cursor mCursor) {
        DiscoverModel mPublicVideoVO = new DiscoverModel();
        mPublicVideoVO.setVideoId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_ID)));
        mPublicVideoVO.setLocalVideoPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH)));
        mPublicVideoVO.setVideoUrl(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_URL)));
        mPublicVideoVO.setVideoThumbnail(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL)));
        mPublicVideoVO.setNoOfViews(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIEWS)));
        mPublicVideoVO.setNoOfConversation(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_CONVERSATIONS)));
        mPublicVideoVO.setIsFlag(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FLAG)));
        mPublicVideoVO.setDescription(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));
        mPublicVideoVO.setDuration(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION)));
        mPublicVideoVO.setLink(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_LINK)));
        mPublicVideoVO.setAspectRatio(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_ASPECT_RATIO)));
        mPublicVideoVO.setResolution(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_RESOLUTION)));
        mPublicVideoVO.setSize(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SIZE)));
        mPublicVideoVO.setVideoUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS)));
        mPublicVideoVO.setImageUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS)));
        mPublicVideoVO.setApiStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_API_STATUS)));
        mPublicVideoVO.setImagePath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH)));
        mPublicVideoVO.setShareURL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL)));
        mPublicVideoVO.setSelectedContacts(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_USERS)));
        mPublicVideoVO.setSelectedQuestions(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_QUESTIONS)));
        mPublicVideoVO.setQrCode(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_QR_CODE)));
        mPublicVideoVO.setFfMpegCommand(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FFMPEG_COMMAND)));
        mPublicVideoVO.setCompressionStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMPRESSION_STATUS)));
        mPublicVideoVO.setRetry(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_RETRY)) == 1);
        String metaData = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_META_DATA));
        MetaDataModel metaDataModel = new Gson().fromJson(metaData, MetaDataModel.class);
        @SuppressLint("Range") int isDiscoverEnabled = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_DISCOVER_ENABLE));
        SettingsModel settings = new SettingsModel();
        settings.setDiscoverable(isDiscoverEnabled == 1);
        settings.setTopic(metaDataModel.getTopic());
        settings.setContainsExternalVideos(metaDataModel.getContainsExternalVideos());
        settings.setMediaType(metaDataModel.getMediaType());
        mPublicVideoVO.setSettings(settings);
        return mPublicVideoVO;
    }

    @SuppressLint("Range")
    private ChatModel getChatObjectWithCursor(Cursor mCursor) {
        ChatModel chat = new ChatModel();
        chat.setChatId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CHAT_ID)));
        chat.setVideoUrl(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_URL)));
        chat.setVideoUrlM3U8(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_URL_M3U8)));
        chat.setThumbnailUrl(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL)));
        chat.setVideoThumbnailLarge(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L)));
        chat.setVideoThumbnailSmall(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S)));
        chat.setLocalVideoPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH)));
        chat.setFirstVideoLocalPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FIRST_VIDEO_LOCAL_PATH)));
        chat.setConversationId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_ID)));
        chat.setConversationAt(String.valueOf(mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_AT))));
        chat.setDuration(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION)));
        chat.setLink(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_LINK)));
        chat.setAspectRatio(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_ASPECT_RATIO)));
        chat.setResolution(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_RESOLUTION)));
        chat.setSize(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SIZE)));
        chat.setReply(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_REPLY)) == 1);
        chat.setRead(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_READ)) == 1);
        chat.setIsReplyReceived(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_REPLY_RECEIVED)));
        chat.setRetry(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_RETRY)) == 1);
        chat.setIsReplyOrReaction(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION)));
        chat.setVideoUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS)));
        chat.setFromStatus(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FROM_STATUS)));
        chat.setFront(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FRONT)) == 1);
        chat.setImagePath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH)));
        chat.setImageUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS)));
        chat.setDpUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS)));
        chat.setConvType(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_TYPE)));
        chat.setDescription(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));
        chat.setShareURL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL)));
        chat.setNoOfViews(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIEWS)));
        chat.setNoOfComments(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_COMMENTS)));
        chat.setConvShareURL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_SHARE_URL)));
        chat.setConvNoOfViews(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_NO_OF_VIEWS)));
        chat.setFfMpegCommand(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FFMPEG_COMMAND)));
        chat.setCompressionStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMPRESSION_STATUS)));
        MembersModel owner = new MembersModel();
        owner.setUserId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
        owner.setName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
        owner.setNickname(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
        owner.setAvatar(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
        owner.setProfileImage(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
        owner.setProfileImageL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
        owner.setProfileImageM(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
        owner.setProfileImageS(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
        owner.setBio(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
        chat.setOwner(owner);
        Type questionType = new TypeToken<List<QuestionModel>>() {
        }.getType();
        String selectedQuestions = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_QUESTIONS));
        if (!TextUtils.isEmpty(selectedQuestions)) {
            Gson gson = new Gson();
            List<QuestionModel> questions = gson.fromJson(selectedQuestions, questionType);
            chat.setQuestions(questions);
        }
        String metaData = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_META_DATA));
        if (!TextUtils.isEmpty(metaData)) {
            MetaDataModel metaDataModel = new Gson().fromJson(metaData, MetaDataModel.class);
            chat.setMetaData(metaDataModel);
        }
        String settings = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SETTINGS));
        if (!TextUtils.isEmpty(settings)) {
            SettingsModel settingsModel = new Gson().fromJson(settings, SettingsModel.class);
            chat.setSettings(settingsModel);
        }
        String repostData = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_REPOST));
        if (!TextUtils.isEmpty(repostData)) {
            RepostModel repostModel = new Gson().fromJson(repostData, RepostModel.class);
            chat.setRepostModel(repostModel);
        }
        return chat;
    }

    public List<ConversationModel> getConversations(boolean isReaction) {
        List<ConversationModel> conversationList = new ArrayList<>();
        LinkedHashMap<String, List<ChatModel>> hashMapChats = new LinkedHashMap<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery;
            if (isReaction) {
                selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION + " = 1 AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4 ORDER BY " + DatabaseHelper.COLUMN_CONVERSATION_AT + " DESC, " + DatabaseHelper.COLUMN_ID + " DESC";
            } else {
                selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_IS_REPLY_RECEIVED + " = 1 AND " + DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION + " = 2 AND " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " != 4 ORDER BY " + DatabaseHelper.COLUMN_CONVERSATION_AT + " DESC, " + DatabaseHelper.COLUMN_ID + " DESC";
            }
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        if (!hashMapChats.containsKey(chat.getChatId())) {
                            hashMapChats.put(chat.getChatId(), new ArrayList<>());
                        }
                        Objects.requireNonNull(hashMapChats.get(chat.getChatId())).add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

            for (LinkedHashMap.Entry<String, List<ChatModel>> entry : hashMapChats.entrySet()) {
                String key = entry.getKey();
                List<ChatModel> value = entry.getValue();
                if (value != null && value.size() > 0) {
                    ConversationModel conversation = new ConversationModel();
                    conversation.setChatId(key);
                    conversation.setConvType(value.get(0).getConvType());
                    if (key.equalsIgnoreCase("-101")) {
                        if (value.size() == 1 || (value.size() == 2 && conversation.getConvType() == VideoConvType.REACTION.getValue())) {
                            conversation.setGroup(getGroupDetails(key, value.get(0).getVideoUrl()));
                            conversation.setChats(value);
                            conversationList.add(conversation);
                        } else {
                            LinkedHashMap<String, List<ChatModel>> hashMapValueChats = new LinkedHashMap<>();
                            for (int i = 0; i < value.size(); i++) {
                                ChatModel chatValue = value.get(i);
                                if (!hashMapValueChats.containsKey(chatValue.getVideoUrl())) {
                                    hashMapValueChats.put(chatValue.getVideoUrl(), new ArrayList<>());
                                }
                                Objects.requireNonNull(hashMapValueChats.get(chatValue.getVideoUrl())).add(chatValue);
                            }

                            for (LinkedHashMap.Entry<String, List<ChatModel>> entryValue : hashMapValueChats.entrySet()) {
                                List<ChatModel> value1 = entryValue.getValue();
                                ConversationModel conversation1 = new ConversationModel();
                                conversation1.setChatId(key);
                                conversation1.setConvType(value1.get(0).getConvType());
                                conversation1.setGroup(getGroupDetails(key, value1.get(0).getVideoUrl()));
                                conversation1.setChats(value1);
                                conversationList.add(conversation1);
                            }
                        }
                    } else {
                        conversation.setGroup(getGroupDetails(key, ""));
                        conversation.setChats(value);
                        conversationList.add(conversation);
                    }
                } else {
                    deleteChat(key);
                }
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return conversationList;
    }

    public List<ConversationModel> getRoundTables() {
        List<ConversationModel> conversationList = new ArrayList<>();
        LinkedHashMap<String, List<ChatModel>> hashMapChats = new LinkedHashMap<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 ORDER BY " + DatabaseHelper.COLUMN_CONVERSATION_AT + " DESC, " + DatabaseHelper.COLUMN_ID + " DESC";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        if (!hashMapChats.containsKey(chat.getChatId())) {
                            hashMapChats.put(chat.getChatId(), new ArrayList<>());
                        }
                        Objects.requireNonNull(hashMapChats.get(chat.getChatId())).add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

            for (LinkedHashMap.Entry<String, List<ChatModel>> entry : hashMapChats.entrySet()) {
                String key = entry.getKey();
                List<ChatModel> value = entry.getValue();
                if (value != null && value.size() > 0) {
                    ConversationModel conversation = new ConversationModel();
                    conversation.setChatId(key);
                    conversation.setConvType(value.get(0).getConvType());
                    conversation.setShareURL(value.get(0).getConvShareURL());
                    conversation.setNoOfViews(value.get(0).getConvNoOfViews());
                    conversation.setSettings(value.get(value.size() - 1).getSettings());
                    if (key.equalsIgnoreCase("-101")) {
                        if (value.size() == 2) {
                            conversation.setGroup(getGroupDetails(key, value.get(0).getVideoUrl()));
                            conversation.setChats(value);
                            conversationList.add(conversation);
                        } else {
                            LinkedHashMap<String, List<ChatModel>> hashMapValueChats = new LinkedHashMap<>();
                            for (int i = 0; i < value.size(); i++) {
                                ChatModel chatValue = value.get(i);
                                if (!hashMapValueChats.containsKey(chatValue.getVideoUrl())) {
                                    hashMapValueChats.put(chatValue.getVideoUrl(), new ArrayList<>());
                                }
                                Objects.requireNonNull(hashMapValueChats.get(chatValue.getVideoUrl())).add(chatValue);
                            }

                            for (LinkedHashMap.Entry<String, List<ChatModel>> entryValue : hashMapValueChats.entrySet()) {
                                List<ChatModel> value1 = entryValue.getValue();
                                ConversationModel conversation1 = new ConversationModel();
                                conversation1.setChatId(key);
                                conversation1.setConvType(value1.get(0).getConvType());
                                conversation1.setShareURL(value1.get(0).getConvShareURL());
                                conversation1.setNoOfViews(value1.get(0).getConvNoOfViews());
                                conversation1.setSettings(value1.get(value1.size() - 1).getSettings());
                                conversation1.setGroup(getGroupDetails(key, value1.get(0).getVideoUrl()));
                                conversation1.setChats(value1);
                                conversationList.add(conversation1);
                            }
                        }
                    } else {
                        conversation.setGroup(getGroupDetails(key, ""));
                        conversation.setChats(value);
                        conversationList.add(conversation);
                    }
                } else {
                    deleteChat(key);
                }
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return conversationList;
    }

    // This function will retrieve all loops from DB
    public ArrayList<LoopsModel> getLoops() {
        ArrayList<LoopsModel> loopList = new ArrayList<>();
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving loops from loop master
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " + DatabaseHelper.COLUMN_CONVERSATION_TYPE + " = 4 ORDER BY " + DatabaseHelper.COLUMN_LATEST_MESSAGE_AT + " DESC";

            // Execute query to retrieve loops
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        // Retrieve loop object from cursor
                        LoopsModel loop = getLoopObjectWithCursor(mCursor);
                        loopList.add(loop);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

            // Add LatestMessages and Group details for each loop
            for (int i = 0; i < loopList.size(); i++) {
                LoopsModel loopsModel = loopList.get(i);
                // Set LatestMessages for loop
                loopsModel.setLatestMessages(getMessagesForLoop(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
                // Set Group for loop
                loopsModel.setGroup(getLoopGroupDetails(loopsModel.getChatId(), loopsModel.getLocalVideoPath()));
            }
        } catch (Exception e) {
            Utility.showLogException(e);
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // Return loop list
        return loopList;
    }

    // This function will prepare loop object from given cursor
    @SuppressLint("Range")
    private LoopsModel getLoopObjectWithCursor(Cursor mCursor) {

        // Prepare loop object from given cursor
        LoopsModel loop = new LoopsModel();
        loop.setChatId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CHAT_ID)));
        loop.setConvType(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_TYPE)));
        loop.setShareUrl(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL)));
        loop.setLocalVideoPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH)));
        loop.setLatestMessageAt(String.valueOf(mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_LATEST_MESSAGE_AT))));
        loop.setUnreadMessageCount(String.valueOf(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT))));
        loop.setNoOfViews(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIEWS)));
        loop.setCommunityId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMMUNITY_ID)));
        loop.setTemplateId(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_TEMPLATE_ID)));
        loop.setWelcomeLoop(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_WELCOME_LOOP)) == 1);

        // Retrieve settings object for loop
        String settings = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SETTINGS));
        if (!TextUtils.isEmpty(settings)) {
            SettingsModel settingsModel = new Gson().fromJson(settings, SettingsModel.class);
            loop.setSettings(settingsModel);
        }

        // Return loop object
        return loop;
    }

    // This function will retrieve all messages for loop
    private ArrayList<MessageModel> getMessagesForLoop(String chatId, String localVideoPath) {
        ArrayList<MessageModel> messageList = new ArrayList<>();

        // Prepare query for retrieving messages for loop
        String selectMessageQuery;
        if (chatId.equalsIgnoreCase("-101")) {
            // chatId = "-101" means loop is not created at, so retrieve message based on localVideoPath
            selectMessageQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " mm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON mm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE mm." + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND mm." + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "' ORDER BY mm." + DatabaseHelper.COLUMN_MESSAGE_AT + " DESC";
        } else {
            selectMessageQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " mm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON mm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE mm." + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' ORDER BY mm." + DatabaseHelper.COLUMN_MESSAGE_AT + " DESC";
        }

        // Execute query to retrieve messages
        Cursor mMessageCursor = mDatabase.rawQuery(selectMessageQuery, null);
        if (mMessageCursor != null) {
            if (mMessageCursor.moveToFirst()) {
                do {
                    // Retrieve message object from cursor
                    MessageModel message = getMessageObjectWithCursor(mMessageCursor);
                    messageList.add(message);
                } while (mMessageCursor.moveToNext());
            }
            mMessageCursor.close();
        }

        // Return message list
        return messageList;
    }

    // This function will retrieve message for loop
    private MessageModel getMessageByMessageId(String messageId) {
        MessageModel message = new MessageModel();

        // Prepare query for retrieving messages for loop
        String selectMessageQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " mm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON mm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE mm." + DatabaseHelper.COLUMN_MESSAGE_ID + " = '" + messageId + "' ORDER BY mm." + DatabaseHelper.COLUMN_MESSAGE_AT + " DESC";

        // Execute query to retrieve messages
        Cursor mMessageCursor = mDatabase.rawQuery(selectMessageQuery, null);
        if (mMessageCursor != null) {
            if (mMessageCursor.moveToFirst()) {
                // Retrieve message object from cursor
                message = getMessageObjectWithCursor(mMessageCursor);
            }
            mMessageCursor.close();
        }

        // return message object
        return message;
    }

    // This function will prepare message object from given cursor
    @SuppressLint("Range")
    private MessageModel getMessageObjectWithCursor(Cursor mCursor) {

        // Prepare message object
        MessageModel message = new MessageModel();
        message.setChatId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CHAT_ID)));
        message.setMediaUrl(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_MEDIA_URL)));
        message.setMediaUrlM3U8(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_MEDIA_URL_M3U8)));
        message.setThumbnailUrl(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL)));
        message.setVideoThumbnailLarge(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L)));
        message.setVideoThumbnailSmall(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S)));
        message.setLocalVideoPath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH)));
        message.setMessageId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGE_ID)));
        message.setMessageAt(String.valueOf(mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGE_AT))));
        message.setLink(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_LINK)));
        message.setRead(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_READ)) == 1);
        message.setRetry(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_RETRY)) == 1);
        message.setVideoUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS)));
        message.setLocalImagePath(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH)));
        message.setImageUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS)));
        message.setDpUploadStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS)));
        message.setConvType(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_TYPE)));
        message.setMessageSummary(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGE_SUMMARY)));
        message.setShareURL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL)));
        message.setNoOfViews(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIEWS)));
        message.setNoOfComments(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_COMMENTS)));
        message.setFfMpegCommand(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FFMPEG_COMMAND)));
        message.setCompressionStatus(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_COMPRESSION_STATUS)));

        // Prepare owner object of the message
        MembersModel owner = new MembersModel();
        owner.setUserId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
        owner.setName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
        owner.setNickname(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
        owner.setUserName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
        owner.setAvatar(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
        owner.setProfileImage(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
        owner.setProfileImageL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
        owner.setProfileImageM(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
        owner.setProfileImageS(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
        owner.setBio(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
        message.setOwner(owner);

        // Prepare questions list of the message
        Type questionType = new TypeToken<List<QuestionModel>>() {
        }.getType();
        String selectedQuestions = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_QUESTIONS));
        if (!TextUtils.isEmpty(selectedQuestions)) {
            Gson gson = new Gson();
            List<QuestionModel> questions = gson.fromJson(selectedQuestions, questionType);
            message.setQuestions(questions);
        }

        // Prepare metadata of the message
        String metaData = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_META_DATA));
        if (!TextUtils.isEmpty(metaData)) {
            MetaDataModel metaDataModel = new Gson().fromJson(metaData, MetaDataModel.class);
            message.setMetaData(metaDataModel);
        }

        // Prepare repost object of the message
        String repostData = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_REPOST));
        if (!TextUtils.isEmpty(repostData)) {
            RepostModel repostModel = new Gson().fromJson(repostData, RepostModel.class);
            message.setRepostModel(repostModel);
        }

        // Return message object
        return message;
    }

    @SuppressLint("Range")
    public GroupModel getGroupDetails(String chatId, String videoURL) {
        GroupModel group = new GroupModel();
        List<MembersModel> membersList = new ArrayList<>();
        List<MembersModel> subscribersList = new ArrayList<>();
        List<MembersModel> pendingReqList = new ArrayList<>();
        String selectMemberQuery;
        if (chatId.equalsIgnoreCase("-101")) {
            selectMemberQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATION_MEMBERS + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND " + DatabaseHelper.COLUMN_VIDEO_URL + " = '" + videoURL + "'";
        } else {
            selectMemberQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATION_MEMBERS + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";
        }
        Cursor mMemberCursor = mDatabase.rawQuery(selectMemberQuery, null);
        if (mMemberCursor != null) {
            if (mMemberCursor.moveToFirst()) {
                do {
                    MembersModel member = new MembersModel();
                    member.setUserId(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                    member.setName(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                    member.setNickname(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                    member.setAvatar(mMemberCursor.getInt(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                    member.setProfileImage(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                    member.setProfileImageL(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                    member.setProfileImageM(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                    member.setProfileImageS(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                    member.setPhone(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
                    member.setBio(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
                    member.setMemberRole(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_MEMBER_ROLE)));
                    member.setMemberStatus(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_MEMBER_STATUS)));
                    membersList.add(member);
                } while (mMemberCursor.moveToNext());
            }
            mMemberCursor.close();
        }

        if (!chatId.equalsIgnoreCase("-101")) {
            String selectSubscribersQuery = "SELECT * FROM " + DatabaseHelper.TABLE_SUBSCRIBERS + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";
            Cursor mSubscribersCursor = mDatabase.rawQuery(selectSubscribersQuery, null);
            if (mSubscribersCursor != null) {
                if (mSubscribersCursor.moveToFirst()) {
                    do {
                        MembersModel member = new MembersModel();
                        member.setUserId(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                        member.setName(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                        member.setNickname(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                        member.setAvatar(mSubscribersCursor.getInt(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                        member.setProfileImage(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                        member.setProfileImageL(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                        member.setProfileImageM(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                        member.setProfileImageS(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                        member.setPhone(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
                        member.setBio(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
                        subscribersList.add(member);
                    } while (mSubscribersCursor.moveToNext());
                }
                mSubscribersCursor.close();
            }

            String selectPendingReqQuery = "SELECT * FROM " + DatabaseHelper.TABLE_PENDING_REQUESTS + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";
            Cursor mPendingReqCursor = mDatabase.rawQuery(selectPendingReqQuery, null);
            if (mPendingReqCursor != null) {
                if (mPendingReqCursor.moveToFirst()) {
                    do {
                        MembersModel member = new MembersModel();
                        member.setUserId(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                        member.setName(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                        member.setNickname(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                        member.setAvatar(mPendingReqCursor.getInt(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                        member.setProfileImage(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                        member.setProfileImageL(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                        member.setProfileImageM(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                        member.setProfileImageS(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                        member.setPhone(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
                        member.setBio(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
                        pendingReqList.add(member);
                    } while (mPendingReqCursor.moveToNext());
                }
                mPendingReqCursor.close();
            }
        }

        String selectGroupQuery;
        if (chatId.equalsIgnoreCase("-101")) {
            selectGroupQuery = "SELECT * FROM " + DatabaseHelper.TABLE_GROUP + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND " + DatabaseHelper.COLUMN_VIDEO_URL + " = '" + videoURL + "'";
        } else {
            selectGroupQuery = "SELECT * FROM " + DatabaseHelper.TABLE_GROUP + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";
        }
        Cursor mGroupCursor = mDatabase.rawQuery(selectGroupQuery, null);
        if (mGroupCursor != null) {
            if (mGroupCursor.moveToFirst()) {
                group.setName(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_NAME)));
                group.setDescription(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_DESC)));
                group.setGroupId(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_ID)));
                group.setDp(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP)));
                group.setSmallDp(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_S)));
                group.setColorCode(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_COLOR)));
                group.setTextColorCode(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_TEXT_COLOR)));
                group.setNoOfViews(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIEWS)));
                group.setNoOfVideos(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIDEOS)));
                group.setNoOfMembers(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_MEMBERS)));
                group.setNoOfSubscribers(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_SUBSCRIBERS)));
            }
            mGroupCursor.close();
        }
        group.setMembers(membersList);
        group.setSubscribers(subscribersList);
        group.setRequests(pendingReqList);
        return group;
    }

    // This function will retrieve group details for loop
    @SuppressLint("Range")
    private GroupModel getLoopGroupDetails(String chatId, String localVideoPath) {
        GroupModel group = new GroupModel();
        List<MembersModel> membersList = new ArrayList<>();
        List<MembersModel> subscribersList = new ArrayList<>();
        List<MembersModel> pendingReqList = new ArrayList<>();

        // Prepare query for retrieving group members for loop
        String selectMemberQuery;
        if (chatId.equalsIgnoreCase("-101")) {
            // chatId = "-101" means loop is not created at, so retrieve group members based on localVideoPath
            selectMemberQuery = "SELECT * FROM " + DatabaseHelper.TABLE_GROUP_MEMBERS + " gm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON gm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE gm." + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND gm." + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";
        } else {
            selectMemberQuery = "SELECT * FROM " + DatabaseHelper.TABLE_GROUP_MEMBERS + " gm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON gm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE gm." + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";
        }

        // Execute query to retrieve group members
        Cursor mMemberCursor = mDatabase.rawQuery(selectMemberQuery, null);
        if (mMemberCursor != null) {
            if (mMemberCursor.moveToFirst()) {
                do {

                    // Prepare member object and add to the memberList
                    MembersModel member = new MembersModel();
                    member.setUserId(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                    member.setName(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                    member.setNickname(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                    member.setUserName(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                    member.setAvatar(mMemberCursor.getInt(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                    member.setProfileImage(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                    member.setProfileImageL(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                    member.setProfileImageM(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                    member.setProfileImageS(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                    member.setPhone(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
                    member.setBio(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
                    member.setMemberRole(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_MEMBER_ROLE)));
                    member.setMemberStatus(mMemberCursor.getString(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_MEMBER_STATUS)));
                    membersList.add(member);
                } while (mMemberCursor.moveToNext());
            }
            mMemberCursor.close();
        }

        // chatId = "-101" means loop is not created at.
        if (!chatId.equalsIgnoreCase("-101")) {

            // Prepare query for retrieving subscribers for loop
            String selectSubscribersQuery = "SELECT * FROM " + DatabaseHelper.TABLE_LOOP_SUBSCRIBERS + " sm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON sm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE sm." + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Execute query to retrieve group members
            Cursor mSubscribersCursor = mDatabase.rawQuery(selectSubscribersQuery, null);
            if (mSubscribersCursor != null) {
                if (mSubscribersCursor.moveToFirst()) {
                    do {
                        // Prepare member object and add to the subscribersList
                        MembersModel member = new MembersModel();
                        member.setUserId(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                        member.setName(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                        member.setNickname(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                        member.setUserName(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                        member.setAvatar(mSubscribersCursor.getInt(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                        member.setProfileImage(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                        member.setProfileImageL(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                        member.setProfileImageM(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                        member.setProfileImageS(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                        member.setPhone(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
                        member.setBio(mSubscribersCursor.getString(mSubscribersCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
                        subscribersList.add(member);
                    } while (mSubscribersCursor.moveToNext());
                }
                mSubscribersCursor.close();
            }

            // Prepare query for retrieving pending requests for approve/reject to join for loop
            String selectPendingReqQuery = "SELECT * FROM " + DatabaseHelper.TABLE_LOOP_PENDING_REQUESTS + " pm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON pm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE pm." + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Execute query to retrieve pending requests
            Cursor mPendingReqCursor = mDatabase.rawQuery(selectPendingReqQuery, null);
            if (mPendingReqCursor != null) {
                if (mPendingReqCursor.moveToFirst()) {
                    do {

                        // Prepare member object and add to the pendingReqList
                        MembersModel member = new MembersModel();
                        member.setUserId(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                        member.setName(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                        member.setNickname(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                        member.setUserName(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                        member.setAvatar(mPendingReqCursor.getInt(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                        member.setProfileImage(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                        member.setProfileImageL(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                        member.setProfileImageM(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                        member.setProfileImageS(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                        member.setPhone(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
                        member.setBio(mPendingReqCursor.getString(mPendingReqCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
                        pendingReqList.add(member);
                    } while (mPendingReqCursor.moveToNext());
                }
                mPendingReqCursor.close();
            }
        }

        // Prepare query for retrieving group details for loop
        String selectGroupQuery;
        if (chatId.equalsIgnoreCase("-101")) {
            // chatId = "-101" means loop is not created at, so retrieve group members based on localVideoPath
            selectGroupQuery = "SELECT * FROM " + DatabaseHelper.TABLE_LOOP_GROUP_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";
        } else {
            selectGroupQuery = "SELECT * FROM " + DatabaseHelper.TABLE_LOOP_GROUP_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";
        }

        // Execute query to retrieve group
        Cursor mGroupCursor = mDatabase.rawQuery(selectGroupQuery, null);
        if (mGroupCursor != null) {
            if (mGroupCursor.moveToFirst()) {

                // Prepare group object
                group.setName(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_NAME)));
                group.setDescription(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_DESC)));
                group.setGroupId(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_GROUP_ID)));
                group.setDp(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP)));
                group.setSmallDp(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_S)));
                group.setVideoURL(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH)));
                group.setColorCode(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_COLOR)));
                group.setTextColorCode(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_TEXT_COLOR)));
                group.setNoOfViews(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIEWS)));
                group.setNoOfVideos(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_VIDEOS)));
                group.setNoOfMembers(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_MEMBERS)));
                group.setNoOfSubscribers(mGroupCursor.getString(mGroupCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_SUBSCRIBERS)));
            }
            mGroupCursor.close();
        }

        // Add membersList to the group object
        group.setMembers(membersList);

        // Add subscribersList to the group object
        group.setSubscribers(subscribersList);

        // Add pendingReqList to the group object
        group.setRequests(pendingReqList);

        // return group object
        return group;
    }

    //TODO: Need to check
//    public List<QueueModel> getQueueList() {
//        List<QueueModel> queueList = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_QUEUE;
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                if (mCursor.moveToFirst()) {
//                    do {
//                        QueueModel queue = new QueueModel();
//                        queue.queue_type = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_QUEUE_TYPE));
//                        queue.queue_count = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_QUEUE_COUNT));
//                        queue.thumbnail_url = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL));
//                        queue.video_thumbnail_l = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L));
//                        queue.video_thumbnail_s = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S));
//                        queueList.add(queue);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return queueList;
//    }

    @SuppressLint("Range")
    public List<DiscoverModel> getSavedVideos() {
        List<DiscoverModel> savedVideoList = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_SAVED_VIDEO + " ORDER BY " + DatabaseHelper.COLUMN_SAVED_AT + " ASC";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        DiscoverModel savedVideo = new DiscoverModel();
                        savedVideo.setUserId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                        savedVideo.setVideoId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_ID)));
                        savedVideo.setVideoUrl(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_URL)));
                        savedVideo.setVideoThumbnail(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_THUMBNAIL)));
                        savedVideo.setVideoThumbnailLarge(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L)));
                        savedVideo.setVideoThumbnailSmall(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S)));
                        savedVideo.setDuration(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DURATION)));
                        savedVideo.setLink(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_LINK)));
                        savedVideo.setDescription(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));
                        savedVideo.setAspectRatio(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_ASPECT_RATIO)));
                        savedVideo.setResolution(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_RESOLUTION)));
                        savedVideo.setSize(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SIZE)));
                        savedVideo.setShareURL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SHARE_URL)));
                        savedVideo.setSavedAt(String.valueOf(mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SAVED_AT))));
                        savedVideo.setAvatar(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                        savedVideo.setProfileImage(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                        savedVideo.setProfileImageL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                        savedVideo.setProfileImageM(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                        savedVideo.setProfileImageS(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                        savedVideoList.add(savedVideo);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return savedVideoList;
    }

    public void convertReactionIntoReply(String chatId) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION, 2);
            values.put(DatabaseHelper.COLUMN_IS_REPLY_RECEIVED, 1);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);

            ContentValues groupValues = new ContentValues();
            groupValues.put(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION, 2);
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, groupValues, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public int getChatCountBasedOnChatId(String chatId) {
        int count = 0;

        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 3 AND " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 2";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                count = mCursor.getCount();
                mCursor.close();
            }

        } catch (IllegalStateException e) {
            count = 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return count;
    }

    public int getReactionVideoCountBasedOnChatId(String chatId) {
        int count = 0;

        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 3 AND " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 2 AND " + DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION + " = 1";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                count = mCursor.getCount();
                mCursor.close();
            }

        } catch (IllegalStateException e) {
            count = 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return count;
    }

    public List<ChatModel> getS3PendingChats() {
        List<ChatModel> pendingChats = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 1 AND (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 1)" + " AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        pendingChats.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return pendingChats;
    }

    public List<ChatModel> getCompressionPendingChats() {
        List<ChatModel> pendingChats = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 0 AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails(chat.getChatId(), chat.getVideoUrl()));
                        pendingChats.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return pendingChats;
    }

    public List<ChatModel> getS3PendingReactions() {
        List<ChatModel> pendingChats = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 1 AND (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_DP_UPLOAD_STATUS + " = 1)" + " AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '-101'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails("-101", chat.getVideoUrl()));
                        pendingChats.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return pendingChats;
    }

    public List<ChatModel> getCompressionPendingReactions() {
        List<ChatModel> pendingChats = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 0 AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '-101'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails("-101", chat.getVideoUrl()));
                        pendingChats.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return pendingChats;
    }

    public List<ChatModel> getS3PendingReactions2(String videoId) {
        List<ChatModel> pendingChants = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE "
                    + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 1 AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '-101' AND "
                    + DatabaseHelper.COLUMN_CONVERSATION_ID + " = '" + videoId + "'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails(chat.getChatId(), chat.getVideoUrl()));
                        pendingChants.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return pendingChants;
    }

    public List<ChatModel> getAPIPendingChats() {
        List<ChatModel> pendingChants = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 1 AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 2 AND " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 2 AND " + DatabaseHelper.COLUMN_CHAT_ID + " != '-101'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails(chat.getChatId(), chat.getVideoUrl()));
                        pendingChants.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return pendingChants;
    }

    public List<ChatModel> getAPIPendingReactions() {
        List<ChatModel> pendingChants = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CHAT_MASTER + " WHERE " + DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 1 AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 2 AND " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 2 AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '-101'";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        ChatModel chat = getChatObjectWithCursor(mCursor);
                        chat.setGroup(getGroupDetails("-101", chat.getVideoUrl()));
                        pendingChants.add(chat);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return pendingChants;
    }

    public void insertReactionOrReply(ChatModel chat, boolean isReaction) {
        try {
            mDatabase.beginTransaction();
            chat.setRetry(false);
            if (isReaction) {
                chat.setIsReplyOrReaction(1);
                chat.setFromStatus("1");
            } else {
                chat.setIsReplyOrReaction(2);
                chat.setFromStatus("");
            }
            List<ChatModel> chatList = new ArrayList<>();
            chatList.add(chat);
            insertChatList(chatList, chat.getChatId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will insert message in loop
    public void insertLoopVideo(MessageModel messageModel) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // add message object to list
            List<MessageModel> messageList = new ArrayList<>();
            messageList.add(messageModel);

            // insert/update message in message master
            insertORUpdateLoopVideoList(messageList, messageModel.getChatId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update messageAt in loop master by given chatId
    public void updateLatestMessageAt(String chatId) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            long latestTimeStamp = getLatestTimeStamp(chatId);

            // Prepare content value object to update last updated at
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_LATEST_MESSAGE_AT, latestTimeStamp);

            // Update last updated at in loop master by given chatId
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void insertDirectVideo(ChatModel chat) {
        try {
            mDatabase.beginTransaction();
            List<ChatModel> chatList = new ArrayList<>();
            chatList.add(chat);
            insertChatList(chatList, chat.getChatId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void insertReactions(String chatId, List<ChatModel> chatList) {
        try {
            mDatabase.beginTransaction();
            insertChatList(chatList, chatId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void insertReply(String chatId, List<ChatModel> chatList, int convType) {
        try {
            mDatabase.beginTransaction();
            for (int i = 0; i < chatList.size(); i++) {
                ChatModel chat = chatList.get(i);
                chat.setVideoUploadStatus(3);
                chat.setImageUploadStatus(2);
                chat.setDpUploadStatus(2);
                chat.setCompressionStatus(1);
                chat.setFfMpegCommand("");
                chat.setIsReplyReceived(1);
                chat.setRetry(false);
                chat.setIsReplyOrReaction(2);
                chat.setFromStatus("");
                chat.setConvType(convType);
                chat.setLocalVideoPath("");
                chat.setImagePath("");
                chat.setFirstVideoLocalPath("");
            }
            insertChatList(chatList, chatId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void insertReactionOrReply(ChatModel chat, boolean isReaction, String fromStatus) {
        try {
            mDatabase.beginTransaction();
            chat.setRetry(false);
            if (isReaction) {
                chat.setIsReplyOrReaction(1);
                chat.setFromStatus(fromStatus);
            } else {
                chat.setIsReplyOrReaction(2);
                chat.setFromStatus("");
            }
            List<ChatModel> chatList = new ArrayList<>();
            chatList.add(chat);
            insertChatList(chatList, chat.getChatId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will insert or update single loop in DB
    public void insertORUpdateLoop(LoopsModel loop) {

        // Check for messages are present in loop object or not
        if (loop.getLatestMessages() != null && loop.getLatestMessages().size() > 0) {
            try {
                // Initialize db transaction
                mDatabase.beginTransaction();

                // Insert or update loop in loop master
                insertORUpdateLoopMaster(loop);

                // Insert or update loop message in message master
                insertORUpdateLoopVideoList(loop.getLatestMessages(), loop.getChatId());

                if (loop.getGroup() != null) {
                    GroupModel group = loop.getGroup();

                    // Insert or update loop group members
                    insertLoopMembersWithoutDBTransition(loop.getChatId(), group, loop.getConvType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Close the db transaction
                if (isOpen()) {
                    mDatabase.setTransactionSuccessful();
                    mDatabase.endTransaction();
                }
            }
        }
    }

    // This function will delete all loops for given chatIds
    public void deleteLoops(ArrayList<String> chatIds) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Make comma separated string to pass in 'IN' query
            String commaSeparatedChatIds = TextUtils.join("','", chatIds);

            // Prepare where condition to delete loops from required tables
            String whereClause = DatabaseHelper.COLUMN_CHAT_ID + " IN ('" + commaSeparatedChatIds + "')";

            // Delete loops from every table
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP_MEMBERS, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_SUBSCRIBERS, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_PENDING_REQUESTS, whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This will delete loop by local video path
    public void deleteLoopByLocalPath(String localVideoPath) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare where condition to delete loop from required tables
            String whereClause = DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            // Delete loop from every table
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP_MEMBERS, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_SUBSCRIBERS, whereClause, null);
            mDatabase.delete(DatabaseHelper.TABLE_LOOP_PENDING_REQUESTS, whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This will delete particular loop video by local video path
    public void deleteLoopVideoByLocalPath(String localVideoPath) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare where condition to delete message from message master
            String whereClause = DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'";

            // Delete message from message master
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This will delete particular loop video by messageId
    public void deleteLoopVideoByMessageId(String messageId) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare where condition to delete message from message master
            String whereClause = DatabaseHelper.COLUMN_MESSAGE_ID + " = '" + messageId + "'";

            // Delete message from message master
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    private void insertChatList(List<ChatModel> chatList, String chatId) {
        for (int i = 0; i < chatList.size(); i++) {
            ChatModel chat = chatList.get(i);
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
            values.put(DatabaseHelper.COLUMN_THUMBNAIL, chat.getThumbnailUrl());
            values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L, chat.getVideoThumbnailLarge());
            values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S, chat.getVideoThumbnailSmall());
            values.put(DatabaseHelper.COLUMN_VIDEO_URL, chat.getVideoUrl());
            values.put(DatabaseHelper.COLUMN_VIDEO_URL_M3U8, chat.getVideoUrlM3U8());
            values.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, chat.getLocalVideoPath());
            values.put(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH, chat.getImagePath());
            values.put(DatabaseHelper.COLUMN_FIRST_VIDEO_LOCAL_PATH, chat.getFirstVideoLocalPath());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_ID, chat.getConversationId());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_AT, Long.parseLong(chat.getConversationAt()));
            values.put(DatabaseHelper.COLUMN_DURATION, chat.getDuration());
            values.put(DatabaseHelper.COLUMN_LINK, chat.getLink());
            values.put(DatabaseHelper.COLUMN_ASPECT_RATIO, chat.getAspectRatio());
            values.put(DatabaseHelper.COLUMN_RESOLUTION, chat.getResolution());
            values.put(DatabaseHelper.COLUMN_SIZE, chat.getSize());
            try {
                values.put(DatabaseHelper.COLUMN_IS_READ, chat.getRead() ? 1 : 0);
            } catch (Exception e) {
                values.put(DatabaseHelper.COLUMN_IS_READ, 0);
            }
            values.put(DatabaseHelper.COLUMN_IS_REPLY, chat.getReply() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, chat.getVideoUploadStatus());
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, chat.getImageUploadStatus());
            values.put(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS, chat.getDpUploadStatus());
            values.put(DatabaseHelper.COLUMN_IS_REPLY_RECEIVED, chat.getIsReplyReceived());
            values.put(DatabaseHelper.COLUMN_IS_RETRY, chat.isRetry() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_IS_FRONT, 0);
            values.put(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION, chat.getIsReplyOrReaction());
            values.put(DatabaseHelper.COLUMN_FROM_STATUS, chat.getFromStatus());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, chat.getConvType());
            values.put(DatabaseHelper.COLUMN_DESCRIPTION, chat.getDescription());
            values.put(DatabaseHelper.COLUMN_SHARE_URL, chat.getShareURL());
            values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, chat.getNoOfViews());
            values.put(DatabaseHelper.COLUMN_NO_OF_COMMENTS, chat.getNoOfComments());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_SHARE_URL, chat.getConvShareURL());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_NO_OF_VIEWS, chat.getConvNoOfViews());
            if (chat.getOwner() != null) {
                MembersModel member = chat.getOwner();
                values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
                values.put(DatabaseHelper.COLUMN_NAME, member.getName());
                values.put(DatabaseHelper.COLUMN_NICKNAME, member.getNickname());
                values.put(DatabaseHelper.COLUMN_IS_AVATAR, member.isAvatar() ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, member.getProfileImage());
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, member.getProfileImageS());
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, member.getProfileImageM());
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, member.getProfileImageL());
                values.put(DatabaseHelper.COLUMN_CONTACTS_BIO, member.getBio());
            }
            if (chat.getQuestions() != null && chat.getQuestions().size() > 0) {
                Type questionType = new TypeToken<List<QuestionModel>>() {
                }.getType();
                Gson gson = new Gson();
                String selectedQuestions = gson.toJson(chat.getQuestions(), questionType);
                values.put(DatabaseHelper.COLUMN_QUESTIONS, selectedQuestions);
            }
            if (chat.getMetaData() != null) {
                String metaData = new Gson().toJson(chat.getMetaData(), MetaDataModel.class);
                values.put(DatabaseHelper.COLUMN_META_DATA, metaData);
            }
            if (chat.getSettings() != null) {
                String settings = new Gson().toJson(chat.getSettings(), SettingsModel.class);
                values.put(DatabaseHelper.COLUMN_SETTINGS, settings);
            }
            if (chat.getRepostModel() != null) {
                String repostData = new Gson().toJson(chat.getRepostModel(), RepostModel.class);
                values.put(DatabaseHelper.COLUMN_REPOST, repostData);
            }
            values.put(DatabaseHelper.COLUMN_FFMPEG_COMMAND, chat.getFfMpegCommand());
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, chat.getCompressionStatus());
            mDatabase.insert(DatabaseHelper.TABLE_CHAT_MASTER, null, values);
        }
    }

    // This function will generate linkedHashMap by given listOfMessages
    public LinkedHashMap<String, MessageModel> generateMapFromList(List<MessageModel> messageList) {
        LinkedHashMap<String, MessageModel> messageMap = new LinkedHashMap<>();
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            ArrayList<String> messageIds = new ArrayList<>();

            for (int i = 0; i < messageList.size(); i++) {
                // Get the message object
                MessageModel message = messageList.get(i);
                messageIds.add(message.getMessageId());
            }

            // Make comma separated string to pass in 'IN' query
            String commaSeparatedMessageIds = TextUtils.join("','", messageIds);

            // Prepare query for retrieving messages for loop
            String selectMessageQuery = "SELECT * FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " mm LEFT JOIN " + DatabaseHelper.TABLE_USER_MASTER + " um ON mm." + DatabaseHelper.COLUMN_USER_ID + " = um." + DatabaseHelper.COLUMN_USER_ID + " WHERE mm." + DatabaseHelper.COLUMN_MESSAGE_ID + " IN ('" + commaSeparatedMessageIds + "') ORDER BY mm." + DatabaseHelper.COLUMN_MESSAGE_AT + " DESC";

            // Execute query to retrieve messages
            Cursor mMessageCursor = mDatabase.rawQuery(selectMessageQuery, null);
            if (mMessageCursor != null) {
                if (mMessageCursor.moveToFirst()) {
                    do {

                        // Retrieve message object from cursor
                        MessageModel message = getMessageObjectWithCursor(mMessageCursor);
                        messageMap.put(message.getMessageId(), message);
                    } while (mMessageCursor.moveToNext());
                }
                mMessageCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // return linkedHashMap
        return messageMap;
    }

    // This function will update no_of_views and no_of_comments in messages
    public void updateCountsForMessage(String chatId, List<MessageModel> messageList, boolean isNewVideos) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            for (int i = 0; i < messageList.size(); i++) {

                // Get the message object
                MessageModel message = messageList.get(i);

                // Prepare ContentValues object to update message in DB
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
                values.put(DatabaseHelper.COLUMN_MESSAGE_ID, message.getMessageId());
                values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, message.getNoOfViews());
                values.put(DatabaseHelper.COLUMN_NO_OF_COMMENTS, message.getNoOfComments());
                values.put(DatabaseHelper.COLUMN_MESSAGE_SUMMARY, message.getMessageSummary());
                if (!TextUtils.isEmpty(message.getMessageAt())) {
                    values.put(DatabaseHelper.COLUMN_MESSAGE_AT, message.getMessageAt());
                }
                values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 3);
                values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);
                values.put(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS, 2);
                values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);

                // Insert or update message object in DB
                long id = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_MESSAGE_MASTER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                if (id == -1) {
                    mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_MESSAGE_ID + " = '" + message.getMessageId() + "'", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will return oldest timestamp from available messages in loop
    @SuppressLint("Range")
    private long getOldestTimeStamp(String chatId) {
        long oldestTimeStamp = 0;
        try {

            // Prepare select query to get oldest timestamp
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_MESSAGE_AT +
                    " FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'" +
                    " ORDER BY " + DatabaseHelper.COLUMN_MESSAGE_AT + " LIMIT 1";

            // Execute query and get oldest timestamp
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    oldestTimeStamp = mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGE_AT));
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return oldest timestamp
        return oldestTimeStamp;
    }

    // This function will return latest timestamp from available messages in loop
    @SuppressLint("Range")
    private long getLatestTimeStamp(String chatId) {
        long latestTimeStamp = 0;
        try {

            // Prepare select query to get latest timestamp
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_MESSAGE_AT +
                    " FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'" +
                    " ORDER BY " + DatabaseHelper.COLUMN_MESSAGE_AT + " DESC LIMIT 1";

            // Execute query and get latest timestamp
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    latestTimeStamp = mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGE_AT));
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return latest timestamp
        return latestTimeStamp;
    }

    // This function will insert or update message list in message master(loop's videos)
    public void insertORUpdateLoopVideoListWithTransaction(List<MessageModel> messageList, String chatId) {
        try {
            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare and add missing parameters for message object
            for (int j = 0; j < messageList.size(); j++) {
                MessageModel message = messageList.get(j);
                message.setLocalVideoPath("");
                message.setLocalImagePath("");
                message.setVideoUploadStatus(3);
                message.setImageUploadStatus(2);
                message.setDpUploadStatus(2);
                message.setCompressionStatus(1);
                message.setFfMpegCommand("");
                message.setRetry(false);
                message.setConvType(VideoConvType.ROUND_TABLE.getValue());
            }

            // Insert or Update messages
            insertORUpdateLoopVideoList(messageList, chatId);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will insert or update message list in message master(loop's videos)
    private void insertORUpdateLoopVideoList(List<MessageModel> messageList, String chatId) {
        for (int i = 0; i < messageList.size(); i++) {

            // Get the message object
            MessageModel message = messageList.get(i);

            // Prepare ContentValues object to insert or update message in DB
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
            values.put(DatabaseHelper.COLUMN_MESSAGE_ID, message.getMessageId());
            values.put(DatabaseHelper.COLUMN_MESSAGE_AT, Long.parseLong(message.getMessageAt()));
            values.put(DatabaseHelper.COLUMN_THUMBNAIL, message.getThumbnailUrl());
            values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_L, message.getVideoThumbnailLarge());
            values.put(DatabaseHelper.COLUMN_VIDEO_THUMBNAIL_S, message.getVideoThumbnailSmall());

            // Check for null for local video path as if null is inserted in db than UNIQUE constraint will not work for DB
            if (!TextUtils.isEmpty(message.getLocalVideoPath())) {
                values.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, message.getLocalVideoPath());
            }
            values.put(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH, message.getLocalImagePath());
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, message.getVideoUploadStatus());
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, message.getImageUploadStatus());
            values.put(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS, message.getDpUploadStatus());
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, message.getCompressionStatus());
            values.put(DatabaseHelper.COLUMN_FFMPEG_COMMAND, message.getFfMpegCommand());
            values.put(DatabaseHelper.COLUMN_IS_RETRY, message.isRetry() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, message.getConvType());

            // Add owner of message
            if (message.getOwner() != null) {
                MembersModel member = message.getOwner();
                values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());

                // Insert or update member in user master
                insertORUpdateUser(member);
            }

            // Add question of message
            if (message.getQuestions() != null && message.getQuestions().size() > 0) {
                Type questionType = new TypeToken<List<QuestionModel>>() {
                }.getType();
                Gson gson = new Gson();
                String selectedQuestions = gson.toJson(message.getQuestions(), questionType);
                values.put(DatabaseHelper.COLUMN_QUESTIONS, selectedQuestions);
            }

            // Add meta data info of message
            if (message.getMetaData() != null) {
                String metaData = new Gson().toJson(message.getMetaData(), MetaDataModel.class);
                values.put(DatabaseHelper.COLUMN_META_DATA, metaData);
            }

            // Add repost model of message
            if (message.getRepostModel() != null) {
                String repostData = new Gson().toJson(message.getRepostModel(), RepostModel.class);
                values.put(DatabaseHelper.COLUMN_REPOST, repostData);
            }

            /*
                1) If we are syncing loops than below info will not be present in message object
                2) If we are syncing loop videos than below info will be present in message object
             */
            if (!TextUtils.isEmpty(message.getMediaUrl())) {
                values.put(DatabaseHelper.COLUMN_MEDIA_URL, message.getMediaUrl());
                values.put(DatabaseHelper.COLUMN_MEDIA_URL_M3U8, message.getMediaUrlM3U8());
                values.put(DatabaseHelper.COLUMN_LINK, message.getLink());
                try {
                    values.put(DatabaseHelper.COLUMN_IS_READ, message.isRead() ? 1 : 0);
                } catch (Exception e) {
                    values.put(DatabaseHelper.COLUMN_IS_READ, 0);
                }
                values.put(DatabaseHelper.COLUMN_MESSAGE_SUMMARY, message.getMessageSummary());
                values.put(DatabaseHelper.COLUMN_SHARE_URL, message.getShareURL());
                values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, message.getNoOfViews());
                values.put(DatabaseHelper.COLUMN_NO_OF_COMMENTS, message.getNoOfComments());
            }

            // Insert or update message object in DB
            long id = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_MESSAGE_MASTER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_MESSAGE_ID + " = '" + message.getMessageId() + "'", null);
            }
        }
    }

    public void insertComment(CommentModel comment) {
        try {
            mDatabase.beginTransaction();
            List<CommentModel> commentList = new ArrayList<>();
            commentList.add(comment);
            insertCommentsList(commentList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateComment(CommentModel comment, String localFilePath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_COMMENT_ID, comment.getCommentId());
            values.put(DatabaseHelper.COLUMN_THUMBNAIL, comment.getThumbnail());
            values.put(DatabaseHelper.COLUMN_FILE_URL, comment.getFileURL());
            values.put(DatabaseHelper.COLUMN_CREATED_AT, Long.parseLong(comment.getCreatedAt()));
            values.put(DatabaseHelper.COLUMN_FILE_UPLOAD_STATUS, 2);
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);
            values.put(DatabaseHelper.COLUMN_API_STATUS, 2);
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);
            mDatabase.update(DatabaseHelper.TABLE_COMMENTS, values, DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    private void insertCommentsList(List<CommentModel> commentList) {
        for (int i = 0; i < commentList.size(); i++) {
            CommentModel comment = commentList.get(i);
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAT_ID, comment.getChatId());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_ID, comment.getVideoId());
            values.put(DatabaseHelper.COLUMN_COMMENT_ID, comment.getCommentId());
            values.put(DatabaseHelper.COLUMN_THUMBNAIL, comment.getThumbnail());
            values.put(DatabaseHelper.COLUMN_FILE_URL, comment.getFileURL());
            values.put(DatabaseHelper.COLUMN_FILE_LOCAL_PATH, comment.getFileLocalVideoPath());
            values.put(DatabaseHelper.COLUMN_IMAGE_LOCAL_PATH, comment.getImageLocalVideoPath());
            values.put(DatabaseHelper.COLUMN_CREATED_AT, Long.parseLong(comment.getCreatedAt()));
            values.put(DatabaseHelper.COLUMN_DURATION, comment.getDuration());
            values.put(DatabaseHelper.COLUMN_LINK, comment.getLink());
            try {
                values.put(DatabaseHelper.COLUMN_IS_READ, comment.isRead() ? 1 : 0);
            } catch (Exception e) {
                values.put(DatabaseHelper.COLUMN_IS_READ, 0);
            }
            values.put(DatabaseHelper.COLUMN_FILE_UPLOAD_STATUS, comment.getFileUploadStatus());
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, comment.getImageUploadStatus());
            values.put(DatabaseHelper.COLUMN_API_STATUS, comment.getApiStatus());
            values.put(DatabaseHelper.COLUMN_IS_RETRY, comment.isRetry() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_SHARE_URL, comment.getShareURL());
            values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, comment.getNoOfViews());
            values.put(DatabaseHelper.COLUMN_FILE_TYPE, comment.getFileType());
            if (comment.getOwner() != null) {
                MembersModel member = comment.getOwner();
                values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
                values.put(DatabaseHelper.COLUMN_NAME, member.getName());
                values.put(DatabaseHelper.COLUMN_NICKNAME, member.getNickname());
                values.put(DatabaseHelper.COLUMN_IS_AVATAR, member.isAvatar() ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, member.getProfileImage());
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, member.getProfileImageS());
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, member.getProfileImageM());
                values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, member.getProfileImageL());
                values.put(DatabaseHelper.COLUMN_CONTACTS_BIO, member.getBio());
            }
            if (comment.getQuestions() != null && comment.getQuestions().size() > 0) {
                Type questionType = new TypeToken<List<QuestionModel>>() {
                }.getType();
                Gson gson = new Gson();
                String selectedQuestions = gson.toJson(comment.getQuestions(), questionType);
                values.put(DatabaseHelper.COLUMN_QUESTIONS, selectedQuestions);
            }
            if (comment.getMetaData() != null) {
                String metaData = new Gson().toJson(comment.getMetaData(), MetaDataModel.class);
                values.put(DatabaseHelper.COLUMN_META_DATA, metaData);
            }
            values.put(DatabaseHelper.COLUMN_FFMPEG_COMMAND, comment.getFfMpegCommand());
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, comment.getCompressionStatus());
            if (!TextUtils.isEmpty(comment.getCommentText())) {
                values.put(DatabaseHelper.COLUMN_COMMENT_TEXT, comment.getCommentText());
            }
            if (!TextUtils.isEmpty(comment.getCommentData())) {
                values.put(DatabaseHelper.COLUMN_COMMENT_DATA, comment.getCommentData());
            }
            mDatabase.insert(DatabaseHelper.TABLE_COMMENTS, null, values);
        }
    }

    @SuppressLint("Range")
    public void updateMembers(String chatId, GroupModel group) {
        try {
            mDatabase.beginTransaction();
            int convType = 0;
            long timeStamp = System.currentTimeMillis();
            boolean isReaction = false;
            String selectMemberQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATION_MEMBERS + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' ORDER BY " + DatabaseHelper.COLUMN_CONVERSATION_AT + " DESC LIMIT 1";
            Cursor mMemberCursor = mDatabase.rawQuery(selectMemberQuery, null);
            if (mMemberCursor != null) {
                if (mMemberCursor.moveToFirst()) {
                    do {
                        convType = mMemberCursor.getInt(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_TYPE));
                        timeStamp = mMemberCursor.getInt(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_AT));
                        isReaction = mMemberCursor.getInt(mMemberCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION)) == 1;
                    } while (mMemberCursor.moveToNext());
                }
                mMemberCursor.close();
            }

            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_SUBSCRIBERS, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_PENDING_REQUESTS, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            insertMembersWithoutDBTransition(chatId, group, timeStamp, convType, isReaction ? 1 : 2);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updatePrivacySettingsInChat(String chatId, SettingsModel settingsModel) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            String settings = new Gson().toJson(settingsModel, SettingsModel.class);
            values.put(DatabaseHelper.COLUMN_SETTINGS, settings);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void insertMembersWithoutDBTransition(String chatId, GroupModel group, long timeStamp, int convType, int isReplyOrReaction) {
        try {
            ContentValues groupValues = new ContentValues();
            groupValues.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
            groupValues.put(DatabaseHelper.COLUMN_GROUP_NAME, group.getName());
            groupValues.put(DatabaseHelper.COLUMN_GROUP_ID, group.getGroupId());
            groupValues.put(DatabaseHelper.COLUMN_VIDEO_URL, group.getVideoURL());
            groupValues.put(DatabaseHelper.COLUMN_GROUP_DESC, group.getDescription());
            groupValues.put(DatabaseHelper.COLUMN_DP, group.getDp());
            groupValues.put(DatabaseHelper.COLUMN_DP_S, group.getSmallDp());
            groupValues.put(DatabaseHelper.COLUMN_DP_COLOR, group.getColorCode());
            groupValues.put(DatabaseHelper.COLUMN_DP_TEXT_COLOR, group.getTextColorCode());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, group.getNoOfViews());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_VIDEOS, group.getNoOfVideos());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_MEMBERS, group.getNoOfMembers());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_SUBSCRIBERS, group.getNoOfSubscribers());
            groupValues.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, convType);
            groupValues.put(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION, isReplyOrReaction);
            mDatabase.insert(DatabaseHelper.TABLE_GROUP, null, groupValues);
            List<MembersModel> membersList = group.getMembers();
            if (membersList != null && membersList.size() > 0) {
                for (int k = 0; k < membersList.size(); k++) {
                    MembersModel member = membersList.get(k);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
                    values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
                    values.put(DatabaseHelper.COLUMN_NAME, member.getName());
                    values.put(DatabaseHelper.COLUMN_NICKNAME, member.getNickname());
                    values.put(DatabaseHelper.COLUMN_IS_AVATAR, member.isAvatar() ? 1 : 0);
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, member.getProfileImage());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, member.getProfileImageS());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, member.getProfileImageM());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, member.getProfileImageL());
                    values.put(DatabaseHelper.COLUMN_MEMBER_ROLE, member.getMemberRole());
                    values.put(DatabaseHelper.COLUMN_MEMBER_STATUS, member.getMemberStatus());
                    values.put(DatabaseHelper.COLUMN_CONTACTS_PHONE, member.getPhone());
                    values.put(DatabaseHelper.COLUMN_CONTACTS_BIO, member.getBio());
                    values.put(DatabaseHelper.COLUMN_IS_REPLY_OR_REACTION, isReplyOrReaction);
                    values.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, convType);
                    values.put(DatabaseHelper.COLUMN_CONVERSATION_AT, timeStamp);
                    values.put(DatabaseHelper.COLUMN_VIDEO_URL, member.getVideoURL());
                    mDatabase.insert(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, null, values);
                }
            }

            List<MembersModel> subscribersList = group.getSubscribers();
            if (subscribersList != null && subscribersList.size() > 0) {
                for (int k = 0; k < subscribersList.size(); k++) {
                    MembersModel member = subscribersList.get(k);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
                    values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
                    values.put(DatabaseHelper.COLUMN_NAME, member.getName());
                    values.put(DatabaseHelper.COLUMN_NICKNAME, member.getNickname());
                    values.put(DatabaseHelper.COLUMN_IS_AVATAR, member.isAvatar() ? 1 : 0);
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, member.getProfileImage());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, member.getProfileImageS());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, member.getProfileImageM());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, member.getProfileImageL());
                    values.put(DatabaseHelper.COLUMN_CONTACTS_BIO, member.getBio());
                    values.put(DatabaseHelper.COLUMN_CONTACTS_PHONE, member.getPhone());
                    mDatabase.insert(DatabaseHelper.TABLE_SUBSCRIBERS, null, values);
                }
            }

            List<MembersModel> pendingReqList = group.getRequests();
            if (pendingReqList != null && pendingReqList.size() > 0) {
                for (int l = 0; l < pendingReqList.size(); l++) {
                    MembersModel member = pendingReqList.get(l);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
                    values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
                    values.put(DatabaseHelper.COLUMN_NAME, member.getName());
                    values.put(DatabaseHelper.COLUMN_NICKNAME, member.getNickname());
                    values.put(DatabaseHelper.COLUMN_IS_AVATAR, member.isAvatar() ? 1 : 0);
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, member.getProfileImage());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, member.getProfileImageS());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, member.getProfileImageM());
                    values.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, member.getProfileImageL());
                    values.put(DatabaseHelper.COLUMN_CONTACTS_BIO, member.getBio());
                    values.put(DatabaseHelper.COLUMN_CONTACTS_PHONE, member.getPhone());
                    mDatabase.insert(DatabaseHelper.TABLE_PENDING_REQUESTS, null, values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This function will insert or update group member by chatId
    private void insertORUpdateGroupMember(String chatId, MembersModel member, int convType) {

        // Prepare contentValues object for group members
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);

        // In case of non-genuin user we will add phone number as userId
        if (!TextUtils.isEmpty(member.getUserId())) {
            values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
        } else {
            values.put(DatabaseHelper.COLUMN_USER_ID, member.getPhone());
        }
        values.put(DatabaseHelper.COLUMN_MEMBER_ROLE, member.getMemberRole());
        values.put(DatabaseHelper.COLUMN_MEMBER_STATUS, member.getMemberStatus());

        // Check for null for local video path as if null is inserted in db than UNIQUE constraint will not work for DB
        if (!TextUtils.isEmpty(member.getVideoURL())) {
            values.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, member.getVideoURL());
        }
        values.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, convType);

        // Insert or update group member in DB
        long mID = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_GROUP_MEMBERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (mID == -1) {
            mDatabase.update(DatabaseHelper.TABLE_GROUP_MEMBERS, values, DatabaseHelper.COLUMN_USER_ID + " = '" + member.getUserId() + "' AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
        }

        // Insert or update user details in user master for member
        insertORUpdateUser(member);
    }

    // This function will insert/update group, group members, subscribers, pending request
    public void insertLoopMembersWithoutDBTransition(String chatId, GroupModel group, int convType) {
        try {

            // Prepare contentValues object for group
            ContentValues groupValues = new ContentValues();
            groupValues.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
            groupValues.put(DatabaseHelper.COLUMN_GROUP_NAME, group.getName());
            groupValues.put(DatabaseHelper.COLUMN_GROUP_ID, group.getGroupId());

            // Check for null for local video path as if null is inserted in db than UNIQUE constraint will not work for DB
            if (!TextUtils.isEmpty(group.getVideoURL())) {
                groupValues.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, group.getVideoURL());
            }
            groupValues.put(DatabaseHelper.COLUMN_GROUP_DESC, group.getDescription());
            groupValues.put(DatabaseHelper.COLUMN_DP, group.getDp());
            groupValues.put(DatabaseHelper.COLUMN_DP_S, group.getSmallDp());
            groupValues.put(DatabaseHelper.COLUMN_DP_COLOR, group.getColorCode());
            groupValues.put(DatabaseHelper.COLUMN_DP_TEXT_COLOR, group.getTextColorCode());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, group.getNoOfViews());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_VIDEOS, group.getNoOfVideos());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_MEMBERS, group.getNoOfMembers());
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_SUBSCRIBERS, group.getNoOfSubscribers());
            groupValues.put(DatabaseHelper.COLUMN_CONVERSATION_TYPE, convType);

            // Insert or update group object in DB
            long id = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, null, groupValues, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                mDatabase.update(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, groupValues, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
            }

            // Get group member list from group object
            List<MembersModel> membersList = group.getMembers();

            if (membersList != null && membersList.size() > 0) {
                for (int k = 0; k < membersList.size(); k++) {
                    MembersModel member = membersList.get(k);

                    // Insert or update member in group
                    insertORUpdateGroupMember(chatId, member, convType);
                }
            }

            // Get subscribers list from group object
            List<MembersModel> subscribersList = group.getSubscribers();

            if (subscribersList != null && subscribersList.size() > 0) {

                for (int k = 0; k < subscribersList.size(); k++) {
                    MembersModel member = subscribersList.get(k);

                    // Prepare contentValues object for subscribers
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
                    values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());

                    // Insert or update loop subscribers in DB
                    long mID = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_LOOP_SUBSCRIBERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    if (mID == -1) {
                        mDatabase.update(DatabaseHelper.TABLE_LOOP_SUBSCRIBERS, values, DatabaseHelper.COLUMN_USER_ID + " = '" + member.getUserId() + "' AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
                    }

                    // Insert or update user details in user master for member
                    insertORUpdateUser(member);
                }
            }

            // Get pending requests list from group object
            List<MembersModel> pendingReqList = group.getRequests();

            if (pendingReqList != null && pendingReqList.size() > 0) {
                for (int l = 0; l < pendingReqList.size(); l++) {
                    MembersModel member = pendingReqList.get(l);

                    // Prepare contentValues object for pending requests
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
                    values.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());

                    // Insert or update loop's pending requests in DB
                    long mID = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_LOOP_PENDING_REQUESTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    if (mID == -1) {
                        mDatabase.update(DatabaseHelper.TABLE_LOOP_PENDING_REQUESTS, values, DatabaseHelper.COLUMN_USER_ID + " = '" + member.getUserId() + "' AND " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
                    }

                    // Insert or update user details in user master for member
                    insertORUpdateUser(member);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This function will insert/update user in DB
    private void insertORUpdateUser(MembersModel member) {

        // Prepare contentValues object for user
        ContentValues userValues = new ContentValues();

        // In case of non-genuin user we will add phone number as userId
        if (!TextUtils.isEmpty(member.getUserId())) {
            userValues.put(DatabaseHelper.COLUMN_USER_ID, member.getUserId());
        } else {
            userValues.put(DatabaseHelper.COLUMN_CONTACTS_PHONE, member.getPhone());
        }
        userValues.put(DatabaseHelper.COLUMN_NICKNAME, TextUtils.isEmpty(member.getNickname()) ? member.getUserName() : member.getNickname());
        userValues.put(DatabaseHelper.COLUMN_CONTACTS_PHONE, (TextUtils.isEmpty(member.getNickname()) && TextUtils.isEmpty(member.getUserName())) ? member.getPhone() : "");

        /*
            1) If profile_image is empty we will not get all values in the object so not including in object
            2) If profile_image is not empty than we will all values in object
         */
        if (!TextUtils.isEmpty(member.getProfileImage())) {
            userValues.put(DatabaseHelper.COLUMN_NAME, member.getName());
            userValues.put(DatabaseHelper.COLUMN_IS_AVATAR, member.isAvatar() ? 1 : 0);
            userValues.put(DatabaseHelper.COLUMN_PROFILE_IMAGE, member.getProfileImage());
            userValues.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_S, member.getProfileImageS());
            userValues.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_M, member.getProfileImageM());
            userValues.put(DatabaseHelper.COLUMN_PROFILE_IMAGE_L, member.getProfileImageL());
            userValues.put(DatabaseHelper.COLUMN_CONTACTS_BIO, member.getBio());
        }

        // Insert or update user in DB
        long id = mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_USER_MASTER, null, userValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            mDatabase.update(DatabaseHelper.TABLE_USER_MASTER, userValues, DatabaseHelper.COLUMN_USER_ID + " = '" + member.getUserId() + "'", null);
        }
    }

    public void insertGroupAndMembers(String chatId, GroupModel group, long timeStamp, int convType, int isReplyOrReaction) {
        try {
            mDatabase.beginTransaction();
            insertMembersWithoutDBTransition(chatId, group, timeStamp, convType, isReplyOrReaction);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public int checkVideoUrlExists(String toVideoUrl) {
        int count = 0;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_VIDEO_URL +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " ='-101'" + " AND " + DatabaseHelper.COLUMN_VIDEO_URL +
                    "='" + toVideoUrl +
                    "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                count = mCursor.getCount();
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return count;
    }

    public int checkChatIDExists(String chatID) {
        int count = 0;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_CHAT_ID +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " ='" + chatID + "'" + " AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS +
                    " = 2";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                count = mCursor.getCount();
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return count;
    }

    public boolean isVideoUploadedFromCurrentDevice(String localPath, String cachedPath) {
        boolean isFound = false;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_CHAT_ID +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " ='" + localPath + "'" + " OR " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH +
                    " = '" + cachedPath + "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                isFound = mCursor.getCount() > 0;
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return isFound;
    }

    public void updateReply(String chatId, ChatModel chat, String localPath, String cachedPath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
            values.put(DatabaseHelper.COLUMN_THUMBNAIL, chat.getThumbnailUrl());
            values.put(DatabaseHelper.COLUMN_VIDEO_URL, chat.getVideoUrl());
            values.put(DatabaseHelper.COLUMN_VIDEO_URL_M3U8, chat.getVideoUrlM3U8());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_AT, Long.parseLong(chat.getConversationAt()));
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 3);
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);
            values.put(DatabaseHelper.COLUMN_SHARE_URL, chat.getShareURL());
            values.put(DatabaseHelper.COLUMN_NO_OF_VIEWS, chat.getNoOfViews());
            values.put(DatabaseHelper.COLUMN_NO_OF_COMMENTS, chat.getNoOfComments());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_ID, chat.getConversationId());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_SHARE_URL, chat.getConvShareURL());
            values.put(DatabaseHelper.COLUMN_CONVERSATION_NO_OF_VIEWS, chat.getConvNoOfViews());
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'" + " AND (" +
                    DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " ='" + localPath + "'" + " OR " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH +
                    " = '" + cachedPath + "')", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update related iDs after creating loop
    public void updateLoopRelatedIdsByLocalPath(LoopsModel loopsModel, String localPath, String cachedPath) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update chatId in loop master
            ContentValues loopValues = new ContentValues();
            loopValues.put(DatabaseHelper.COLUMN_CHAT_ID, loopsModel.getChatId());
            loopValues.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, "");

            // Update chatId in loop master by given localVideoPath
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, loopValues,
                    DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " ='" + localPath + "'" + " OR " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH +
                            " = '" + cachedPath + "'", null);

            if (loopsModel.getLatestMessages() != null && loopsModel.getLatestMessages().size() > 0) {

                // Get the first message object of loop
                MessageModel messageModel = loopsModel.getLatestMessages().get(0);

                // Prepare content values object to update messageId in message master
                ContentValues messageValues = new ContentValues();
                messageValues.put(DatabaseHelper.COLUMN_CHAT_ID, loopsModel.getChatId());
                messageValues.put(DatabaseHelper.COLUMN_MESSAGE_ID, messageModel.getMessageId());
                messageValues.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, "");
                messageValues.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 3);

                // Update messageId in message master by given localVideoPath
                mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, messageValues,
                        DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " ='" + localPath + "'" + " OR " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH +
                                " = '" + cachedPath + "'", null);
            }

            // Check for group object is null or not
            if (loopsModel.getGroup() != null) {

                // Prepare content values object to update groupId, chatId in group master
                ContentValues groupValues = new ContentValues();
                groupValues.put(DatabaseHelper.COLUMN_GROUP_ID, loopsModel.getGroup().getGroupId());
                groupValues.put(DatabaseHelper.COLUMN_CHAT_ID, loopsModel.getChatId());
                groupValues.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, "");

                // Update groupId, chatId in group master by given localVideoPath
                mDatabase.update(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, loopValues,
                        DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " ='" + localPath + "'" + " OR " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH +
                                " = '" + cachedPath + "'", null);

                // Check for member list is empty or not.
                if (loopsModel.getGroup().getMembers() != null && loopsModel.getGroup().getMembers().size() > 0) {
                    List<MembersModel> membersModelList = loopsModel.getGroup().getMembers();
                    for (int j = 0; j < membersModelList.size(); j++) {

                        // Get the member object
                        MembersModel membersModel = membersModelList.get(j);

                        // Prepare content values object to update userId, chatId in group members
                        ContentValues memberValues = new ContentValues();
                        memberValues.put(DatabaseHelper.COLUMN_USER_ID, membersModel.getUserId());

                        // Update userId in user master in case of non-genuin user
                        mDatabase.update(DatabaseHelper.TABLE_USER_MASTER, memberValues, DatabaseHelper.COLUMN_USER_ID + " = '" + membersModel.getPhone() + "'", null);
                        memberValues.put(DatabaseHelper.COLUMN_CHAT_ID, loopsModel.getChatId());
                        memberValues.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, "");

                        // Update userId, chatId in group members by given localVideoPath
                        mDatabase.update(DatabaseHelper.TABLE_GROUP_MEMBERS, memberValues, "(" + DatabaseHelper.COLUMN_USER_ID + " = '" + membersModel.getUserId() + "' OR " + DatabaseHelper.COLUMN_USER_ID + " = '" + membersModel.getPhone() + "') AND (" +
                                DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " ='" + localPath + "'" + " OR " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH +
                                " = '" + cachedPath + "')", null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update messageId in message by given localVideoPath
    public void updateMessageIdByLocalPath(String messageId, String localPath, String cachedPath) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update messageId in message master
            ContentValues messageValues = new ContentValues();
            messageValues.put(DatabaseHelper.COLUMN_MESSAGE_ID, messageId);
            messageValues.put(DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH, "");
            messageValues.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 3);

            // Update messageId in message master by given localVideoPath
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, messageValues,
                    DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " ='" + localPath + "'" + " OR " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH +
                            " = '" + cachedPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateReactionOrReplyVideoStatus(String localPath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 2);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update loop video status by given localVideoPath
    public void updateLoopVideoStatus(String localPath) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update videoUploadStatus
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 2);

            // Update videoUploadStatus in message master by given localVideoPath
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void deleteReplyVideo(String localPath) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will delete all the message from DB which are failed to upload
    public void deleteAllFailedReplyMessages(String chatId) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Delete all failed to upload message by given chatId
            mDatabase.delete(DatabaseHelper.TABLE_MESSAGE_MASTER, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "' AND " + DatabaseHelper.COLUMN_IS_RETRY + " = 1", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void deletePublicVideo(String localPath) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_PUBLIC_VIDEO, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " =  '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void deleteCommentVideo(String localPath) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_COMMENTS, DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " =  '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    //This function is used to update the compression status of chat video to Done for given localVideoPath
    public void updateChatCompressionStatus(String localVideoPath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 0 AND "
                    + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update compression status of loop message by given localVideoPath
    public void updateLoopVideoCompressionStatus(String localVideoPath) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update compression status
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);

            // Update compressionStatus in message master by given localVideoPath
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 0 AND "
                    + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    //This function is used to update the chat retry status for given localVideoPath
    public void updateChatRetryStatus(String localVideoPath, boolean isRetry) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, isRetry ? 1 : 0);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 0 AND "
                    + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update retry status of loop message by given localVideoPath
    public void updateLoopVideoRetryStatus(String localVideoPath, boolean isRetry) {
        try {

            // Close the db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update retry status
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, isRetry ? 1 : 0);

            // Update retryStatus in message master by given localVideoPath
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_COMPRESSION_STATUS + " = 0 AND "
                    + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localVideoPath + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    @SuppressLint("Range")
    public int checkVideoStatus(String localPath) {
        int videoStatus = -1;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        videoStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return videoStatus;
    }

    @SuppressLint("Range")
    public int checkDpStatus(String localPath) {
        int dpStatus = -1;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        dpStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return dpStatus;
    }

    // This function will return loop video status by given localVideoPath
    @SuppressLint("Range")
    public int checkLoopVideoStatus(String localPath) {
        int videoStatus = -1;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query to retrieve loop video status from message master
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'";

            // Execute query to retrieve loop video status
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {

                    // get the video status
                    videoStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS));
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        return videoStatus;
    }

    // This function will return loop dp status by given localVideoPath
    @SuppressLint("Range")
    public int checkLoopDpStatus(String localPath) {
        int dpStatus = -1;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query to retrieve loop dp status from message master
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'";

            // Execute query to retrieve loop dp status
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {

                    // Get the dp status
                    dpStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS));
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return dpStatus;
    }

    @SuppressLint("Range")
    public int getConvType(String localPath) {
        int convType = -1;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        convType = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_TYPE));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return convType;
    }

    @SuppressLint("Range")
    public int checkImageStatus(String localPath) {
        int imageStatus = -1;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        imageStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return imageStatus;
    }

    // This function will return loop image status by given localVideoPath
    @SuppressLint("Range")
    public int checkLoopImageStatus(String localPath) {
        Utility.showLog("Monali", "checkLoopImageStatus");
        int imageStatus = -1;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query to retrieve loop image status from message master
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_MESSAGE_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'";

            // Execute query to retrieve loop image status
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {

                    // Get the image status
                    imageStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS));
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return imageStatus;
    }

    @SuppressLint("Range")
    public int checkCommentFileStatus(String localFilePath) {
        int fileStatus = -1;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_COMMENTS +
                    " WHERE " + DatabaseHelper.COLUMN_COMMENT_ID + " = '-101'" +
                    " AND " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        fileStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_UPLOAD_STATUS));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return fileStatus;
    }

    @SuppressLint("Range")
    public int checkCommentImageStatus(String localFilePath) {
        int imageStatus = -1;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT *" +
                    " FROM " + DatabaseHelper.TABLE_COMMENTS +
                    " WHERE " + DatabaseHelper.COLUMN_COMMENT_ID + " = '-101'" +
                    " AND " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'";

            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        imageStatus = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return imageStatus;
    }

    public void updateCommentFileStatus(String localFilePath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_FILE_UPLOAD_STATUS, 2);
            mDatabase.update(DatabaseHelper.TABLE_COMMENTS, values, DatabaseHelper.COLUMN_COMMENT_ID +
                    " = '-101' AND " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateCommentImageStatus(String localFilePath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);
            mDatabase.update(DatabaseHelper.TABLE_COMMENTS, values, DatabaseHelper.COLUMN_COMMENT_ID +
                    " = '-101' AND " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateCompressionStatus(String localFilePath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);
            mDatabase.update(DatabaseHelper.TABLE_COMMENTS, values, DatabaseHelper.COLUMN_COMMENT_ID +
                    " = '-101' AND " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void deleteCommentByPath(String localFilePath) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_COMMENTS, DatabaseHelper.COLUMN_COMMENT_ID +
                    " = '-101' AND " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateCommentRetryStatus(String localFilePath, boolean isRetry) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, isRetry ? 1 : 0);
            mDatabase.update(DatabaseHelper.TABLE_COMMENTS, values, DatabaseHelper.COLUMN_COMMENT_ID +
                    " = '-101' AND " + DatabaseHelper.COLUMN_FILE_LOCAL_PATH + " = '" + localFilePath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateCommentCount(String videoId, String noOfComments) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NO_OF_COMMENTS, noOfComments);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_CONVERSATION_ID +
                    " = '" + videoId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    @SuppressLint("Range")
    public String getCommentCount(String videoId) {
        String commentCount = "";
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_NO_OF_COMMENTS +
                    " FROM " + DatabaseHelper.TABLE_CHAT_MASTER +
                    " WHERE " + DatabaseHelper.COLUMN_CONVERSATION_ID + " = '" + videoId + "'";
            Utility.showLog("Query", selectQuery);
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        commentCount = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NO_OF_COMMENTS));
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return commentCount;
    }

    public void updateReactionOrReplyImageStatus(String localPath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateReactionOrReplyDPStatus(String localPath) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS, 2);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update the loop message image status by given localVideoPath
    public void updateLoopImageStatus(String localPath) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query to update loop image status in message master
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);

            // Update loop image status in message status by given localVideoPath
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update the loop message dp status by given localVideoPath
    public void updateLoopDPStatus(String localPath) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query to update loop dp status in message master
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_DP_UPLOAD_STATUS, 2);

            // Update loop dp status in message status by given localVideoPath
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateRetryStatus(String localPath, boolean isRetry) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, isRetry ? 1 : 0);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This will update retry status for particular loop video in DB by local path
    public void updateRetryStatusForLoopVideo(String localPath, boolean isRetry) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content value object to update is_retry
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, isRetry ? 1 : 0);

            // Execute update query
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_VIDEO_LOCAL_PATH + " = '" + localPath + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }


    // This will change retry status to false for loop messages for the given ChatId
    public void updateAllMessagesRetryStatus(String chatId) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update retry status
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);

            // Update retryStatus in message master by given chatId
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateRetryStatus() {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update all the message's retry status to false
    public void updateLoopRetryStatus() {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update all the message's retry status to false
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);

            // Update retry status of all the messages to false
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateProfileRetryStatus() {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);
            mDatabase.update(DatabaseHelper.TABLE_PUBLIC_VIDEO, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateCommentRetryStatus() {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_RETRY, 0);
            mDatabase.update(DatabaseHelper.TABLE_COMMENTS, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    public void updateReadStatus(String conversationId) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_READ, 1);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_CONVERSATION_ID + " = '" + conversationId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    /* This function will update the read status of all messages
       in message master and update unread count of loop in
       loop master
     */
    @SuppressLint("Range")
    public void updateAllMessageReadStatus(String chatId) {
        int unreadMessageCount = 0;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update read status
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_READ, 1);

            // Update read status for all messages by given chatId
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);

            // Prepare content values object to update unread message count to zero
            ContentValues loopValues = new ContentValues();
            loopValues.put(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT, 0);

            // Update unread count in loop master by given chatId
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, loopValues, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }


    /* This function will update the read status of message
       in message master and update unread count of loop in
       loop master
     */
    @SuppressLint("Range")
    public boolean updateMessageReadStatus(String messageId, String chatId) {
        int unreadMessageCount = 0;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare content values object to update read status
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_READ, 1);

            // Update read status in message master by given messageId
            mDatabase.update(DatabaseHelper.TABLE_MESSAGE_MASTER, values, DatabaseHelper.COLUMN_MESSAGE_ID + " = '" + messageId + "'", null);

            // Prepare query for retrieving compression failed loops from loop master
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT + " FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Execute query to retrieve unread count for loop by given chatId
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    unreadMessageCount = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT));
                }
                mCursor.close();
            }

            // If unread count is greater than 0 than we need to update
            if (unreadMessageCount > 0) {

                // We need to minus unread count as we are marking message as read
                unreadMessageCount -= 1;

                // Prepare content values object to update unread message count
                ContentValues loopValues = new ContentValues();
                loopValues.put(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT, unreadMessageCount);

                // Update unread count in loop master by given chatId
                mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, loopValues, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // return this values so we can know if we need to update UI or not
        return unreadMessageCount == 0;
    }

    // This function will update unread count when we receive a new message in loop.
    @SuppressLint("Range")
    public boolean updateUnReadMessageCount(String chatId) {
        int unreadMessageCount = 0;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving unread count for given loop
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT + " FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Execute query to retrieve unread count in loop master by given chatId
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    unreadMessageCount = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT));
                }
                mCursor.close();
            }

            // We need to plus the unread count as we have received a new video in loop
            unreadMessageCount += 1;

            // Prepare the content values object to update unread message count
            ContentValues loopValues = new ContentValues();

            // Update unread count in loop master by given chatId
            loopValues.put(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT, unreadMessageCount);
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, loopValues, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // return this values so we can know if we need to update UI or not
        return unreadMessageCount == 1;
    }

    // This function will retrieve unread count of loop.
    @SuppressLint("Range")
    public int getUnReadMessageCount(String chatId) {
        int unreadMessageCount = 0;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving unread count for given loop
            String selectQuery = "SELECT " + DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT + " FROM " + DatabaseHelper.TABLE_CONVERSATIONS_MASTER + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Execute query to retrieve unread count in loop master by given chatId
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    unreadMessageCount = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT));
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // return unread count
        return unreadMessageCount;
    }

    // This function will set unread count of loop.
    @SuppressLint("Range")
    public void setUnReadMessageCount(String chatId, int unreadMessageCount) {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare the content values object to update unread message count
            ContentValues loopValues = new ContentValues();

            // Update unread count in loop master by given chatId
            loopValues.put(DatabaseHelper.COLUMN_UNREAD_MESSAGE_COUNT, unreadMessageCount);
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATIONS_MASTER, loopValues, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will update no Of members in group master
    @SuppressLint("Range")
    public boolean updateNoOfGroupMembers(String chatId) {
        int noOfGroupMembers = 0;
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Prepare query for retrieving noOfMembers from group master
            String selectQuery = "SELECT Count(1) as count FROM " + DatabaseHelper.TABLE_GROUP_MEMBERS + " WHERE " + DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'";

            // Execute query to retrieve noOfMember by given chatId
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    noOfGroupMembers = mCursor.getInt(mCursor.getColumnIndex("count"));
                }
                mCursor.close();
            }

            // Prepare the content values object to update no of members
            ContentValues groupValues = new ContentValues();
            groupValues.put(DatabaseHelper.COLUMN_NO_OF_MEMBERS, noOfGroupMembers);

            // Update no of members in group master
            mDatabase.update(DatabaseHelper.TABLE_LOOP_GROUP_MASTER, groupValues, DatabaseHelper.COLUMN_CHAT_ID + " = '" + chatId + "'", null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        // return this values so we can know if we need to update UI or not
        return noOfGroupMembers == 1;
    }

    /**
     * Delete chat from local database
     *
     * @param chatId -Chat id
     */
    public void deleteChat(String chatId) {
        try {
            mDatabase.beginTransaction();

            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_SUBSCRIBERS, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_PENDING_REQUESTS, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "'", null);
            if (CommunityDetailsActivity.activity != null) {
                deleteConversationVideos(chatId);
                mDatabase.delete(DatabaseHelper.TABLE_VIDEO_CACHE, DatabaseHelper.COLUMN_CONVERSATION_ID + "='" + chatId + "'", null);
            }
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteVideo(String chatId, String videoId) {
        try {
            mDatabase.beginTransaction();
            String whereClause = DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "' AND " + DatabaseHelper.COLUMN_CONVERSATION_ID + "='" + videoId + "'";
            Utility.showLog("where", whereClause);
            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, whereClause, null);
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public void deleteConversationVideos(String chatId) {
        try {
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_VIDEO_CACHE
                    + " WHERE " + DatabaseHelper.COLUMN_CONVERSATION_ID + " = '" + chatId + "'";
            Cursor mCursor = mDatabase.rawQuery(query, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        String conversationId = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONVERSATION_ID));
                        if (TextUtils.isEmpty(conversationId)) {
                            String finalFileUrl = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_VIDEO_PATH));
                            File file = new File(CommunityDetailsActivity.activity.getCacheDir(), finalFileUrl);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                    while (mCursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteChatWithStatus(String chatId) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "' AND " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + "=2", null);

            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteChatForSyncReply(String chatId) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "' AND (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 1)", null);

            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePendingChatForSyncReply(String chatId) {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, DatabaseHelper.COLUMN_CHAT_ID + "='" + chatId + "' AND (" + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS + " = 1 OR " + DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS + " = 2)", null);

            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSavedVideo(String videoId) {
        try {
            mDatabase.beginTransaction();

            mDatabase.delete(DatabaseHelper.TABLE_SAVED_VIDEO, DatabaseHelper.COLUMN_VIDEO_ID + "='" + videoId + "'", null);

            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteReaction(String videoURL) {
        try {
            mDatabase.beginTransaction();

            mDatabase.delete(DatabaseHelper.TABLE_CHAT_MASTER, DatabaseHelper.COLUMN_VIDEO_URL + "='" + videoURL + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, DatabaseHelper.COLUMN_VIDEO_URL + "='" + videoURL + "'", null);
            mDatabase.delete(DatabaseHelper.TABLE_GROUP, DatabaseHelper.COLUMN_VIDEO_URL + "='" + videoURL + "'", null);
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateChatIdInReaction(String videoURL, String chatId) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_CHAT_ID, chatId);
            mDatabase.update(DatabaseHelper.TABLE_CONVERSATION_MEMBERS, values, DatabaseHelper.COLUMN_VIDEO_URL + "='" + videoURL + "'", null);
            mDatabase.update(DatabaseHelper.TABLE_GROUP, values, DatabaseHelper.COLUMN_VIDEO_URL + "='" + videoURL + "'", null);
            values.put(DatabaseHelper.COLUMN_VIDEO_UPLOAD_STATUS, 3);
            values.put(DatabaseHelper.COLUMN_IMAGE_UPLOAD_STATUS, 2);
            values.put(DatabaseHelper.COLUMN_COMPRESSION_STATUS, 1);
            mDatabase.update(DatabaseHelper.TABLE_CHAT_MASTER, values, DatabaseHelper.COLUMN_VIDEO_URL + "='" + videoURL + "'", null);
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public HashMap<String, ContactsModel> getAllContactsHashMap() {
//        HashMap<String, ContactsModel> contactLists = new HashMap<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONTACTS;
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                Type contactType = new TypeToken<ContactsModel.Genuin>() {
//                }.getType();
//                if (mCursor.moveToFirst()) {
//                    do {
//                        ContactsModel contact = new ContactsModel();
//                        contact.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
//                        contact.setFirstName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME)));
//                        contact.setLastName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME)));
//                        contact.setMiddleName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME)));
//                        contact.setBlocked(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BLOCKED)) == 1);
//                        contact.setCanPrivateChat(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CAN_PRIVATE_CHAT)) == 1);
//                        String contactStr = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_GENUIN));
//                        if (!TextUtils.isEmpty(contactStr)) {
//                            Gson gson = new Gson();
//                            ContactsModel.Genuin genuin = gson.fromJson(contactStr, contactType);
//                            contact.setGenuin(genuin);
//                            if (genuin.getName() != null) {
//                                String genuinName = genuin.getName();
//                                if (genuinName != null && !TextUtils.isEmpty(genuinName)) {
//                                    contact.setFirstName(genuinName);
//                                }
//                            }
//                        }
//                        if (!contactLists.containsKey(contact.getPhoneNumber())) {
//                            contactLists.put(contact.getPhoneNumber(), contact);
//                        }
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return contactLists;
//    }
//
//    public List<ContactsModel> getAllContacts() {
//        List<ContactsModel> contactLists = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONTACTS;
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                Type contactType = new TypeToken<ContactsModel.Genuin>() {
//                }.getType();
//                if (mCursor.moveToFirst()) {
//                    do {
//                        ContactsModel contact = new ContactsModel();
//                        contact.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
//                        contact.setFirstName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME)));
//                        contact.setLastName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME)));
//                        contact.setMiddleName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME)));
//                        contact.setBlocked(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BLOCKED)) == 1);
//                        contact.setCanPrivateChat(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CAN_PRIVATE_CHAT)) == 1);
//                        String contactStr = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_GENUIN));
//                        if (!TextUtils.isEmpty(contactStr)) {
//                            Gson gson = new Gson();
//                            ContactsModel.Genuin genuin = gson.fromJson(contactStr, contactType);
//                            contact.setGenuin(genuin);
//                            if (genuin.getName() != null) {
//                                String genuinName = genuin.getName();
//                                if (genuinName != null && !TextUtils.isEmpty(genuinName)) {
//                                    contact.setFirstName(genuinName);
//                                }
//                            }
//                        }
//                        contactLists.add(contact);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return contactLists;
//    }

    public int getSyncedContactCount() {
        int count = 0;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CONTACTS;
            Cursor cursor = mDatabase.rawQuery(selectQuery, null);
            if (null != cursor) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return count;
    }

    public int getTempSyncedContactCount() {
        int count = 0;
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TEMP_CONTACTS;
            Cursor cursor = mDatabase.rawQuery(selectQuery, null);
            if (null != cursor) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    count = cursor.getInt(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return count;
    }

//    public List<ContactsModel> getTempContactsForSync() {
//        List<ContactsModel> contactLists = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_TEMP_CONTACTS;
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                if (mCursor.moveToFirst()) {
//                    do {
//                        ContactsModel contact = new ContactsModel();
//                        contact.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
//                        contact.setFirstName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME)));
//                        contact.setLastName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME)));
//                        contact.setMiddleName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME)));
//                        contactLists.add(contact);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return contactLists;
//    }
//
//    public List<ContactsModel> getAllContactsForSync() {
//        List<ContactsModel> contactLists = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONTACTS;
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                if (mCursor.moveToFirst()) {
//                    do {
//                        ContactsModel contact = new ContactsModel();
//                        contact.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
//                        contact.setFirstName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME)));
//                        contact.setLastName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME)));
//                        contact.setMiddleName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME)));
//                        contactLists.add(contact);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return contactLists;
//    }
//
//    public List<ContactsModel> getNormalContacts() {
//        List<ContactsModel> contactLists = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONTACTS + " WHERE " + DatabaseHelper.COLUMN_CONTACTS_GENUIN + " = ''";
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                if (mCursor.moveToFirst()) {
//                    do {
//                        ContactsModel contact = new ContactsModel();
//                        contact.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
//                        contact.setFirstName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME)));
//                        contact.setLastName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME)));
//                        contact.setMiddleName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME)));
//                        contact.setBlocked(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BLOCKED)) == 1);
//                        contact.setCanPrivateChat(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CAN_PRIVATE_CHAT)) == 1);
//                        contactLists.add(contact);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return contactLists;
//    }
//
//    public List<ContactsModel> getGenuinContacts() {
//        List<ContactsModel> contactLists = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONTACTS + " WHERE " + DatabaseHelper.COLUMN_CONTACTS_GENUIN + " != ''";
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                Type contactType = new TypeToken<ContactsModel.Genuin>() {
//                }.getType();
//                if (mCursor.moveToFirst()) {
//                    do {
//                        ContactsModel contact = new ContactsModel();
//                        contact.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
//                        contact.setFirstName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME)));
//                        contact.setLastName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME)));
//                        contact.setMiddleName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME)));
//                        contact.setBlocked(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_BLOCKED)) == 1);
//                        contact.setCanPrivateChat(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CAN_PRIVATE_CHAT)) == 1);
//                        String contactStr = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_GENUIN));
//                        Gson gson = new Gson();
//                        ContactsModel.Genuin genuin = gson.fromJson(contactStr, contactType);
//                        contact.setGenuin(genuin);
//                        contactLists.add(contact);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return contactLists;
//    }
//
//    public void insertContacts(List<ContactsModel> contactList) {
//        try {
//            mDatabase.beginTransaction();
//            mDatabase.delete(DatabaseHelper.TABLE_CONTACTS, null, null);
//            if (contactList != null && contactList.size() > 0) {
//                Type contactType = new TypeToken<ContactsModel.Genuin>() {
//                }.getType();
//                for (int i = 0; i < contactList.size(); i++) {
//                    try {
//                        ContactsModel contact = contactList.get(i);
//                        ContentValues values = new ContentValues();
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME, contact.getFirstName());
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME, contact.getLastName());
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME, contact.getMiddleName());
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_PHONE, contact.getPhoneNumber());
//                        values.put(DatabaseHelper.COLUMN_IS_BLOCKED, contact.isBlocked() ? 1 : 0);
//                        values.put(DatabaseHelper.COLUMN_CAN_PRIVATE_CHAT, contact.isCanPrivateChat() ? 1 : 0);
//                        if (contact.getGenuin() != null) {
//                            String contactStr = new Gson().toJson(contact.getGenuin(), contactType);
//                            values.put(DatabaseHelper.COLUMN_CONTACTS_GENUIN, contactStr);
//                        } else {
//                            values.put(DatabaseHelper.COLUMN_CONTACTS_GENUIN, "");
//                        }
//                        mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_CONTACTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//    }

    public void deleteTempContacts() {
        try {
            mDatabase.beginTransaction();
            mDatabase.delete(DatabaseHelper.TABLE_TEMP_CONTACTS, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

//    public void insertTempContacts(List<ContactsModel> contactList) {
//        try {
//            mDatabase.beginTransaction();
//            mDatabase.delete(DatabaseHelper.TABLE_TEMP_CONTACTS, null, null);
//            if (contactList != null && contactList.size() > 0) {
//                for (int i = 0; i < contactList.size(); i++) {
//                    try {
//                        ContactsModel contact = contactList.get(i);
//                        ContentValues values = new ContentValues();
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_FIRST_NAME, contact.getFirstName());
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_LAST_NAME, contact.getLastName());
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_MIDDLE_NAME, contact.getMiddleName());
//                        values.put(DatabaseHelper.COLUMN_CONTACTS_PHONE, contact.getPhoneNumber());
//                        mDatabase.insertWithOnConflict(DatabaseHelper.TABLE_TEMP_CONTACTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//    }

    @SuppressLint("Range")
    public List<MembersModel> getRecentConversationMembers(String myUserId) {
        List<MembersModel> contactLists = new ArrayList<>();
        try {
            mDatabase.beginTransaction();
            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_CONVERSATION_MEMBERS + " as cm2 WHERE cm2.user_id != '" + myUserId + "' AND cm2.member_status = 1 AND cm2.conversation_type = 2 AND cm2.chat_id IN (SELECT chat_id FROM " + DatabaseHelper.TABLE_CONVERSATION_MEMBERS + " as cm WHERE cm.chat_id IN (SELECT chat_id FROM " + DatabaseHelper.TABLE_CONVERSATION_MEMBERS + " WHERE user_id = '" + myUserId + "') GROUP BY cm.chat_id HAVING COUNT(cm.user_id) <= 2) GROUP BY cm2.user_id ORDER BY cm2.conversation_at DESC";
            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
            if (mCursor != null) {
                if (mCursor.moveToFirst()) {
                    do {
                        MembersModel member = new MembersModel();
                        member.setUserId(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                        member.setName(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
                        member.setNickname(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_NICKNAME)));
                        member.setAvatar(mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_AVATAR)) == 1);
                        member.setProfileImage(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE)));
                        member.setProfileImageL(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_L)));
                        member.setProfileImageM(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_M)));
                        member.setProfileImageS(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE_S)));
                        member.setPhone(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_PHONE)));
                        member.setBio(mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACTS_BIO)));
                        contactLists.add(member);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }

        } catch (IllegalStateException e) {
            Utility.showLogException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
        return contactLists;
    }

//    public List<UTMSourceModel> getAllUTMSourcesForSync() {
//        List<UTMSourceModel> utmSourceList = new ArrayList<>();
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_UTM_SOURCE + " WHERE " + DatabaseHelper.COLUMN_IS_DUMPED + " = 0";
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                if (mCursor.moveToFirst()) {
//                    do {
//                        UTMSourceModel utmSource = new UTMSourceModel();
//                        utmSource.id = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
//                        utmSource.deepLink = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DEEP_LINK));
//                        utmSource.utmSource = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UTM_SOURCE));
//                        utmSource.utmMedium = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UTM_MEDIUM));
//                        utmSource.utmCampaign = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UTM_CAMPAIGN));
//                        utmSource.action = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_ACTION));
//                        utmSource.fromUsername = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FROM_USERNAME));
//                        utmSource.sourceId = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SOURCE_ID));
//                        utmSource.parentId = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PARENT_ID));
//                        utmSource.contentType = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTENT_TYPE));
//                        utmSource.createdAt = mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT));
//                        utmSource.isDumped = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_DUMPED)) == 1;
//                        utmSourceList.add(utmSource);
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return utmSourceList;
//    }
//
//    public UTMSourceModel getLatestUTMSource() {
//        UTMSourceModel utmSource = null;
//        try {
//            mDatabase.beginTransaction();
//            String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_UTM_SOURCE + " ORDER BY " + DatabaseHelper.COLUMN_CREATED_AT + " DESC LIMIT 1";
//            Cursor mCursor = mDatabase.rawQuery(selectQuery, null);
//            if (mCursor != null) {
//                if (mCursor.moveToFirst()) {
//                    do {
//                        utmSource = new UTMSourceModel();
//                        utmSource.id = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
//                        utmSource.deepLink = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_DEEP_LINK));
//                        utmSource.utmSource = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UTM_SOURCE));
//                        utmSource.utmMedium = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UTM_MEDIUM));
//                        utmSource.utmCampaign = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_UTM_CAMPAIGN));
//                        utmSource.action = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_ACTION));
//                        utmSource.fromUsername = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_FROM_USERNAME));
//                        utmSource.sourceId = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_SOURCE_ID));
//                        utmSource.parentId = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_PARENT_ID));
//                        utmSource.contentType = mCursor.getString(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CONTENT_TYPE));
//                        utmSource.createdAt = mCursor.getLong(mCursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT));
//                        utmSource.isDumped = mCursor.getInt(mCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_DUMPED)) == 1;
//                    } while (mCursor.moveToNext());
//                }
//                mCursor.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (isOpen()) {
//                mDatabase.setTransactionSuccessful();
//                mDatabase.endTransaction();
//            }
//        }
//        return utmSource;
//    }
//
//    public long insertUtmSource(UTMSourceModel utmSourceModel) {
//        long id = -1;
//        try {
//            ContentValues values = new ContentValues();
//            values.put(DatabaseHelper.COLUMN_UTM_SOURCE, utmSourceModel.utmSource);
//            values.put(DatabaseHelper.COLUMN_UTM_MEDIUM, utmSourceModel.utmMedium);
//            values.put(DatabaseHelper.COLUMN_UTM_CAMPAIGN, utmSourceModel.utmCampaign);
//            values.put(DatabaseHelper.COLUMN_DEEP_LINK, utmSourceModel.deepLink);
//            values.put(DatabaseHelper.COLUMN_ACTION, utmSourceModel.action);
//            values.put(DatabaseHelper.COLUMN_FROM_USERNAME, utmSourceModel.fromUsername);
//            values.put(DatabaseHelper.COLUMN_SOURCE_ID, utmSourceModel.sourceId);
//            values.put(DatabaseHelper.COLUMN_PARENT_ID, utmSourceModel.parentId);
//            values.put(DatabaseHelper.COLUMN_CONTENT_TYPE, utmSourceModel.contentType);
//            values.put(DatabaseHelper.COLUMN_CREATED_AT, utmSourceModel.createdAt);
//            values.put(DatabaseHelper.COLUMN_IS_DUMPED, utmSourceModel.isDumped ? 1 : 0);
//            id = mDatabase.insert(DatabaseHelper.TABLE_UTM_SOURCE, null, values);
//        } catch (Exception e) {
//            Utility.showLog("db", "Unable to insert");
//        }
//        return id;
//    }

    public void updateUTMDumpedStatus(int id) {
        try {
            mDatabase.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_IS_DUMPED, 1);
            mDatabase.update(DatabaseHelper.TABLE_UTM_SOURCE, values, DatabaseHelper.COLUMN_ID + " = " + id, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }

    // This function will delete already dumped utm sources
    public void deleteDumpedUTMSources() {
        try {

            // Initialize db transaction
            mDatabase.beginTransaction();

            // Execute delete query for already dumped utm sources
            mDatabase.delete(DatabaseHelper.TABLE_UTM_SOURCE, DatabaseHelper.COLUMN_IS_DUMPED + " = 1", null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the db transaction
            if (isOpen()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }
    }
}