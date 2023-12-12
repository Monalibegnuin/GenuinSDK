package com.begenuin.library.data.model;

import static com.begenuin.library.common.Utility.getRequestBody;
import static com.begenuin.library.common.Utility.showLogException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;

import com.begenuin.library.common.Constants;
import com.begenuin.library.common.Utility;
import com.begenuin.library.core.interfaces.ExploreViewModelInterface;
import com.begenuin.library.core.interfaces.OnVideoShare;
import com.begenuin.library.data.remote.BaseAPIService;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentModel implements Serializable, ExploreViewModelInterface<CommentModel> {

    @SerializedName("owner")
    @Expose
    private MembersModel owner;
    @SerializedName("chat_id")
    @Expose
    private String chatId;
    @SerializedName("conversation_id")
    @Expose
    private String videoId;
    @SerializedName("comment_id")
    @Expose
    private String commentId;
    @SerializedName("type")
    @Expose
    private Integer fileType = 1;
    @SerializedName("url")
    @Expose
    private String fileURL;
    @SerializedName("video_url_m3u8")
    @Expose
    private String videoUrlM3U8;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("meta_data")
    @Expose
    private MetaDataModel metaData;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("no_of_views")
    @Expose
    private String noOfViews;
    @SerializedName("is_read")
    @Expose
    private boolean isRead = false;
    @SerializedName("questions")
    @Expose
    private List<QuestionModel> questions = null;
    @SerializedName("comment_text")
    @Expose
    private String commentText;
    @SerializedName("comment_data")
    @Expose
    private String commentData;

    @SerializedName("is_sparked")
    @Expose
    private boolean isSparked;

    @SerializedName("no_of_sparks")
    @Expose
    private String sparkCount = "0";

    private String transcript;
    private List<CommentWordModel> commentDataList = null;
    private String shareURL;
    private int fileUploadStatus;
    private int imageUploadStatus;
    private int apiStatus;
    private String fileLocalVideoPath;
    private String imageLocalVideoPath;
    private boolean isRetry;
    private int downloadID;
    private boolean isViewCountUpdated = false;
    private OnVideoShare onVideoShare;
    private String ffMpegCommand;
    private int compressionStatus;
    private int uploadProgress;
    private boolean isExpanded = false;
    private boolean isTranscriptExpanded = false;
    private boolean isTranscriptionLoading = false;
    private boolean isMediaPlay = false;
    private long seekPos = 0L;
    private double videoProgress = 0.0;
    private double maxVideoProgress = 0.0;

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

    public boolean isTranscriptionLoading() {
        return isTranscriptionLoading;
    }

    public void setTranscriptionLoading(boolean transcriptionLoading) {
        isTranscriptionLoading = transcriptionLoading;
    }

    public boolean isTranscriptExpanded() {
        return isTranscriptExpanded;
    }

    public void setTranscriptExpanded(boolean transcriptExpanded) {
        isTranscriptExpanded = transcriptExpanded;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentData() {
        return commentData;
    }

    public List<CommentWordModel> getCommentDataList() {
        return commentDataList;
    }

    public void setCommentDataList(List<CommentWordModel> commentDataList) {
        this.commentDataList = commentDataList;
    }

    public void setCommentData(String commentData) {
        this.commentData = commentData;
    }

    public void prepareCommentDataList() {
        if (!TextUtils.isEmpty(commentData)) {
            commentDataList = new ArrayList<>();
            try {
                JSONArray jDataArray = new JSONArray(commentData);
                for (int i = 0; i < jDataArray.length(); i++) {

                    if (jDataArray.get(i) instanceof String) {
                        CommentWordModel commentWordModel = new CommentWordModel();
                        commentWordModel.setContent(jDataArray.get(i));
                        commentWordModel.setMention(false);
                        commentDataList.add(commentWordModel);
                    } else if (jDataArray.get(i) instanceof JSONObject) {
                        JSONObject jObj = jDataArray.getJSONObject(i);
                        if (jObj.has("member_id")) {
                            MembersModel membersModel = new Gson().fromJson(jObj.toString(), MembersModel.class);
                            CommentWordModel commentWordMentionModel = new CommentWordModel();
                            commentWordMentionModel.setContent(membersModel);
                            commentWordMentionModel.setMention(true);
                            commentDataList.add(commentWordMentionModel);
                        } else if (jObj.has("community_id")) {
                            CommunityModel communityModel = new Gson().fromJson(jObj.toString(), CommunityModel.class);
                            CommentWordModel commentWordCommunityModel = new CommentWordModel();
                            commentWordCommunityModel.setContent(communityModel);
                            commentWordCommunityModel.setMention(true);
                            commentDataList.add(commentWordCommunityModel);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int[] audioSamples;

    public int[] getAudioSamples() {
        return audioSamples;
    }

    public void setAudioSamples(int[] audioSamples) {
        this.audioSamples = audioSamples;
    }

    public double getMaxVideoProgress() {
        return maxVideoProgress;
    }

    public void setMaxVideoProgress(double maxVideoProgress) {
        this.maxVideoProgress = maxVideoProgress;
    }

    public double getVideoProgress() {
        return videoProgress;
    }

    public void setVideoProgress(double videoProgress) {
        this.videoProgress = videoProgress;
    }

    public long getSeekPos() {
        return seekPos;
    }

    public void setSeekPos(long seekPos) {
        this.seekPos = seekPos;
    }

    public boolean isMediaPlay() {
        return isMediaPlay;
    }

    public void setMediaPlay(boolean mediaPlay) {
        isMediaPlay = mediaPlay;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public int getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(int uploadProgress) {
        this.uploadProgress = uploadProgress;
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

    public List<QuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionModel> questions) {
        this.questions = questions;
    }

    public OnVideoShare getOnVideoShare() {
        return onVideoShare;
    }

    public void setOnVideoShare(OnVideoShare onVideoShare) {
        this.onVideoShare = onVideoShare;
    }

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(String shareURL) {
        this.shareURL = shareURL;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getFileUploadStatus() {
        return fileUploadStatus;
    }

    public void setFileUploadStatus(int fileUploadStatus) {
        this.fileUploadStatus = fileUploadStatus;
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

    public String getFileLocalVideoPath() {
        return fileLocalVideoPath;
    }

    public void setFileLocalVideoPath(String fileLocalVideoPath) {
        this.fileLocalVideoPath = fileLocalVideoPath;
    }

    public String getImageLocalVideoPath() {
        return imageLocalVideoPath;
    }

    public void setImageLocalVideoPath(String imageLocalVideoPath) {
        this.imageLocalVideoPath = imageLocalVideoPath;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public boolean isViewCountUpdated() {
        return isViewCountUpdated;
    }

    public void setViewCountUpdated(boolean viewCountUpdated) {
        isViewCountUpdated = viewCountUpdated;
    }

    public MembersModel getOwner() {
        return owner;
    }

    public void setOwner(MembersModel owner) {
        this.owner = owner;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getVideoUrlM3U8() {
        return videoUrlM3U8;
    }

    public void setVideoUrlM3U8(String videoUrlM3U8) {
        this.videoUrlM3U8 = videoUrlM3U8;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public MetaDataModel getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataModel metaData) {
        this.metaData = metaData;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getNoOfViews() {
        return noOfViews;
    }

    public void setNoOfViews(String noOfViews) {
        this.noOfViews = noOfViews;
    }

    public void viewVideo(Activity activity) {
        try {
            String afTopic = "";
            if (getMetaData() != null && getMetaData().getTopic() != null) {
                afTopic = Utility.getAFTopic(activity, getMetaData().getTopic());
            }
            boolean isQuestion = (getQuestions() != null && getQuestions().size() > 0);
            //Utility.logAFVideoWatched(getChatId(), Constants.FROM_COMMENT, afTopic, isQuestion);
            isViewCountUpdated = true;
            String module = Constants.VIEW_VIDEO;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("video_id", commentId);
            jsonObject.put("type", 3);
            jsonObject.put("screen_name", Constants.SCREEN_COMMENT);
            new BaseAPIService(activity, module, getRequestBody(jsonObject.toString()), true, null, "PUT", false);
        } catch (Exception e) {
            showLogException(e);
        }
    }

    public void downloadVideo(Activity activity) {
        if (commentId.equalsIgnoreCase("-1")) {
            return;
        }

        String finalUrl = TextUtils.isEmpty(fileLocalVideoPath) ? fileURL.substring(fileURL.lastIndexOf('/') + 1) : fileLocalVideoPath.substring(fileLocalVideoPath.lastIndexOf('/') + 1);
        File destinationLocation = activity.getExternalFilesDir(Constants.MERGE_DIRECTORY);
        File dirPath = activity.getCacheDir();

        String localPath = destinationLocation.getAbsolutePath() + File.separator + finalUrl;
        String cachedPath = dirPath.getAbsolutePath() + File.separator + finalUrl;
        File localFile = new File(localPath);

        File file = new File(cachedPath);
        if (file.exists()) {
            setFileLocalVideoPath(file.getAbsolutePath());
            if (onVideoShare != null) {
                onVideoShare.onVideoReadyToShare();
            }
            onVideoShare = null;
        } else if (localFile.exists()) {
            setFileLocalVideoPath(localFile.getAbsolutePath());
            if (onVideoShare != null) {
                onVideoShare.onVideoReadyToShare();
            }
            onVideoShare = null;
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
            Date date = new Date();
            if (downloadID != 0) {
                return;
            }
//            downloadID = PRDownloader.download(fileURL, dirPath.toString(), File.separator + finalUrl)
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
//                            setFileLocalVideoPath(file.getAbsolutePath());
//                            if (Utility.getDBHelper() != null) {
//                                Utility.getDBHelper().insertOrUpdateVideoCache(file.getName(), dateFormat.format(date), dateFormat.format(date), fileURL, chatId);
//                            }
//                            if (onVideoShare != null) {
//                                onVideoShare.onVideoReadyToShare();
//                            }
//                            onVideoShare = null;
//                        }
//
//                        @Override
//                        public void onError(Error error) {
//                            PRDownloader.resume(downloadID);
//                            if (onVideoShare != null) {
//                                onVideoShare.onVideoFailedToDownload();
//                            }
//                            onVideoShare = null;
//                        }
//                    });
        }
    }

//    public void getTranscriptForFile(Activity activity, CommentsAdapter.TranscriptListener transcriptListener) {
//        try {
//            isTranscriptionLoading = true;
//            Map<String, Object> map = new HashMap<>();
//            map.put("content_id", commentId);
//            map.put("type", TranscriptionEntityType.COMMENT.getValue());
//
//            new BaseAPIService(
//                    activity,
//                    Constants.GET_TRANSCRIPTION,
//                    true,
//                    "",
//                    map,
//                    new ResponseListener() {
//                        @Override
//                        public void onSuccess(String response) {
//                            try {
//                                isTranscriptionLoading = false;
//                                JSONObject object = new JSONObject(response);
//                                JSONObject dataJson = object.getJSONObject(Constants.JSON_DATA);
//                                if (dataJson.has("transcript")) {
//                                    transcript = dataJson.optString("transcript", "");
//                                    if (transcriptListener != null) {
//                                        transcriptListener.onTranscriptionSuccess(transcript);
//                                    }
//                                } else {
//                                    if (transcriptListener != null) {
//                                        transcriptListener.onTranscriptionFailure();
//                                    }
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                if (transcriptListener != null) {
//                                    transcriptListener.onTranscriptionFailure();
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(String error) {
//                            isTranscriptionLoading = false;
//                            if (transcriptListener != null) {
//                                transcriptListener.onTranscriptionFailure();
//                            }
//                        }
//                    },
//                    "GET_DATA",
//                    false
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void cancelDownload() {
//        if (downloadID != 0) {
//            PRDownloader.cancel(downloadID);
//        }
//        downloadID = 0;
//    }

    public boolean isPendingUpload() {
        return (fileUploadStatus != 2 || imageUploadStatus != 2 || apiStatus != 2);
    }

    @Override
    public String getImageURL() {
        return thumbnail;
    }

    @Override
    public String getVideoURL() {
        return fileURL;
    }

    @Override
    public String getVideoM3U8URL() {
        return videoUrlM3U8;
    }

    @Override
    public String getFeedThumbnail() {
        return thumbnail;
    }

    @Override
    public String getGridThumbnail() {
        return thumbnail;
    }

    @Override
    public String getFeedURL() {
        return fileURL;
    }

    @Override
    public String getConvId() {
        return commentId;
    }

    @Override
    public String getOwnerId() {
        if (owner != null && !TextUtils.isEmpty(owner.getUserId())) {
            return owner.getUserId();
        } else {
            return "";
        }
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
        return commentId;
    }

    @Override
    public String getFeedNickName() {
        if (owner != null && !TextUtils.isEmpty(owner.getNickname())) {
            return owner.getNickname();
        } else {
            return "";
        }
    }

    @Override
    public String getRecordedByText() {
        if (metaData != null && metaData.getContainsExternalVideos() != null && metaData.getContainsExternalVideos()) {
            return "/ From camera roll";
        } else {
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
    public CommentModel getObj() {
        return this;
    }
}
