package com.begnuine.library.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GroupModel implements Serializable {
    @SerializedName("group_name")
    @Expose
    private String name;
    @SerializedName("group_description")
    @Expose
    private String description;
    @SerializedName("group_id")
    @Expose
    private String groupId;
    @SerializedName("dp")
    @Expose
    private String dp;
    @SerializedName("dp_l")
    @Expose
    private String smallDp;
    @SerializedName("color_code")
    @Expose
    private String colorCode;
    @SerializedName("text_color_code")
    @Expose
    private String textColorCode;
    @SerializedName("no_of_views")
    @Expose
    private String noOfViews;
    @SerializedName("no_of_videos")
    @Expose
    private String noOfVideos;
    @SerializedName("no_of_members")
    @Expose
    private String noOfMembers;
    @SerializedName("no_of_subscribers")
    @Expose
    private String noOfSubscribers;
    @SerializedName("members")
    @Expose
    private List<MembersModel> members = null;
    @SerializedName("subscribers")
    @Expose
    private List<MembersModel> subscribers = null;
    @SerializedName("requests")
    @Expose
    private List<MembersModel> requests = null;

    private String videoURL;

    public List<MembersModel> getRequests() {
        return requests;
    }

    public void setRequests(List<MembersModel> requests) {
        this.requests = requests;
    }

    public String getNoOfViews() {
        return noOfViews;
    }

    public void setNoOfViews(String noOfViews) {
        this.noOfViews = noOfViews;
    }

    public String getNoOfVideos() {
        return noOfVideos;
    }

    public void setNoOfVideos(String noOfVideos) {
        this.noOfVideos = noOfVideos;
    }

    public String getNoOfMembers() {
        return noOfMembers;
    }

    public void setNoOfMembers(String noOfMembers) {
        this.noOfMembers = noOfMembers;
    }

    public String getNoOfSubscribers() {
        return noOfSubscribers;
    }

    public void setNoOfSubscribers(String noOfSubscribers) {
        this.noOfSubscribers = noOfSubscribers;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getSmallDp() {
        return smallDp;
    }

    public void setSmallDp(String smallDp) {
        this.smallDp = smallDp;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getTextColorCode() {
        return textColorCode;
    }

    public void setTextColorCode(String textColorCode) {
        this.textColorCode = textColorCode;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<MembersModel> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<MembersModel> subscribers) {
        this.subscribers = subscribers;
    }

    public List<MembersModel> getMembers() {
        return members;
    }

    public void setMembers(List<MembersModel> members) {
        this.members = members;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
