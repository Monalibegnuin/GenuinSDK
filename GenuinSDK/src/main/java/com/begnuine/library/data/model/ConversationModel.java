package com.begnuine.library.data.model;

import android.app.Activity;
import android.text.TextUtils;
import com.begnuine.library.common.Utility;
import com.begnuine.library.core.interfaces.ExploreViewModelInterface;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ConversationModel implements Serializable, ExploreViewModelInterface<ConversationModel> {

//    "from_status":{
//        "reaction":1,
//                "conversation":2,
//                "queue":3,
//                "dmqueue":4,
//                "deleted":5,
//                "flagged":6
//    }

    @SerializedName("chat_id")
    @Expose
    private String chatId;
    @SerializedName("from_status")
    @Expose
    private String fromStatus;
    @SerializedName("type")
    @Expose
    private int convType;
    @SerializedName("no_of_views")
    @Expose
    private String noOfViews;
    @SerializedName("share_url")
    @Expose
    private String shareURL;
    @SerializedName("is_offline")
    @Expose
    private boolean isOffline;
    @SerializedName("group")
    @Expose
    private GroupModel group;
    @SerializedName("community")
    @Expose
    private CommunityModel community;
    @SerializedName("chats")
    @Expose
    private List<ChatModel> chats = null;
    @SerializedName("questions")
    @Expose
    private List<QuestionModel> questions = null;
    @SerializedName("member_info")
    @Expose
    private MemberInfoModel memberInfo;
    @SerializedName("is_subscriber")
    @Expose
    private boolean isSubscriber;
    @SerializedName("settings")
    @Expose
    private SettingsModel settings;

    private boolean isRequestToJoinSent;

    public boolean isRequestToJoinSent() {
        return isRequestToJoinSent;
    }

    public void setRequestToJoinSent(boolean requestToJoinSent) {
        isRequestToJoinSent = requestToJoinSent;
    }

    // This list will include pending upload list to display in loop card while uploading videos
    private List<ChatModel> pendingUploadList = null;


    public CommunityModel getCommunity() {
        return community;
    }

    public void setCommunity(CommunityModel community) {
        this.community = community;
    }

    public List<ChatModel> getPendingUploadList() {
        return pendingUploadList;
    }

    public void setPendingUploadList(List<ChatModel> pendingUploadList) {
        this.pendingUploadList = pendingUploadList;
    }

    public SettingsModel getSettings() {
        return settings;
    }

    public void setSettings(SettingsModel settings) {
        this.settings = settings;
    }

    public MemberInfoModel getMemberInfo() {
        return memberInfo;
    }

    public void setMemberInfo(MemberInfoModel memberInfo) {
        this.memberInfo = memberInfo;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public void setSubscriber(boolean subscriber) {
        isSubscriber = subscriber;
    }

    public String getNoOfViews() {
        return noOfViews;
    }

    public void setNoOfViews(String noOfViews) {
        this.noOfViews = noOfViews;
    }

    public String getShareURL() {
        return shareURL;
    }

    public void setShareURL(String shareURL) {
        this.shareURL = shareURL;
    }


    public boolean isOffline() {
        return isOffline;
    }

    public List<QuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionModel> questions) {
        this.questions = questions;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public int getConvType() {
        return convType;
    }

    public void setConvType(int convType) {
        this.convType = convType;
    }

    public GroupModel getGroup() {
        return group;
    }

    public void setGroup(GroupModel group) {
        this.group = group;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public List<ChatModel> getChats() {
        return chats;
    }

    public void setChats(List<ChatModel> chats) {
        this.chats = chats;
    }

    @Override
    public String getImageURL() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getThumbnailUrl())) {
                return chat.getThumbnailUrl();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getVideoURL() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getVideoUrl())) {
                return chat.getVideoUrl();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getVideoM3U8URL() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getVideoUrlM3U8())) {
                return chat.getVideoUrlM3U8();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getFeedThumbnail() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getThumbnailUrl())) {
                return chat.getThumbnailUrl();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getGridThumbnail() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getVideoThumbnailLarge())) {
                return chat.getVideoThumbnailLarge();
            } else if (!TextUtils.isEmpty(chat.getThumbnailUrl())) {
                return chat.getThumbnailUrl();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getConvId() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getConversationId())) {
                return chat.getConversationId();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getOwnerId() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (chat.getOwner() != null && !TextUtils.isEmpty(chat.getOwner().getUserId())) {
                return chat.getOwner().getUserId();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getFeedShareURL() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getShareURL())) {
                return chat.getShareURL();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getFeedLink() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getLink())) {
                return chat.getLink();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getFeedId() {
        return chatId;
    }

    @Override
    public String getFeedNickName() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (chat.getOwner() != null && !TextUtils.isEmpty(chat.getOwner().getNickname())) {
                return chat.getOwner().getNickname();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getRecordedByText() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (chat.getMetaData() != null && chat.getMetaData().getContainsExternalVideos() != null && chat.getMetaData().getContainsExternalVideos()) {
                return "/ From camera roll";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public int getTotalDuration() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            return TextUtils.isEmpty(chat.getDuration()) ? 0 : Integer.parseInt(chat.getDuration());
        } else {
            return 0;
        }
    }

    @Override
    public String getCreatedAtTime() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            return chat.getConversationAt();
        } else {
            return "";
        }
    }

    @Override
    public String getRepostOwnerName() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (chat.getRepostModel() != null && chat.getRepostModel().getOwner() != null) {
                return chat.getRepostModel().getOwner().getNickname();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getRepostOwnerId() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (chat.getRepostModel() != null && chat.getRepostModel().getOwner() != null) {
                return chat.getRepostModel().getOwner().getUserId();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public Boolean isRepostSourceAvailable() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (chat.getRepostModel() != null) {
                return !chat.getRepostModel().isDeleted();
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public String getFeedURL() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getLocalVideoPath())) {
                return chat.getLocalVideoPath();
            } else if (!TextUtils.isEmpty(chat.getVideoUrlM3U8())) {
                return chat.getVideoUrlM3U8();
            } else {
                return chat.getVideoUrl();
            }
        } else {
            return "";
        }
    }

    @Override
    public ConversationModel getObj() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            chat.setChatId(chatId);
        }
        return this;
    }

    public String getConvViews() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getNoOfViews())) {
                return Utility.formatNumber(Long.parseLong(chat.getNoOfViews()));
            } else {
                return "0";
            }
        } else {
            return "0";
        }
    }

    public String getCommentsCount() {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getNoOfComments())) {
                return Utility.formatNumber(Long.parseLong(chat.getNoOfComments()));
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public void downloadVideoFromExplore(Activity activity) {
        if (chats != null && chats.size() > 0) {
            ChatModel chat = chats.get(chats.size() - 1);
            if (!TextUtils.isEmpty(chat.getVideoUrl())) {
                chat.downloadVideo(activity);
            }
        }
    }
}