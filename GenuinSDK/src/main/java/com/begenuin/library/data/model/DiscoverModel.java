package com.begenuin.library.data.model;

import static com.begenuin.library.common.Utility.getRequestBody;
import static com.begenuin.library.common.Utility.showLogException;
import android.app.Activity;
import android.text.TextUtils;

import com.begenuin.library.common.Utility;
import com.begenuin.library.core.interfaces.OnVideoDownload;
import com.begenuin.library.core.interfaces.ExploreViewModelInterface;
import com.begenuin.library.core.interfaces.ResponseListener;
import com.begenuin.library.data.remote.BaseAPIService;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.begenuine.feedscreensdk.common.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.List;

public class DiscoverModel implements Serializable, ExploreViewModelInterface<DiscoverModel> {

    public DiscoverModel(DiscoverModel in) {
        this(in.rank, in.userId, in.videoId, in.videoThumbnail, in.videoUrl, in.savedAt, in.duration, in.link, in.description,
                in.isSaved, in.size, in.aspectRatio, in.resolution, in.shareURL, in.isAvatar, in.profileImage, in.noOfViews, in.noOfConversation, in.isFlag, in.nickName, in.name, in.bio, in.createdAt);
    }

    private int rank;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("video_id")
    @Expose
    private String videoId;
    @SerializedName("video_thumbnail")
    @Expose
    private String videoThumbnail;
    @SerializedName("video_thumbnail_s")
    @Expose
    private String videoThumbnailSmall;
    @SerializedName("video_thumbnail_l")
    @Expose
    private String videoThumbnailLarge;
    @SerializedName("video_url")
    @Expose
    private String videoUrl;
    @SerializedName("video_url_m3u8")
    @Expose
    private String videoUrlM3U8;
    @SerializedName("saved_at")
    @Expose
    private String savedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("is_saved")
    @Expose
    private Boolean isSaved = false;
    @SerializedName("size")
    @Expose
    private String size;
    @SerializedName("aspect_ratio")
    @Expose
    private String aspectRatio;
    @SerializedName("resolution")
    @Expose
    private String resolution;

    @SerializedName("share_url")
    @Expose
    private String shareURL;

    @SerializedName("is_avatar")
    @Expose
    private Boolean isAvatar = true;

    @SerializedName("profile_image")
    @Expose
    private String profileImage = "pig_face";

    @SerializedName("profile_image_s")
    @Expose
    private String profileImageS;

    @SerializedName("profile_image_m")
    @Expose
    private String profileImageM;

    @SerializedName("profile_image_l")
    @Expose
    private String profileImageL;

    @SerializedName("no_of_views")
    @Expose
    private Integer noOfViews = 0;

    @SerializedName("no_of_conversation")
    @Expose
    private Integer noOfConversation = 0;

    @SerializedName("is_flag")
    @Expose
    private Integer isFlag = 0;

    @SerializedName("user_name")
    @Expose
    private String nickName;

    @SerializedName("user_full_name")
    @Expose
    private String name;

    @SerializedName("bio")
    @Expose
    private String bio;

    @SerializedName("settings")
    @Expose
    private SettingsModel settings;

    @SerializedName("questions")
    @Expose
    private List<QuestionModel> questions = null;

    @SerializedName("recorded_by")
    @Expose
    private MembersModel recordedBy;

    @SerializedName("no_of_sparks")
    @Expose
    private String sparkCount = "0";

    @SerializedName("is_sparked")
    @Expose
    private boolean isSparked;
    private String localVideoPath;
    private int videoUploadStatus;
    private String imagePath;
    private int imageUploadStatus;
    private int apiStatus;
    private boolean isRetry;
    private int downloadID;
    private int convType;
    private String selectedContacts;
    private boolean isViewCountUpdated = false;
    private boolean isEventLogged = false;
    private boolean isSpanned = false;
    private String selectedQuestions;
    private String qrCode;
    private String ffMpegCommand;
    private int compressionStatus;
    private OnVideoDownload onVideoDownload;
    private int uploadProgress;

    public DiscoverModel() {
    }

    private long stableId;

    public DiscoverModel(int rank, String userId, String videoId, String videoThumbnail, String videoUrl,
                         String savedAt, String duration, String link, String description,
                         Boolean isSaved, String size, String aspectRatio, String resolution, String shareURL,
                         Boolean isAvatar, String profileImage, int noOfViews, int noOfConversation, int isFlag, String nickName, String name, String bio, String createdAt) {
        this.rank = rank;
        this.userId = userId;
        this.videoId = videoId;
        this.videoThumbnail = videoThumbnail;
        this.videoUrl = videoUrl;
        this.savedAt = savedAt;
        this.duration = duration;
        this.link = link;
        this.description = description;
        this.isSaved = isSaved;
        this.size = size;
        this.aspectRatio = aspectRatio;
        this.resolution = resolution;
        this.shareURL = shareURL;
        this.isAvatar = isAvatar;
        this.profileImage = profileImage;
        this.noOfViews = noOfViews;
        this.noOfConversation = noOfConversation;
        this.isFlag = isFlag;
        this.nickName = nickName;
        this.name = name;
        this.bio = bio;
        this.createdAt = createdAt;
    }

    public int getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(int uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public MembersModel getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(MembersModel recordedBy) {
        this.recordedBy = recordedBy;
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

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public List<QuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionModel> questions) {
        this.questions = questions;
    }

    public String getSelectedQuestions() {
        return selectedQuestions;
    }

    public void setSelectedQuestions(String selectedQuestions) {
        this.selectedQuestions = selectedQuestions;
    }

    public boolean isSpanned() {
        return isSpanned;
    }

    public void setSpanned(boolean spanned) {
        isSpanned = spanned;
    }

    public boolean isViewCountUpdated() {
        return isViewCountUpdated;
    }

    public void setViewCountUpdated(boolean viewCountUpdated) {
        isViewCountUpdated = viewCountUpdated;
    }

    public boolean isEventLogged() {
        return isEventLogged;
    }

    public void setEventLogged(boolean eventLogged) {
        isEventLogged = eventLogged;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getConvType() {
        return convType;
    }

    public void setConvType(int convType) {
        this.convType = convType;
    }

    public String getSelectedContacts() {
        return selectedContacts;
    }

    public void setSelectedContacts(String selectedContacts) {
        this.selectedContacts = selectedContacts;
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

    public int getApiStatus() {
        return apiStatus;
    }

    public void setApiStatus(int apiStatus) {
        this.apiStatus = apiStatus;
    }

    public String getProfileImageS() {
        return profileImageS;
    }

    public void setProfileImageS(String profileImageS) {
        this.profileImageS = profileImageS;
    }

    public String getProfileImageM() {
        return profileImageM;
    }

    public void setProfileImageM(String profileImageM) {
        this.profileImageM = profileImageM;
    }

    public String getProfileImageL() {
        return profileImageL;
    }

    public void setProfileImageL(String profileImageL) {
        this.profileImageL = profileImageL;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
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

    public String getLocalVideoPath() {
        return localVideoPath;
    }

    public void setLocalVideoPath(String localVideoPath) {
        this.localVideoPath = localVideoPath;
    }

    public int getVideoUploadStatus() {
        return videoUploadStatus;
    }

    public void setVideoUploadStatus(int videoUploadStatus) {
        this.videoUploadStatus = videoUploadStatus;
    }

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(String shareURL) {
        this.shareURL = shareURL;
    }

    public long getStableId() {
        return stableId;
    }

    public void setStableId(long stableId) {
        this.stableId = stableId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoThumbnail() {
        return videoThumbnail;
    }

    public void setVideoThumbnail(String videoThumbnail) {
        this.videoThumbnail = videoThumbnail;
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

    public String getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(String savedAt) {
        this.savedAt = savedAt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSaved() {
        return isSaved;
    }

    public void setSaved(Boolean saved) {
        isSaved = saved;
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

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Boolean getAvatar() {
        return isAvatar;
    }

    public void setAvatar(Boolean avatar) {
        isAvatar = avatar;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getNoOfViews() {
        return noOfViews;
    }

    public void setNoOfViews(int noOfViews) {
        this.noOfViews = noOfViews;
    }

    public Integer getNoOfConversation() {
        return noOfConversation;
    }

    public void setNoOfConversation(Integer noOfConversation) {
        this.noOfConversation = noOfConversation;
    }

    public Integer getIsFlag() {
        return isFlag;
    }

    public void setIsFlag(Integer isFlag) {
        this.isFlag = isFlag;
    }

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

    public SettingsModel getSettings() {
        return settings;
    }

    public void setSettings(SettingsModel settings) {
        this.settings = settings;
    }

    @Override
    public String getImageURL() {
        if (!TextUtils.isEmpty(imagePath)) {
            return imagePath;
        } else if (!TextUtils.isEmpty(videoThumbnailLarge)) {
            return videoThumbnailLarge;
        } else {
            return videoThumbnail;
        }
    }

    @Override
    public String getVideoURL() {
        return videoUrl;
    }

    @Override
    public String getVideoM3U8URL() {
        return videoUrlM3U8;
    }

    @Override
    public String getFeedThumbnail() {
        if (!TextUtils.isEmpty(imagePath)) {
            return imagePath;
        } else {
            return videoThumbnail;
        }
    }

    @Override
    public String getGridThumbnail() {
        if (!TextUtils.isEmpty(videoThumbnailLarge)) {
            return videoThumbnailLarge;
        } else {
            return videoThumbnail;
        }
    }

    @Override
    public String getFeedURL() {
        if (!TextUtils.isEmpty(localVideoPath)) {
            return localVideoPath;
        } else if (!TextUtils.isEmpty(videoUrlM3U8)) {
            return videoUrlM3U8;
        } else {
            return videoUrl;
        }
    }

    @Override
    public String getConvId() {
        return videoId;
    }

    @Override
    public String getOwnerId() {
        return userId;
    }

    @Override
    public String getFeedShareURL() {
        return shareURL;
    }

    @Override
    public String getFeedLink() {
        return link;
    }

    @Override
    public String getFeedId() {
        return videoId;
    }

    @Override
    public String getFeedNickName() {
        return nickName;
    }

    @Override
    public String getRecordedByText() {
        if(recordedBy != null){
            if(recordedBy.getUserId().equalsIgnoreCase(userId)){
                if(settings != null && settings.getContainsExternalVideos() != null && settings.getContainsExternalVideos()){
                    return "/ From camera roll";
                }else{
                    return "";
                }
            }else{
                if(settings != null && settings.getContainsExternalVideos() != null && settings.getContainsExternalVideos()){
                    return "/ From @" + recordedBy.getNickname() + "'s camera roll";
                }else{
                    return "/ From @" + recordedBy.getNickname();
                }
            }
        }else{
            return "";
        }
    }

    @Override
    public int getTotalDuration() {
        return TextUtils.isEmpty(duration) ? 0 : Integer.parseInt(duration);
    }

    @Override
    public String getCreatedAtTime() {
        return createdAt;
    }

    @Override
    public String getRepostOwnerName() {
        return null;
    }

    @Override
    public String getRepostOwnerId() {
        return null;
    }

    @Override
    public Boolean isRepostSourceAvailable() {
        return false;
    }

    @Override
    public DiscoverModel getObj() {
        return this;
    }

    public OnVideoDownload getOnVideoDownload() {
        return onVideoDownload;
    }

    public void setOnVideoDownload(OnVideoDownload onVideoDownload) {
        this.onVideoDownload = onVideoDownload;
    }

    public boolean isPendingUpload(){
        return (compressionStatus != 1 || videoUploadStatus != 2 || imageUploadStatus != 2 || apiStatus != 1);
    }

    public void saveVideo(Activity activity) {
        try {
            String module = Constants.SAVE_VIDEO;
            JSONObject jsonObject = new JSONObject();
            new BaseAPIService(activity, module + videoId, getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        boolean isSaved = dataJson.optBoolean("is_saved", true);
                        setSaved(isSaved);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    setSaved(false);
                }
            }, "POST", false);
        } catch (Exception e) {
            showLogException(e);
        }
    }

    public void unSaveVideo(Activity activity) {
        try {
            String module = Constants.UNSAVE_VIDEO;
            JSONObject jsonObject = new JSONObject();
            new BaseAPIService(activity, module + videoId, getRequestBody(jsonObject.toString()), true, new ResponseListener() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
                        boolean isSaved = dataJson.optBoolean("is_saved", false);
                        setSaved(isSaved);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    setSaved(true);
                }
            }, "POST", false);
        } catch (Exception e) {
            showLogException(e);
        }
    }

    public void viewVideo(Activity context, String screenName) {
        try {
            String afTopic = "";
            if (getSettings() != null && getSettings().getTopic() != null) {
                afTopic = Utility.getAFTopic(context, getSettings().getTopic());
            }
            boolean isQuestion = (getQuestions() != null && getQuestions().size() > 0);
            //Utility.logAFVideoWatched(getVideoId(), Constants.FROM_PUBLIC_VIDEO, afTopic, isQuestion);
            //Need to pass User Id
            //String loggedInUserId = SharedPrefUtils.getStringPreference(activity, Constants.PREF_USER);
            String loggedInUserId = "";
            if(userId.equalsIgnoreCase(loggedInUserId)){
                return;
            }
            isViewCountUpdated = true;
            String module = Constants.VIEW_VIDEO;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("video_id", videoId);
            jsonObject.put("type", 1);
            jsonObject.put("screen_name", screenName);
            new BaseAPIService(context, module, getRequestBody(jsonObject.toString()), true, new ResponseListener() {
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
            showLogException(e);
        }
    }

//    public void downloadVideo() {
//        String finalUrl = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);
//
//        File destinationLocation = activity.getExternalFilesDir(Constants.MERGE_DIRECTORY);
//        File dirPath = activity.getCacheDir();
//
//        String localPath = destinationLocation.getAbsolutePath() + File.separator + finalUrl;
//        String cachedPath = dirPath.getAbsolutePath() + File.separator + finalUrl;
//        File localFile = new File(localPath);
//
//        File file = new File(cachedPath);
//
//        if (file.exists()) {
//            setLocalVideoPath(file.getAbsolutePath());
//        } else if (localFile.exists()) {
//            setLocalVideoPath(localFile.getAbsolutePath());
//        } else {
//            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
//            Date date = new Date();
//            if (downloadID != 0) {
//                return;
//            }
//            downloadID = PRDownloader.download(videoUrl, dirPath.toString(), File.separator + finalUrl)
//                    .build()
//                    .setOnStartOrResumeListener(() -> {
////                        Utility.showLog("VideoA " + "FeedViewStarted", "Started");
//                    })
//                    .setOnPauseListener(() -> Utility.showLog("VideoA " + "FeedViewPause", "paused"))
//                    .setOnProgressListener(progress -> {
////                        Utility.showLog("VideoA " + "FeedViewProgress", String.valueOf(progress.currentBytes));
//
//                    })
//                    .setOnCancelListener(() -> Utility.showLog("VideoA " + "FeedViewCancel", "Cancelled"))
//                    .start(new OnDownloadListener() {
//                        @Override
//                        public void onDownloadComplete() {
////                            Utility.showLog("VideoA " + "FeedViewCompleted", "Downloaded");
//                            File file = new File(dirPath, finalUrl);
//                            setLocalVideoPath(file.getPath());
//                            Utility.getDBHelper().insertOrUpdateVideoCache(file.getName(), dateFormat.format(date), dateFormat.format(date), videoUrl, "");
//                        }
//
//                        @Override
//                        public void onError(Error error) {
////                            Utility.showLog("VideoA " + "FeedViewError", "Error");
//                            //PRDownloader.resume(downloadID);
//                        }
//                    });
//        }
//    }

//    public void cancelDownload() {
//        if (downloadID != 0) {
//            PRDownloader.cancel(downloadID);
//        }
//        downloadID = 0;
//    }

//    public void checkDownloadedVideo() {
//        String finalUrl = videoUrl.substring(videoUrl.lastIndexOf('/') + 1);
//        boolean videoPresentInDB = Utility.getDBHelper().getVideoCachePresent(finalUrl);
//        if (videoPresentInDB) {
//            File file = new File(activity.getCacheDir(), finalUrl);
//            if (file.exists()) {
//                setLocalVideoPath(file.getPath());
//            }
//        }
//    }
}
