package com.begenuin.library.data.remote.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.begenuin.library.SDKInitiate;
import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.core.enums.VideoConvType;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.eventbus.CommentVideoProgressUpdateEvent;
import com.begenuin.library.data.eventbus.ConversationUpdateEvent;
import com.begenuin.library.data.eventbus.ConversationVideoProgressUpdateEvent;
import com.begenuin.library.data.eventbus.PublicVideoStatusChangedEvent;
import com.begenuin.library.data.eventbus.VideoUploadedEvent;
import com.begenuin.library.data.model.VideoParamsModel;
import com.begenuin.library.data.remote.BaseAPIService;
import com.begenuin.library.data.viewmodel.UploadQueueManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class UploadVideoService extends IntentService {

    private String videoFile, imageFile, dpFile;
    private String fileName, imageFileName, dpFileName;
    private File dataFile, imageDataFile, dpDataFile;
    private float fileSize;
    private String reason;
    private String from = "";
    private long startTime, totalTime;
    private VideoParamsModel videoConfigModel = new VideoParamsModel();
    private int uploadProgress;

    public UploadVideoService() {
        super("UploadVideoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            startTime = System.currentTimeMillis();
            if (intent.hasExtra("videoConfig")) {
                videoConfigModel = (VideoParamsModel) intent.getSerializableExtra("videoConfig");
                videoFile = videoConfigModel.videoFile;
                imageFile = videoConfigModel.imageFile;
                dpFile = videoConfigModel.dpFile;
                from = videoConfigModel.from;
                setFileNames();
                videoConfigModel.videoFileName = fileName;
                videoConfigModel.imageFileName = imageFileName;
                videoConfigModel.dpFileName = dpFileName;
                videoConfigModel.fileSize = fileSize;
                //getCredential();
                if (videoConfigModel.isImageRequired) {
                    uploadImage();
                }
                if (videoConfigModel.isDpRequired) {
                    uploadDp();
                }
                if (videoConfigModel.isVideoRequired) {
                    uploadVideo();
                }
                if(videoConfigModel.isAudioRequired){
                    uploadAudio();
                }
            }
        }
    }

    private void setFileNames() {
        if (!TextUtils.isEmpty(videoFile)) {
            dataFile = new File(videoFile);
            fileName = dataFile.getName();
            try {
                if (dataFile.exists()) {
                    // Get length of file in bytes
                    long fileSizeInBytes = dataFile.length();
                    // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                    float fileSizeInKB = fileSizeInBytes / 1024f;
                    // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                    fileSize = fileSizeInKB / 1024f;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (fileName.startsWith("_")) {
                //String userId = SharedPrefUtils.getStringPreference(getApplicationContext(), Constants.PREF_USER);
                String userId = SDKInitiate.INSTANCE.getUserId();
                fileName = userId + fileName;
            }
        }

        if (!TextUtils.isEmpty(imageFile)) {
            imageDataFile = new File(imageFile);
            imageFileName = imageDataFile.getName();

            if (imageFileName.startsWith("_")) {
                //String userId = SharedPrefUtils.getStringPreference(getApplicationContext(), Constants.PREF_USER);
                String userId = SDKInitiate.INSTANCE.getUserId();
                imageFileName = userId + imageFileName;
            }
        }

        if (!TextUtils.isEmpty(dpFile)) {
            dpDataFile = new File(dpFile);
            dpFileName = dpDataFile.getName();

            if (dpFileName.startsWith("_")) {
                //String userId = SharedPrefUtils.getStringPreference(getApplicationContext(), Constants.PREF_USER);
                String userId = SDKInitiate.INSTANCE.getUserId();
                dpFileName = userId + dpFileName;
            }
        }
    }

    private boolean isPublicOrRecordForOther() {
        return (from.equalsIgnoreCase(Constants.FROM_PUBLIC_VIDEO) || from.equalsIgnoreCase(Constants.FROM_RECORD_FOR_OTHER));
    }

    private void updateVideoToServer() {
        UploadQueueManager.getInstance().videoUploadedCallBack(UploadVideoService.this, videoConfigModel);
        //sendDataDogLogs();
        String module = Constants.CREATE_PUBLIC_VIDEO;
        try {
            JSONObject jsonObject = new JSONObject();

            JSONObject settingsObject = new JSONObject(videoConfigModel.settings);
            jsonObject.put("video_name", fileName);
            jsonObject.put("thumbnail_name", imageFileName);
            jsonObject.put("tags", videoConfigModel.tags);
            jsonObject.put("link", videoConfigModel.link);
            jsonObject.put("description", videoConfigModel.description);
            jsonObject.put("size", videoConfigModel.size);
            jsonObject.put("duration", videoConfigModel.duration);
            jsonObject.put("aspect_ratio", videoConfigModel.aspectRatio);
            jsonObject.put("resolution", videoConfigModel.resolution);
            jsonObject.put("settings", settingsObject);
            if (!TextUtils.isEmpty(videoConfigModel.selectedQuestions)) {
                JSONArray jsonArray = new JSONArray(videoConfigModel.selectedQuestions);
                jsonObject.put("questions", jsonArray);
            }
            if (!TextUtils.isEmpty(videoConfigModel.qrCode)) {
                jsonObject.put("qr_code", videoConfigModel.qrCode);
            }
            if (Utility.getDBHelper() != null) {
                String shareURL = Utility.getDBHelper().getShareURLPublicVideo(videoFile);
                if (!TextUtils.isEmpty(shareURL)) {
                    jsonObject.put("share_string", shareURL);
                }
            }
            new BaseAPIService(this, module, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    if (Utility.getDBHelper() != null) {
                        // Video successfully updated to server.
                        try {
                            JSONObject responseJson = new JSONObject(response);
                            JSONObject dataJson = responseJson.getJSONObject(Constants.JSON_DATA);
                            String videoId = dataJson.getString("video_id");
                            String videoPath = dataJson.getString("video_path");
                            String thumbnailUrl = dataJson.getString("video_thumbnail");
                            String shareUrl = dataJson.getString("share_url");
                            Utility.getDBHelper().updatePublicVideo(videoFile, videoId, videoPath, thumbnailUrl, shareUrl);

//                            HashMap<String, Object> map = new HashMap<>();
//                            map.put(Constants.KEY_CONTENT_ID, videoId);
//                            map.put(Constants.KEY_CONTENT_CATEGORY, Utility.getContentType(Constants.FROM_PUBLIC_VIDEO));
//                            String topic = Utility.getAFTopic(UploadVideoService.this, settingsObject.optString("topic", ""));
//                            if (!TextUtils.isEmpty(topic)) {
//                                map.put(Constants.KEY_STICKER_LABEL, topic);
//                            }
//                            if (!TextUtils.isEmpty(videoConfigModel.selectedQuestions)) {
//                                map.put(Constants.KEY_QUESTION, true);
//                            } else {
//                                map.put(Constants.KEY_QUESTION, false);
//                            }
//                            GenuInApplication.getInstance().sendEventLogs(Constants.GENUIN_VIDEO_PUBLISHED, map, LogType.EVENT);

//                            PublicVideoStatusChangedEvent publicVideo = new PublicVideoStatusChangedEvent();
//                            publicVideo.videoId = videoId;
//                            publicVideo.videoLocalPath = videoFile;
//                            publicVideo.isRetry = false;
//                            publicVideo.videoUploadStatus = 2;
//                            publicVideo.imageUploadStatus = 2;
//                            publicVideo.apiUploadStatus = 1;
//                            publicVideo.compressionStatus = 1;
//                            EventBus.getDefault().post(publicVideo);
//
//                            PublicVideoCreatedEvent publicVideoCreated = new PublicVideoCreatedEvent();
//                            publicVideoCreated.videoId = videoId;
//                            publicVideoCreated.videoLocalPath = videoFile;
//                            publicVideoCreated.videoPath = videoPath;
//                            publicVideoCreated.thumbnailUrl = thumbnailUrl;
//                            publicVideoCreated.shareURL = shareUrl;
//                            EventBus.getDefault().post(publicVideoCreated);

                        } catch (Exception e) {
                            Utility.showLogException(e);
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(error);
                        String code = jsonObject.optString("code", "");
                        if (code.equalsIgnoreCase(Constants.CODE_5156)) {
                            // Already published video so delete from db.
                            if (Utility.getDBHelper() != null) {
                                Utility.getDBHelper().deletePublicVideoByPath(videoFile);
                            }
                        } else {
                            if (Utility.getDBHelper() != null) {
                                Utility.getDBHelper().updateProfileRetryStatus(videoFile, true);
                            }
                            //sendBroadCastForRetryPublicVideo(2, 2, 2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (Utility.getDBHelper() != null) {
                            Utility.getDBHelper().updateProfileRetryStatus(videoFile, true);
                        }
                        //sendBroadCastForRetryPublicVideo(2, 2, 2);
                    }
                }
            }, "POST", false);
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    private String[] getBaseURLAndPath(String uploadURL) {
        try {
            URL url = new URL(uploadURL);
            String baseUrl = url.getProtocol() + "://" + url.getHost();
            String[] arr = {baseUrl, uploadURL.substring(baseUrl.length())};
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[2];
        }
    }

    private void uploadVideo() {
        if (isPublicOrRecordForOther()) {
            if (Utility.getDBHelper() != null) {
                Utility.getDBHelper().updatePublicVideoStatus(videoFile, 1);
            }
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contentType", "video/mp4");
            jsonObject.put("path", "temp_video/" + fileName);
            new BaseAPIService(this, Constants.GET_UPLOAD_URL, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        String uploadURL = dataJson.optString("uploadURL", "");
                        if (!TextUtils.isEmpty(uploadURL)) {
                            String[] baseURlAndPath = getBaseURLAndPath(uploadURL);
                            startVideoUpload(baseURlAndPath[0], baseURlAndPath[1]);
                        } else {
                            reason = "PreSigned URL is not received for video";
                            updateRetryAfterAPIFailed();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        reason = e.getMessage();
                        updateRetryAfterAPIFailed();
                    }
                }

                @Override
                public void onFailure(String error) {
                    reason = "PreSigned URL API failed for video";
                    updateRetryAfterAPIFailed();
                }
            }, "POST", false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startVideoUpload(String baseURL, String path) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("video/mp4"), dataFile);
        new BaseAPIService(this, baseURL, path, requestBody, new ResponseListener() {
            @Override
            public void onSuccess(String response) {
                if (Utility.getDBHelper() == null) {
                    return;
                }
                videoConfigModel.isVideoRequired = false;
                if (isPublicOrRecordForOther()) {
                    Utility.getDBHelper().updatePublicVideoStatus(videoFile, 2);
                    if (Utility.getDBHelper().checkPublicImageStatus(videoFile)) {
                        updateVideoToServer();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_DIRECT) || from.equalsIgnoreCase(Constants.FROM_GROUP)) {
                    Utility.getDBHelper().updateReactionOrReplyVideoStatus(videoFile);
                    int status = Utility.getDBHelper().checkImageStatus(videoFile);
                    int dpStatus = Utility.getDBHelper().checkDpStatus(videoFile);
                    if (status == 2 && dpStatus == 2) {
                        sendBroadCastForVideoUploaded();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_ROUND_TABLE)) {
                    Utility.getDBHelper().updateLoopVideoStatus(videoFile);
                    int status = Utility.getDBHelper().checkLoopImageStatus(videoFile);
                    //int dpStatus = Utility.getDBHelper().checkLoopDpStatus(videoFile);
                    Utility.showLog("Send broadcast for video upload", ""+status);
                    if (status == 2) {
                        sendBroadCastForVideoUploaded();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
                    Utility.getDBHelper().updateCommentFileStatus(videoFile);
                    int status = Utility.getDBHelper().checkCommentImageStatus(videoFile);
                    if (status == 2) {
                        //sendBroadCastForCommentUploaded();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_REACTION) || from.equalsIgnoreCase(Constants.FROM_CHAT)) {
                    if (videoConfigModel.convType == VideoConvType.ROUND_TABLE.getValue()) {
                        Utility.getDBHelper().updateLoopVideoStatus(videoFile);
                        int status = Utility.getDBHelper().checkLoopImageStatus(videoFile);
                        Utility.showLog("Send broadcast for video upload", ""+status);
                        //if (status == 2) {
                            sendBroadCastForVideoUploaded();
                        //}
                    } else {
                        Utility.getDBHelper().updateReactionOrReplyVideoStatus(videoFile);
                        int status = Utility.getDBHelper().checkImageStatus(videoFile);
                        if (status == 2) {
                            sendBroadCastForVideoUploaded();
                        }
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                reason = error;
                new Handler(Looper.getMainLooper()).post(() -> updateRetry());
            }
        }, (bytesCurrent, bytesTotal, done) -> {
            int percentage = (int) ((bytesCurrent * 1f / bytesTotal) * 100);
            Utility.showLog("Percentage", percentage + " " + bytesCurrent + " " + bytesTotal);
            if (percentage - uploadProgress > 5 || percentage == 100) {
//                if (isPublicOrRecordForOther()) {
//                    GenuinVideoProgressUpdateEvent genuinVideoProgressUpdateEvent = new GenuinVideoProgressUpdateEvent();
//                    genuinVideoProgressUpdateEvent.localVideoPath = videoFile;
//                    genuinVideoProgressUpdateEvent.progress = percentage;
//                    EventBus.getDefault().post(genuinVideoProgressUpdateEvent);
//                } else
                    if (from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
                    CommentVideoProgressUpdateEvent commentVideoProgressUpdateEvent = new CommentVideoProgressUpdateEvent();
                    commentVideoProgressUpdateEvent.localVideoPath = videoFile;
                    commentVideoProgressUpdateEvent.progress = percentage;
                    EventBus.getDefault().post(commentVideoProgressUpdateEvent);
                } else {
                    ConversationVideoProgressUpdateEvent model = new ConversationVideoProgressUpdateEvent();
                    model.isRT = isRT();
                    model.localVideoPath = videoFile;
                    model.progress = percentage;
                    EventBus.getDefault().post(model);
                }
                uploadProgress = percentage;
            }
        });
    }

    private void uploadAudio() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contentType", "audio/wav");
            jsonObject.put("path", "audio/" + fileName);
            new BaseAPIService(this, Constants.GET_UPLOAD_URL, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        String uploadURL = dataJson.optString("uploadURL", "");
                        if (!TextUtils.isEmpty(uploadURL)) {
                            String[] baseURlAndPath = getBaseURLAndPath(uploadURL);
                            startAudioUpload(baseURlAndPath[0], baseURlAndPath[1]);
                        } else {
                            reason = "PreSigned URL is not received for video";
                            updateRetryAfterAPIFailed();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        reason = e.getMessage();
                        updateRetryAfterAPIFailed();
                    }
                }

                @Override
                public void onFailure(String error) {
                    reason = "PreSigned URL API failed for video";
                    updateRetryAfterAPIFailed();
                }
            }, "POST", false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAudioUpload(String baseURL, String path) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/wav"), dataFile);
        new BaseAPIService(this, baseURL, path, requestBody, new ResponseListener() {
            @Override
            public void onSuccess(String response) {
                if (Utility.getDBHelper() == null) {
                    return;
                }
                videoConfigModel.isAudioRequired = false;
                if (from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
                    //sendBroadCastForCommentUploaded();
                }
            }

            @Override
            public void onFailure(String error) {
                reason = error;
                new Handler(Looper.getMainLooper()).post(() -> updateRetry());
            }
        }, (bytesCurrent, bytesTotal, done) -> {
            int percentage = (int) ((bytesCurrent * 1f / bytesTotal) * 100);
            Utility.showLog("Percentage", percentage + " " + bytesCurrent + " " + bytesTotal);
            if (percentage - uploadProgress > 5 || percentage == 100) {
                if (from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
                    CommentVideoProgressUpdateEvent commentVideoProgressUpdateEvent = new CommentVideoProgressUpdateEvent();
                    commentVideoProgressUpdateEvent.localVideoPath = videoFile;
                    commentVideoProgressUpdateEvent.progress = percentage;
                    EventBus.getDefault().post(commentVideoProgressUpdateEvent);
                }
                uploadProgress = percentage;
            }
        });
    }

    private void updateRetryAfterAPIFailed() {
        if (isPublicOrRecordForOther()) {
            if (Utility.getDBHelper() != null) {
                Utility.getDBHelper().updateProfileRetryStatus(videoFile, true);
            }
            sendBroadCastForRetryPublicVideo(0, 0, 0);
            UploadQueueManager.getInstance().videoUploadedCallBack(UploadVideoService.this, videoConfigModel);
            //sendFailedDataDogLogs();
        } else {
            updateRetry();
        }
    }

    private void uploadImage() {
        if (isPublicOrRecordForOther()) {
            if (Utility.getDBHelper() != null) {
                Utility.getDBHelper().updatePublicImageStatus(videoFile, 1);
            }
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contentType", "image/png");
            if (from.equalsIgnoreCase(Constants.FROM_PROFILE_PHOTO)) {
                jsonObject.put("path", "uploads/profile_images/" + imageFileName);
            } else {
                jsonObject.put("path", "uploads/thumbnails/" + imageFileName);
            }
            new BaseAPIService(this, Constants.GET_UPLOAD_URL, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        String uploadURL = dataJson.optString("uploadURL", "");
                        if (!TextUtils.isEmpty(uploadURL)) {
                            String[] baseURlAndPath = getBaseURLAndPath(uploadURL);
                            startImageUpload(baseURlAndPath[0], baseURlAndPath[1]);
                        } else {
                            reason = "PreSigned URL is not received for image";
                            updateRetryAfterAPIFailed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    reason = "PreSigned URL API failed for image";
                    updateRetryAfterAPIFailed();
                }
            }, "POST", false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startImageUpload(String baseURL, String path) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), imageDataFile);
        new BaseAPIService(this, baseURL, path, requestBody, new ResponseListener() {
            @Override
            public void onSuccess(String response) {
                if (Utility.getDBHelper() == null) {
                    return;
                }
                videoConfigModel.isImageRequired = false;
                if (isPublicOrRecordForOther()) {
                    Utility.getDBHelper().updatePublicImageStatus(videoFile, 2);
                    if (Utility.getDBHelper().checkPublicVideoStatus(videoFile)) {
                        updateVideoToServer();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_DIRECT) || from.equalsIgnoreCase(Constants.FROM_GROUP)) {
                    Utility.getDBHelper().updateReactionOrReplyImageStatus(videoFile);
                    int status = Utility.getDBHelper().checkVideoStatus(videoFile);
                    int dpStatus = Utility.getDBHelper().checkDpStatus(videoFile);
                    if (status == 2 && dpStatus == 2) {
                        sendBroadCastForVideoUploaded();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_ROUND_TABLE)) {
                    Utility.getDBHelper().updateLoopImageStatus(videoFile);
                    int status = Utility.getDBHelper().checkLoopVideoStatus(videoFile);
                    int dpStatus = Utility.getDBHelper().checkLoopDpStatus(videoFile);
                    if (status == 2 && dpStatus == 2) {
                        sendBroadCastForVideoUploaded();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
                    Utility.getDBHelper().updateCommentImageStatus(videoFile);
                    int status = Utility.getDBHelper().checkCommentFileStatus(videoFile);
                    if (status == 2) {
                        //sendBroadCastForCommentUploaded();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_REACTION) || from.equalsIgnoreCase(Constants.FROM_CHAT)) {
                    if (videoConfigModel.convType == VideoConvType.ROUND_TABLE.getValue()) {
                        Utility.getDBHelper().updateLoopImageStatus(videoFile);
                        int status = Utility.getDBHelper().checkLoopVideoStatus(videoFile);
                        if (status == 2) {
                            sendBroadCastForVideoUploaded();
                        }
                    } else {
                        Utility.getDBHelper().updateReactionOrReplyImageStatus(videoFile);
                        int status = Utility.getDBHelper().checkVideoStatus(videoFile);
                        if (status == 2) {
                            sendBroadCastForVideoUploaded();
                        }
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_CHANGE_COVER)) {
                    sendBroadCastForCoverUploaded();
                } else if (from.equalsIgnoreCase(Constants.FROM_PROFILE_PHOTO)) {
                    sendBroadCastForProfilePhotoUploaded(false);
                }
            }

            @Override
            public void onFailure(String error) {
                reason = error;
                new Handler(Looper.getMainLooper()).post(() -> updateRetry());
            }
        }, (bytesCurrent, bytesTotal, done) -> {

        });
    }

    private void uploadDp() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contentType", "image/png");
            jsonObject.put("path", "uploads/profile_images/rt/" + dpFileName);

            new BaseAPIService(this, Constants.GET_UPLOAD_URL, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        String uploadURL = dataJson.optString("uploadURL", "");
                        if (!TextUtils.isEmpty(uploadURL)) {
                            String[] baseURlAndPath = getBaseURLAndPath(uploadURL);
                            startDpUpload(baseURlAndPath[0], baseURlAndPath[1]);
                        } else {
                            reason = "PreSigned URL is not received for dp";
                            updateRetryAfterAPIFailed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    reason = "PreSigned URL API failed for dp";
                    updateRetryAfterAPIFailed();
                }
            }, "POST", false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDpUpload(String baseURL, String path) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), dpDataFile);
        new BaseAPIService(this, baseURL, path, requestBody, new ResponseListener() {
            @Override
            public void onSuccess(String response) {
                if (Utility.getDBHelper() == null) {
                    return;
                }
                videoConfigModel.isDpRequired = false;
                if (from.equalsIgnoreCase(Constants.FROM_GROUP)) {
                    Utility.getDBHelper().updateReactionOrReplyDPStatus(videoFile);
                    int status = Utility.getDBHelper().checkVideoStatus(videoFile);
                    int imageStatus = Utility.getDBHelper().checkImageStatus(videoFile);
                    if (status == 2 && imageStatus == 2) {
                        sendBroadCastForVideoUploaded();
                    }
                } else if (from.equalsIgnoreCase(Constants.FROM_ROUND_TABLE)) {
                    Utility.getDBHelper().updateLoopDPStatus(videoFile);
                    int status = Utility.getDBHelper().checkLoopVideoStatus(videoFile);
                    int imageStatus = Utility.getDBHelper().checkLoopImageStatus(videoFile);
                    if (status == 2 && imageStatus == 2) {
                        sendBroadCastForVideoUploaded();
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                reason = error;
                new Handler(Looper.getMainLooper()).post(() -> updateRetry());
            }
        }, (bytesCurrent, bytesTotal, done) -> {

        });
    }

    private void sendBroadCastForCoverUploaded() {
        if (videoConfigModel.changeCoverModel != null) {
            EventBus.getDefault().post(videoConfigModel.changeCoverModel);
        }
    }

    private void sendBroadCastForProfilePhotoUploaded(boolean isFailure) {
//        if (videoConfigModel.profilePhotoModel != null) {
//            if (!isFailure && videoConfigModel.profilePhotoModel.isDirectUpload()) {
//                ChangeProfilePhotoManager.getInstance().callUpdateProfilePhotoAPI(UploadVideoService.this, videoConfigModel.profilePhotoModel);
//            } else {
//                ProfilePhotoUploadedEvent profilePhotoUploaded = new ProfilePhotoUploadedEvent();
//                profilePhotoUploaded.isFailure = isFailure;
//                profilePhotoUploaded.model = videoConfigModel.profilePhotoModel;
//                EventBus.getDefault().post(profilePhotoUploaded);
//            }
//        }
    }

    private void sendBroadCastForVideoUploaded() {
        //sendDataDogLogs();
        VideoUploadedEvent videoUploaded = new VideoUploadedEvent();
        videoUploaded.from = from;
        videoUploaded.videoParamsModel = videoConfigModel;
        videoUploaded.chatId = videoConfigModel.chatId;
//        if (from.equalsIgnoreCase(Constants.FROM_REACTION)) {
//            videoUploaded.discoverModel = videoConfigModel.discoverModel;
//        } else if (from.equalsIgnoreCase(Constants.FROM_CHAT)) {
//            videoUploaded.chatId = videoConfigModel.chatId;
//        }
        EventBus.getDefault().post(videoUploaded);
        UploadQueueManager.getInstance().videoUploadedCallBack(UploadVideoService.this, videoConfigModel);
    }

//    private void sendBroadCastForCommentUploaded() {
//        sendDataDogLogs();
//        VideoUploadedEvent videoUploaded = new VideoUploadedEvent();
//        videoUploaded.from = from;
//        videoUploaded.videoParamsModel = videoConfigModel;
//        videoUploaded.chatId = videoConfigModel.chatId;
//        videoUploaded.videoId = videoConfigModel.videoId;
//        EventBus.getDefault().post(videoUploaded);
//        UploadQueueManager.getInstance().videoUploadedCallBack(UploadVideoService.this, videoConfigModel);
//    }
//

    private void sendBroadCastForRetryPublicVideo(int videoUploadStatus, int imageUploadStatus, int apiUploadStatus) {
        PublicVideoStatusChangedEvent publicVideo = new PublicVideoStatusChangedEvent();
        publicVideo.videoLocalPath = videoFile;
        publicVideo.isRetry = true;
        publicVideo.videoUploadStatus = videoUploadStatus;
        publicVideo.imageUploadStatus = imageUploadStatus;
        publicVideo.apiUploadStatus = apiUploadStatus;
        publicVideo.compressionStatus = 1;
        EventBus.getDefault().post(publicVideo);
    }

    private void updateRetry() {
        UploadQueueManager.getInstance().videoUploadedCallBack(UploadVideoService.this, videoConfigModel);
        if (from.equalsIgnoreCase(Constants.FROM_CHAT)) {
            if (Utility.getDBHelper() != null) {
                if (videoConfigModel.convType == VideoConvType.ROUND_TABLE.getValue()) {
                    // Update retry status for particular loop video in DB by local path
                    Utility.getDBHelper()
                            .updateRetryStatusForLoopVideo(
                                    videoFile, true
                            );
                    EventBus.getDefault().post(new ConversationUpdateEvent(true));
                } else {
                    // Changed the way to storing retry status to DB, now it will store Retry status on the basic of localVideoPath
                    Utility.getDBHelper().updateRetryStatus(videoFile, true);
                    EventBus.getDefault().post(new ConversationUpdateEvent(false));
                }
            }
        } else if (from.equalsIgnoreCase(Constants.FROM_REACTION)) {
            if (Utility.getDBHelper() != null && videoConfigModel.discoverModel != null) {
                // Changed the way to storing retry status to DB, now it will store Retry status on the basic of localVideoPath
                Utility.getDBHelper().updateRetryStatus(videoFile, true);
            }
            EventBus.getDefault().post(new ConversationUpdateEvent(isRT()));
        } else if (from.equalsIgnoreCase(Constants.FROM_DIRECT) || from.equalsIgnoreCase(Constants.FROM_GROUP)) {
            if (Utility.getDBHelper() != null) {
                // Changed the way to storing retry status to DB, now it will store Retry status on the basic of localVideoPath
                Utility.getDBHelper().updateRetryStatus(videoFile, true);
            }
            EventBus.getDefault().post(new ConversationUpdateEvent(false));
        } else if (from.equalsIgnoreCase(Constants.FROM_ROUND_TABLE)) {
            if (Utility.getDBHelper() != null) {
                // Update retry status for particular loop video in DB by local path
                Utility.getDBHelper()
                        .updateRetryStatusForLoopVideo(
                                videoFile, true
                        );
            }
            EventBus.getDefault().post(new ConversationUpdateEvent(true));
//        } else if (from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
//            if (Utility.getDBHelper() != null) {
//                Utility.getDBHelper().updateCommentRetryStatus(videoFile, true);
//            }
//            PostCommentEvent comment = new PostCommentEvent();
//            comment.isRetry = true;
//            comment.localFilePath = videoFile;
//            EventBus.getDefault().post(comment);
//        }
        }else if (isPublicOrRecordForOther()) {
            if (Utility.getDBHelper() != null) {
                Utility.getDBHelper().updateProfileRetryStatus(videoFile, true);
                if (videoConfigModel.isImageRequired && videoConfigModel.isVideoRequired) {
                    Utility.getDBHelper().updatePublicVideoStatus(videoFile, 1);
                    Utility.getDBHelper().updatePublicImageStatus(videoFile, 1);
                    //sendBroadCastForRetryPublicVideo(1, 1, 0);
                } else if (videoConfigModel.isVideoRequired) {
                    Utility.getDBHelper().updatePublicVideoStatus(videoFile, 1);
                    //sendBroadCastForRetryPublicVideo(1, 2, 0);
                } else if (videoConfigModel.isImageRequired) {
                    Utility.getDBHelper().updatePublicImageStatus(videoFile, 1);
                    //sendBroadCastForRetryPublicVideo(2, 1, 0);
                }
            }
        } else if (from.equalsIgnoreCase(Constants.FROM_PROFILE_PHOTO)) {
            sendBroadCastForProfilePhotoUploaded(true);
        }
        if (!from.equalsIgnoreCase(Constants.FROM_PROFILE_PHOTO) && !from.equalsIgnoreCase(Constants.FROM_CHANGE_COVER)) {
            //sendFailedDataDogLogs();
        }
//        if (!GenuInApplication.getInstance().appIsInForGround()) {
//            Utility.displayNotification(UploadVideoService.this, from, videoFile, videoConfigModel.convType);
//        }
    }

//    private void sendDataDogLogs() {
//        totalTime = (System.currentTimeMillis() - startTime);
//        HashMap<String, Object> map = new HashMap<String, Object>() {{
//            put("latency", totalTime);
//            put("conv_type", getDataDogFrom());
//            put("video_duration", videoConfigModel.duration);
//            put("video_size", fileSize);
//            put("video_name", fileName);
//            put("chat_id", videoConfigModel.chatId);
//        }};
//        GenuInApplication.getInstance().sendEventLogs(Constants.VIDEO_UPLOAD_DD, map);
//    }
//
//    private void sendFailedDataDogLogs() {
//        totalTime = (System.currentTimeMillis() - startTime);
//        HashMap<String, Object> map = new HashMap<String, Object>() {{
//            put("latency", totalTime);
//            put("conv_type", getDataDogFrom());
//            put("video_duration", videoConfigModel.duration);
//            put("video_size", fileSize);
//            put("video_name", fileName);
//            put("chat_id", videoConfigModel.chatId);
//            put("reason", reason);
//        }};
//        GenuInApplication.getInstance().sendEventLogs(Constants.VIDEO_UPLOAD_FAILED, map);
//    }

    private boolean isRT() {
        boolean isRT = false;
        if (from.equalsIgnoreCase(Constants.FROM_CHAT)) {
            int convType = videoConfigModel.convType;
            isRT = convType == VideoConvType.ROUND_TABLE.getValue();
        }
        return isRT || from.equalsIgnoreCase(Constants.FROM_ROUND_TABLE);
    }

    private String getDataDogFrom() {
        String fromStr = "";
        if (from.equalsIgnoreCase(Constants.FROM_PUBLIC_VIDEO) || from.equalsIgnoreCase(Constants.FROM_RECORD_FOR_OTHER)) {
            fromStr = "Public";
        } else if (from.equalsIgnoreCase(Constants.FROM_REACTION)) {
            fromStr = "Reaction";
        } else if (from.equalsIgnoreCase(Constants.FROM_DIRECT)) {
            fromStr = "Direct";
        } else if (from.equalsIgnoreCase(Constants.FROM_GROUP)) {
            fromStr = "Group";
        } else if (from.equalsIgnoreCase(Constants.FROM_ROUND_TABLE)) {
            fromStr = "RT";
        } else if (from.equalsIgnoreCase(Constants.FROM_COMMENT)) {
            fromStr = "Comment";
        } else if (from.equalsIgnoreCase(Constants.FROM_CHAT)) {
            //if (Utility.getDBHelper() != null) {
//                int convType = Utility.getDBHelper().getConvType(videoFile);
//                if (convType == 2) {
//                    fromStr = "Direct";
//                } else if (convType == 3) {
//                    fromStr = "Group";
//                } else if (convType == 4) {
//                    fromStr = "RT";
//                }
//            }
            //}
        }
        return fromStr;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}