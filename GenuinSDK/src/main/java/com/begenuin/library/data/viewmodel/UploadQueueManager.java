package com.begenuin.library.data.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.begenuin.library.common.Constants;
import com.begenuin.library.core.enums.VideoConvType;
import com.begenuin.library.data.model.ChatModel;
import com.begenuin.library.data.model.CommentModel;
import com.begenuin.library.data.model.DiscoverModel;
import com.begenuin.library.data.model.LoopsModel;
import com.begenuin.library.data.model.MembersModel;
import com.begenuin.library.data.model.MessageModel;
import com.begenuin.library.data.model.MetaDataModel;
import com.begenuin.library.data.model.QuestionModel;
import com.begenuin.library.data.model.SettingsModel;
import com.begenuin.library.data.model.VideoParamsModel;
import com.begenuin.library.data.remote.service.UploadVideoService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadQueueManager {
    private static UploadQueueManager mInstance;

    private final ArrayList<VideoParamsModel> pendingQueue = new ArrayList<>();

    private UploadStatus uploadStatus = UploadStatus.NONE;

    public enum UploadStatus {
        NONE,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public static UploadQueueManager getInstance() {
        if (mInstance == null) {
            mInstance = new UploadQueueManager();
        }
        return mInstance;
    }

    public void videoUploadedCallBack(Context context, VideoParamsModel videoParamsModel) {
        uploadStatus = UploadStatus.COMPLETED;
        for (int i = 0; i < pendingQueue.size(); i++) {
            String fileName = videoParamsModel.videoFile.substring(videoParamsModel.videoFile.lastIndexOf('/') + 1);
            String queueFileName = pendingQueue.get(i).videoFile.substring(pendingQueue.get(i).videoFile.lastIndexOf('/') + 1);
            if (queueFileName.equalsIgnoreCase(fileName)) {
                pendingQueue.remove(i);
                break;
            }
        }
        if (pendingQueue.size() > 0) {
            uploadVideo(context, pendingQueue.get(0));
        }
    }

    public void uploadVideo(Context context, VideoParamsModel videoParamsModel) {
        if (uploadStatus != UploadStatus.IN_PROGRESS) {
            uploadStatus = UploadStatus.IN_PROGRESS;
            Intent serviceIntent = new Intent(context, UploadVideoService.class);
            serviceIntent.putExtra("videoConfig", videoParamsModel);
            context.startService(serviceIntent);
        } else {
            pendingQueue.add(videoParamsModel);
        }
    }

    public void uploadPublicVideo(Context context, DiscoverModel discoverModel) {
        int videoUploadStatus = discoverModel.getVideoUploadStatus();
        int imageUploadStatus = discoverModel.getImageUploadStatus();
        VideoParamsModel videoParamsModel = new VideoParamsModel();
        if (TextUtils.isEmpty(discoverModel.getQrCode())) {
            videoParamsModel.from = Constants.FROM_PUBLIC_VIDEO;
        } else {
            videoParamsModel.from = Constants.FROM_RECORD_FOR_OTHER;
            videoParamsModel.qrCode = discoverModel.getQrCode();
        }
        videoParamsModel.isVideoRequired = videoUploadStatus != 2;
        videoParamsModel.isImageRequired = imageUploadStatus != 2;
        videoParamsModel.description = discoverModel.getDescription();
        videoParamsModel.tags = TextUtils.isEmpty(discoverModel.getDescription()) ? "" : getHashTagList(discoverModel.getDescription());
        videoParamsModel.link = discoverModel.getLink();
        videoParamsModel.size = discoverModel.getSize();
        videoParamsModel.duration = discoverModel.getDuration();
        videoParamsModel.aspectRatio = discoverModel.getAspectRatio();
        videoParamsModel.resolution = discoverModel.getResolution();
        String settings = new Gson().toJson(discoverModel.getSettings(), SettingsModel.class);
        videoParamsModel.settings = settings;
        videoParamsModel.selectedQuestions = discoverModel.getSelectedQuestions();
        videoParamsModel.videoFile = discoverModel.getLocalVideoPath();
        videoParamsModel.imageFile = discoverModel.getImagePath();
        uploadVideo(context, videoParamsModel);
    }

    private String getHashTagList(String desc) {
        String hashTags = "";
        try {
            Pattern MY_PATTERN_HASH = Pattern.compile("#(\\S+)");
            Matcher mat_hash;
            mat_hash = MY_PATTERN_HASH.matcher(desc);

            ArrayList<String> selectedHashList = new ArrayList<>(); // Collect strings with #
            while (mat_hash.find()) {
                String dataToAdd = mat_hash.group(1);
                selectedHashList.add(dataToAdd);
            }

            //make comma separated # text
            if (selectedHashList.size() > 0) {
                StringBuilder stringIdBuilderHash = new StringBuilder();
                for (int i = 0; i < selectedHashList.size(); i++) {
                    stringIdBuilderHash.append(selectedHashList.get(i));
                    stringIdBuilderHash.append(",");
                }
                String sbBuilderStringHash = stringIdBuilderHash.toString();
                if (!TextUtils.isEmpty(sbBuilderStringHash)) {
                    //Remove last comma
                    hashTags = sbBuilderStringHash.substring(0, sbBuilderStringHash.length() - 1);
                }
            }
            return hashTags;
        } catch (Exception e) {
            e.printStackTrace();
            return hashTags;
        }
    }

//    public void uploadComment(Context context, CommentModel comment) {
//        VideoParamsModel videoParamsModel = new VideoParamsModel();
//        videoParamsModel.from = Constants.FROM_COMMENT;
//        videoParamsModel.chatId = comment.getChatId();
//        videoParamsModel.videoId = comment.getVideoId();
//        videoParamsModel.fileType = comment.getFileType();
//        if (comment.getFileType() == CommentFileType.VIDEO.getValue()) {
//            videoParamsModel.isVideoRequired = comment.getFileUploadStatus() != 2;
//        } else if (comment.getFileType() == CommentFileType.AUDIO.getValue()) {
//            videoParamsModel.isAudioRequired = comment.getFileUploadStatus() != 2;
//        }
//        videoParamsModel.isImageRequired = comment.getImageUploadStatus() != 2;
//        videoParamsModel.link = comment.getLink();
//        videoParamsModel.duration = comment.getDuration();
//        if (comment.getQuestions() != null && comment.getQuestions().size() > 0) {
//            JSONArray jsonArray = new JSONArray();
//            for (QuestionModel question : comment.getQuestions()) {
//                jsonArray.put(question.getQuestionId());
//            }
//            videoParamsModel.selectedQuestions = jsonArray.toString();
//        }
//        if (comment.getMetaData() != null) {
//            try {
//                String metaData = new Gson().toJson(comment.getMetaData(), MetaDataModel.class);
//                videoParamsModel.metaData = metaData;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        videoParamsModel.videoFile = comment.getFileLocalVideoPath();
//        videoParamsModel.imageFile = comment.getImageLocalVideoPath();
//        uploadVideo(context, videoParamsModel);
//    }

    public void uploadReaction(Context context, ChatModel chat) {
        VideoParamsModel videoParamsModel = new VideoParamsModel();
        videoParamsModel.isVideoRequired = chat.getVideoUploadStatus() != 2;
        videoParamsModel.isImageRequired = chat.getImageUploadStatus() != 2;
        videoParamsModel.link = chat.getLink();
        videoParamsModel.duration = chat.getDuration();
        videoParamsModel.resolution = chat.getResolution();
        videoParamsModel.aspectRatio = chat.getAspectRatio();
        videoParamsModel.size = chat.getSize();
        videoParamsModel.videoFile = chat.getLocalVideoPath();
        videoParamsModel.imageFile = chat.getImagePath();
        if (chat.getQuestions() != null && chat.getQuestions().size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (QuestionModel question : chat.getQuestions()) {
                jsonArray.put(question.getQuestionId());
            }
            videoParamsModel.selectedQuestions = jsonArray.toString();
        }
        if (chat.getMetaData() != null) {
            try {
                String metaData = new Gson().toJson(chat.getMetaData(), MetaDataModel.class);
                videoParamsModel.metaData = metaData;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (chat.getConvType() == VideoConvType.REACTION.getValue()) {
            DiscoverModel discoverVO = new DiscoverModel();
            discoverVO.setVideoId(chat.getConversationId());
            discoverVO.setVideoUrl(chat.getVideoUrl());
            discoverVO.setVideoThumbnail(chat.getThumbnailUrl());
            discoverVO.setDuration(chat.getDuration());
            discoverVO.setLink(chat.getLink());
            discoverVO.setAspectRatio(chat.getAspectRatio());
            discoverVO.setSize(chat.getSize());
            discoverVO.setResolution(chat.getResolution());
            videoParamsModel.from = Constants.FROM_REACTION;
            videoParamsModel.discoverModel = discoverVO;
        } else {
            if (chat.getConvType() == VideoConvType.DIRECT.getValue()) {
                videoParamsModel.from = Constants.FROM_DIRECT;
            } else if (chat.getConvType() == VideoConvType.GROUP.getValue()) {
                videoParamsModel.from = Constants.FROM_GROUP;
            } else if (chat.getConvType() == VideoConvType.ROUND_TABLE.getValue()) {
                videoParamsModel.from = Constants.FROM_ROUND_TABLE;
            }
            if (chat.getGroup() != null && chat.getGroup().getMembers() != null && chat.getGroup().getMembers().size() > 0) {
                videoParamsModel.isDpRequired = chat.getDpUploadStatus() != 2;
                videoParamsModel.dpFile = chat.getGroup().getDp();
                Type listType = new TypeToken<List<MembersModel>>() {
                }.getType();
                String selectedContactsStr = new Gson().toJson(chat.getGroup().getMembers(), listType);
                videoParamsModel.selectedContacts = selectedContactsStr;
                if (chat.getConvType() == VideoConvType.GROUP.getValue()) {
                    videoParamsModel.groupName = TextUtils.isEmpty(chat.getGroup().getName()) ? "" : chat.getGroup().getName();
                    videoParamsModel.groupDesc = TextUtils.isEmpty(chat.getGroup().getDescription()) ? "" : chat.getGroup().getDescription();
                } else if (chat.getConvType() == VideoConvType.ROUND_TABLE.getValue()) {
                    videoParamsModel.rtName = TextUtils.isEmpty(chat.getGroup().getName()) ? "" : chat.getGroup().getName();
                    videoParamsModel.rtDesc = TextUtils.isEmpty(chat.getGroup().getDescription()) ? "" : chat.getGroup().getDescription();
                    if (chat.getSettings() != null) {
                        SettingsModel settings = new SettingsModel();
                        settings.setDiscoverable(chat.getSettings().getDiscoverable());
                        videoParamsModel.settingsModel = settings;
                    }
                }
            }
        }
        uploadVideo(context, videoParamsModel);
    }

    public void uploadLoop(Context context, LoopsModel loop) {
        if (loop.getLatestMessages() != null && loop.getLatestMessages().size() > 0) {
            MessageModel message = loop.getLatestMessages().get(0);
            VideoParamsModel videoParamsModel = new VideoParamsModel();
            videoParamsModel.from = Constants.FROM_ROUND_TABLE;
            videoParamsModel.isVideoRequired = message.getVideoUploadStatus() != 2;
            videoParamsModel.isImageRequired = message.getImageUploadStatus() != 2;
            videoParamsModel.link = message.getLink();
            videoParamsModel.videoFile = message.getLocalVideoPath();
            videoParamsModel.imageFile = message.getLocalImagePath();
            if (!TextUtils.isEmpty(loop.getCommunityId())) {
                videoParamsModel.communityId = loop.getCommunityId();
            }
            if (loop.getTemplateId() != null && loop.getTemplateId() != 0) {
                videoParamsModel.templateId = loop.getTemplateId();
            }
            if (loop.isWelcomeLoop()) {
                videoParamsModel.isWelcomeLoop = true;
            }
            if (message.getQuestions() != null && message.getQuestions().size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (QuestionModel question : message.getQuestions()) {
                    jsonArray.put(question.getQuestionId());
                }
                videoParamsModel.selectedQuestions = jsonArray.toString();
            }
            if (message.getMetaData() != null) {
                try {
                    MetaDataModel metaDataModel = message.getMetaData();
                    String metaData = new Gson().toJson(message.getMetaData(), MetaDataModel.class);
                    videoParamsModel.metaData = metaData;
                    if (!TextUtils.isEmpty(metaDataModel.getDuration())) {
                        videoParamsModel.duration = metaDataModel.getDuration();
                    }
                    if (!TextUtils.isEmpty(metaDataModel.getAspectRatio())) {
                        videoParamsModel.aspectRatio = metaDataModel.getAspectRatio();
                    }
                    if (!TextUtils.isEmpty(metaDataModel.getResolution())) {
                        videoParamsModel.resolution = metaDataModel.getResolution();
                    }
                    if (!TextUtils.isEmpty(metaDataModel.getSize())) {
                        videoParamsModel.size = metaDataModel.getSize();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (loop.getGroup() != null && loop.getGroup().getMembers() != null && loop.getGroup().getMembers().size() > 0) {
                videoParamsModel.isDpRequired = message.getDpUploadStatus() != 2;
                videoParamsModel.dpFile = loop.getGroup().getDp();
                Type listType = new TypeToken<List<MembersModel>>() {
                }.getType();
                String selectedContactsStr = new Gson().toJson(loop.getGroup().getMembers(), listType);
                videoParamsModel.selectedContacts = selectedContactsStr;
                videoParamsModel.rtName = TextUtils.isEmpty(loop.getGroup().getName()) ? "" : loop.getGroup().getName();
                videoParamsModel.rtDesc = TextUtils.isEmpty(loop.getGroup().getDescription()) ? "" : loop.getGroup().getDescription();
            }

            if (loop.getSettings() != null) {
                SettingsModel settings = new SettingsModel();
                settings.setDiscoverable(loop.getSettings().getDiscoverable());
                videoParamsModel.settingsModel = settings;
            }

            uploadVideo(context, videoParamsModel);
        }
    }

    public void uploadChat(Context context, ChatModel chat) {
        VideoParamsModel videoParamsModel = new VideoParamsModel();
        videoParamsModel.from = Constants.FROM_CHAT;
        videoParamsModel.chatId = chat.getChatId();
        videoParamsModel.isVideoRequired = chat.getVideoUploadStatus() != 2;
        videoParamsModel.isImageRequired = chat.getImageUploadStatus() != 2;
        videoParamsModel.link = chat.getLink();
        videoParamsModel.duration = chat.getDuration();
        videoParamsModel.resolution = chat.getResolution();
        videoParamsModel.aspectRatio = chat.getAspectRatio();
        videoParamsModel.size = chat.getSize();
        videoParamsModel.videoFile = chat.getLocalVideoPath();
        videoParamsModel.imageFile = chat.getImagePath();

        if (chat.getQuestions() != null && chat.getQuestions().size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (QuestionModel question : chat.getQuestions()) {
                jsonArray.put(question.getQuestionId());
            }
            videoParamsModel.selectedQuestions = jsonArray.toString();
        }
        if (chat.getMetaData() != null) {
            try {
                String metaData = new Gson().toJson(chat.getMetaData(), MetaDataModel.class);
                videoParamsModel.metaData = metaData;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        uploadVideo(context, videoParamsModel);
    }

    public void uploadLoopVideo(Context context, MessageModel messageModel) {
        VideoParamsModel videoParamsModel = new VideoParamsModel();
        videoParamsModel.from = Constants.FROM_CHAT;
        videoParamsModel.convType = VideoConvType.ROUND_TABLE.getValue();
        videoParamsModel.chatId = messageModel.getChatId();
        videoParamsModel.isVideoRequired = messageModel.getVideoUploadStatus() != 2;
        videoParamsModel.isImageRequired = messageModel.getImageUploadStatus() != 2;
        videoParamsModel.link = messageModel.getLink();
        videoParamsModel.videoFile = messageModel.getLocalVideoPath();
        videoParamsModel.imageFile = messageModel.getLocalImagePath();

        if (messageModel.getQuestions() != null && messageModel.getQuestions().size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (QuestionModel question : messageModel.getQuestions()) {
                jsonArray.put(question.getQuestionId());
            }
            videoParamsModel.selectedQuestions = jsonArray.toString();
        }
        if (messageModel.getMetaData() != null) {
            try {
                videoParamsModel.duration = messageModel.getMetaData().getDuration();
                videoParamsModel.resolution = messageModel.getMetaData().getResolution();
                videoParamsModel.aspectRatio = messageModel.getMetaData().getAspectRatio();
                videoParamsModel.size = messageModel.getMetaData().getSize();
                String metaData = new Gson().toJson(messageModel.getMetaData(), MetaDataModel.class);
                videoParamsModel.metaData = metaData;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        uploadVideo(context, videoParamsModel);
    }
}
