package com.begenuin.library.data.model;

import static com.begenuin.library.common.Utility.getRequestBody;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;

import com.begenuine.feedscreensdk.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.core.enums.VideoConvType;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.remote.BaseAPIService;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatModel implements Serializable {
    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnailUrl;
    @SerializedName("video_url")
    @Expose
    private String videoUrl;
    @SerializedName("video_url_m3u8")
    @Expose
    private String videoUrlM3U8;
    @SerializedName("conversation_id")
    @Expose
    private String conversationId;
    @SerializedName("conversation_at")
    @Expose
    private String conversationAt;
    @SerializedName("is_read")
    @Expose
    private Boolean isRead = false;
    @SerializedName("is_reply")
    @Expose
    private Boolean isReply = false;
    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("resolution")
    @Expose
    private String resolution;
    @SerializedName("size")
    @Expose
    private String size;
    @SerializedName("aspect_ratio")
    @Expose
    private String aspectRatio;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("thumbnail_url_s")
    @Expose
    private String videoThumbnailSmall;
    @SerializedName("thumbnail_url_l")
    @Expose
    private String videoThumbnailLarge;
    @SerializedName("owner")
    @Expose
    private MembersModel owner;
    @SerializedName("share_url")
    @Expose
    private String shareURL;
    @SerializedName("no_of_views")
    @Expose
    private String noOfViews;
    @SerializedName("no_of_comments")
    @Expose
    private String noOfComments;

    @SerializedName("questions")
    @Expose
    private List<QuestionModel> questions = null;

    @SerializedName("meta_data")
    @Expose
    private MetaDataModel metaData;

    @SerializedName("repost")
    @Expose
    private RepostModel repostModel;

    @SerializedName("video_summary")
    @Expose
    private String videoSummary;

    @SerializedName("is_sparked")
    @Expose
    private boolean isSparked;

    @SerializedName("no_of_sparks")
    @Expose
    private String sparkCount = "0";

//    @SerializedName("video_thumbnail_s")
//    @Expose
//    private String videoThumbnail;

    /*
    * For Public Video ::
VideoUploadStatus => 0 - Pending, 1 - In Progress or Failed, 2 - Completed
ImageUploadStatus => 0 - Pending, 1 - In Progress or Failed, 2 - Completed
API Status => 0 - Pending or Failed,  1 - Completed
*
For Conversation Video ::
VideoUploadStatus => 0 - Pending, 1 - In Progress or Failed, 2 - Completed(video upload), 3 - API call completed.
ImageUploadStatus => 0 - Pending, 1 - In Progress or Failed, 2 - Completed
    * */

    private String chatId;
    private int videoUploadStatus;
    private int columnId;
    private String localVideoPath;
    private int isReplyReceived;
    private int isReplyOrReaction;
    private boolean isRetry;
    private String imagePath;
    private String fromStatus;
    private String firstVideoLocalPath;
    private boolean isFront;
    private int imageUploadStatus;
    private int dpUploadStatus;
    private int downloadID;
    private boolean isImage = false;
    private GroupModel group;
    private String description;
    private int convType;
    private String convShareURL;
    private String convNoOfViews;
    private boolean isEventLogged = false;
    private boolean isViewCountUpdated = false;
    private String ffMpegCommand;
    private int compressionStatus;
    private SettingsModel settings;
    private int uploadProgress;

//    public String getVideoThumbnail() {
//        return videoThumbnail;
//    }
//
//    public void setVideoThumbnail(String videoThumbnail) {
//        this.videoThumbnail = videoThumbnail;
//    }


    public boolean isSparked() {
        return isSparked;
    }

    public void setSparkStatus(boolean spark) {
        isSparked = spark;
        if (!TextUtils.isEmpty(sparkCount)) {
            long noOfSparks = Long.parseLong(sparkCount);
            if (spark) {
                noOfSparks++;
            } else {
                noOfSparks--;
            }
            sparkCount = Long.toString(noOfSparks);
        }
    }

    public String getSparkCount() {
        return sparkCount;
    }

    public void setSparkCount(String sparkCount) {
        this.sparkCount = sparkCount;
    }

    public int getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(int uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public RepostModel getRepostModel() {
        return repostModel;
    }

    public void setRepostModel(RepostModel repostModel) {
        this.repostModel = repostModel;
    }

    public SettingsModel getSettings() {
        return settings;
    }

    public void setSettings(SettingsModel settings) {
        this.settings = settings;
    }

    public int getDpUploadStatus() {
        return dpUploadStatus;
    }

    public void setDpUploadStatus(int dpUploadStatus) {
        this.dpUploadStatus = dpUploadStatus;
    }

    public String getFfMpegCommand() {
        return ffMpegCommand;
    }

    public void setFfMpegCommand(String ffMpegCommand) {
        this.ffMpegCommand = ffMpegCommand;
    }

    public int getCompressionStatus() {
        return compressionStatus;
    }

    public void setCompressionStatus(int compressionStatus) {
        this.compressionStatus = compressionStatus;
    }

    public int getDownloadID() {
        return downloadID;
    }

    public void setDownloadID(int downloadID) {
        this.downloadID = downloadID;
    }

    public MetaDataModel getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataModel metaData) {
        this.metaData = metaData;
    }

    public List<QuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionModel> questions) {
        this.questions = questions;
    }

    public String getNoOfComments() {
        return noOfComments;
    }

    public void setNoOfComments(String noOfComments) {
        this.noOfComments = noOfComments;
    }

    public String getConvShareURL() {
        return convShareURL;
    }

    public void setConvShareURL(String convShareURL) {
        this.convShareURL = convShareURL;
    }

    public String getConvNoOfViews() {
        return convNoOfViews;
    }

    public void setConvNoOfViews(String convNoOfViews) {
        this.convNoOfViews = convNoOfViews;
    }

    public boolean isViewCountUpdated() {
        return isViewCountUpdated;
    }

    public void setViewCountUpdated(boolean viewCountUpdated) {
        isViewCountUpdated = viewCountUpdated;
    }

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(String shareURL) {
        this.shareURL = shareURL;
    }

    public String getNoOfViews() {
        return noOfViews;
    }

    public void setNoOfViews(String noOfViews) {
        this.noOfViews = noOfViews;
    }

    public MembersModel getOwner() {
        return owner;
    }

    public void setOwner(MembersModel owner) {
        this.owner = owner;
    }

    public boolean isEventLogged() {
        return isEventLogged;
    }

    public void setEventLogged(boolean eventLogged) {
        isEventLogged = eventLogged;
    }

    public int getConvType() {
        return convType;
    }

    public void setConvType(int convType) {
        this.convType = convType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GroupModel getGroup() {
        return group;
    }

    public void setGroup(GroupModel group) {
        this.group = group;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getVideoThumbnailSmall() {
        return videoThumbnailSmall;
    }

    public void setVideoThumbnailSmall(String videoThumbnailSmall) {
        this.videoThumbnailSmall = videoThumbnailSmall;
    }

    public String getVideoThumbnailLarge() {
        return videoThumbnailLarge;
    }

    public void setVideoThumbnailLarge(String videoThumbnailLarge) {
        this.videoThumbnailLarge = videoThumbnailLarge;
    }

    public boolean isFront() {
        return isFront;
    }

    public void setFront(boolean front) {
        isFront = front;
    }

    public String getFirstVideoLocalPath() {
        return firstVideoLocalPath;
    }

    public void setFirstVideoLocalPath(String firstVideoLocalPath) {
        this.firstVideoLocalPath = firstVideoLocalPath;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public int getIsReplyOrReaction() {
        return isReplyOrReaction;
    }

    public void setIsReplyOrReaction(int isReplyOrReaction) {
        this.isReplyOrReaction = isReplyOrReaction;
    }

    public int getIsReplyReceived() {
        return isReplyReceived;
    }

    public void setIsReplyReceived(int isReplyReceived) {
        this.isReplyReceived = isReplyReceived;
    }

    public String getLocalVideoPath() {
        return localVideoPath;
    }

    public void setLocalVideoPath(String localVideoPath) {
        this.localVideoPath = localVideoPath;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public int getVideoUploadStatus() {
        return videoUploadStatus;
    }

    public void setVideoUploadStatus(int videoUploadStatus) {
        this.videoUploadStatus = videoUploadStatus;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoUrlM3U8() {
        return videoUrlM3U8;
    }

    public void setVideoUrlM3U8(String videoUrlM3U8) {
        this.videoUrlM3U8 = videoUrlM3U8;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationAt() {
        return conversationAt;
    }

    public void setConversationAt(String conversationAt) {
        this.conversationAt = conversationAt;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Boolean getReply() {
        return isReply;
    }

    public void setReply(Boolean reply) {
        isReply = reply;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getImageUploadStatus() {
        return imageUploadStatus;
    }

    public void setImageUploadStatus(int imageUploadStatus) {
        this.imageUploadStatus = imageUploadStatus;
    }

    public String getVideoSummary() {
        return videoSummary;
    }

    public void setVideoSummary(String videoSummary) {
        this.videoSummary = videoSummary;
    }

    public boolean isVideoAndImageUploaded() {
        return imageUploadStatus == 2 && videoUploadStatus == 2 && dpUploadStatus == 2;
    }

    public void viewVideo(Activity activity, int convType, String screenName) {
        try {
            String afTopic = "";
            String from = "";
            if (getMetaData() != null && getMetaData().getTopic() != null) {
                afTopic = Utility.getAFTopic(activity, getMetaData().getTopic());
            }
            boolean isQuestion = (getQuestions() != null && getQuestions().size() > 0);
            if (convType == VideoConvType.ROUND_TABLE.getValue()) {
                from = Constants.FROM_ROUND_TABLE;
            } else if (convType == VideoConvType.GROUP.getValue()) {
                from = Constants.FROM_GROUP;
            } else if (convType == VideoConvType.DIRECT.getValue()) {
                from = Constants.FROM_DIRECT;
            } else if (convType == VideoConvType.REACTION.getValue()) {
                from = Constants.FROM_REACTION;
            }
            //Utility.logAFVideoWatched(getChatId(), from, afTopic, isQuestion);
            //String loggedInUserId = SharedPrefUtils.getStringPreference(activity, Constants.PREF_USER);
            String loggedInUserId = "";
            if (owner != null && owner.getUserId().equalsIgnoreCase(loggedInUserId)) {
                return;
            }
            isViewCountUpdated = true;
            String module = Constants.VIEW_VIDEO;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("video_id", conversationId);
            jsonObject.put("type", 2);
            jsonObject.put("screen_name", screenName);
            new BaseAPIService(activity, module, getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    isViewCountUpdated = false;
                }

                @Override
                public void onFailure(String error) {
                    isViewCountUpdated = false;
                }
            }, "PUT", false);
        } catch (Exception e) {
            Utility.showLogException(e);
        }
    }

    public void readVideo(Activity context, String loggedInUserId) {
        if (owner == null || owner.getUserId().equalsIgnoreCase(loggedInUserId) || isRead) {
            return;
        }
        String module = Constants.READ + conversationId;
        JSONObject jsonObject = new JSONObject();

        new BaseAPIService(context, module, Utility.getRequestBody(jsonObject.toString()), true, new ResponseListener() {
            @Override
            public void onSuccess(String response) {
                try {
//                    if (Utility.getDBHelper() != null) {
//                        Utility.getDBHelper().updateReadStatus(conversationId);
//                    }
                    setRead(true);
                } catch (Exception e) {
                    Utility.showLogException(e);
                }
            }

            @Override
            public void onFailure(String error) {
                if(Utility.isNetworkAvailable(context)){
                    setRead(true);
                }
            }
        }, "POST", false);
    }

    public void downloadVideo(Activity activity) {
        String finalUrl = TextUtils.isEmpty(localVideoPath) ? videoUrl.substring(videoUrl.lastIndexOf('/') + 1) : localVideoPath.substring(localVideoPath.lastIndexOf('/') + 1);
        File destinationLocation = activity.getExternalFilesDir(Constants.MERGE_DIRECTORY);
        File dirPath = activity.getCacheDir();

        String localPath = destinationLocation.getAbsolutePath() + File.separator + finalUrl;
        String cachedPath = dirPath.getAbsolutePath() + File.separator + finalUrl;
        File localFile = new File(localPath);

        File file = new File(cachedPath);
        if (file.exists()) {
            setLocalVideoPath(file.getAbsolutePath());
        } else if (localFile.exists()) {
            setLocalVideoPath(localFile.getAbsolutePath());
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
            Date date = new Date();
            if (downloadID != 0) {
                return;
            }

//            downloadID = PRDownloader.download(videoUrl, dirPath.toString(), File.separator + finalUrl)
//                    .build()
//                    .setOnStartOrResumeListener(() -> {
//                    })
//                    .setOnPauseListener(() -> Utility.showLog("VideoA " + "FeedViewPause", "paused"))
//                    .setOnProgressListener(progress -> {
//                    })
//                    .setOnCancelListener(() -> Utility.showLog("VideoA " + "FeedViewCancel", "Cancelled"))
//                    .start(new OnDownloadListener() {
//                        @Override
//                        public void onDownloadComplete() {
//                            File file = new File(activity.getCacheDir(), finalUrl);
//                            String codecName = Utility.getCodecInfo(file.getAbsolutePath());
//                            boolean is265NotSupported = SharedPrefUtils.getBoolPreference(activity, Constants.PREF_IS_H265_FAILED);
//                            if (is265NotSupported && codecName.equalsIgnoreCase("hevc")) {
//                                // h265 video
//                                new Handler().postDelayed(() -> {
//                                    boolean isConverted = Utility.convertH265IntoH264(file.getAbsolutePath());
//                                    if (isConverted) {
//                                        String name = "converted_" + finalUrl;
//                                        File file1 = new File(activity.getCacheDir(), name);
//                                        if (file1.exists()) {
//                                            if (file.exists()) {
//                                                file.delete();
//                                            }
//                                            file1.renameTo(file);
//
//                                            setLocalVideoPath(file.getAbsolutePath());
//                                            if (Utility.getDBHelper() != null) {
//                                                Utility.getDBHelper().insertOrUpdateVideoCache(file.getName(), dateFormat.format(date), dateFormat.format(date), videoUrl, chatId);
//                                            }
//                                            VideoDownloadEvent event = new VideoDownloadEvent();
//                                            event.videoId = conversationId;
//                                            event.isSuccess = true;
//                                            EventBus.getDefault().post(event);
//                                        }
//                                    } else {
//                                        if (file.exists()) {
//                                            file.delete();
//                                        }
//                                    }
//                                }, 300);
//                            } else {
//                                setLocalVideoPath(file.getAbsolutePath());
//                                if (Utility.getDBHelper() != null) {
//                                    Utility.getDBHelper().insertOrUpdateVideoCache(file.getName(), dateFormat.format(date), dateFormat.format(date), videoUrl, chatId);
//                                }
//                                VideoDownloadEvent event = new VideoDownloadEvent();
//                                event.videoId = conversationId;
//                                event.isSuccess = true;
//                                EventBus.getDefault().post(event);
//                            }
//                        }
//
//                        @Override
//                        public void onError(Error error) {
//                            PRDownloader.resume(downloadID);
//                            VideoDownloadEvent event = new VideoDownloadEvent();
//                            event.videoId = conversationId;
//                            event.isSuccess = false;
//                            EventBus.getDefault().post(event);
//                        }
//                    });
        }
    }
}
