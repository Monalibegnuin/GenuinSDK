package com.begnuine.library.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CommunityModel implements Serializable {

    @SerializedName("community_id")
    @Expose
    private String communityId;
    @SerializedName("handle")
    @Expose
    private String handle;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("color_code")
    @Expose
    private String colorCode;
    @SerializedName("text_color_code")
    @Expose
    private String textColorCode;
    @SerializedName("dp")
    @Expose
    private String dp;
    @SerializedName("dp_s")
    @Expose
    private String dpS;
    @SerializedName("dp_m")
    @Expose
    private String dpM;
    @SerializedName("dp_l")
    @Expose
    private String dpL;
    @SerializedName("no_of_members")
    @Expose
    private Integer noOfMembers = 0;
    @SerializedName("no_of_loops")
    @Expose
    private Integer noOfLoops = 0;
    @SerializedName("no_of_videos")
    @Expose
    private Integer noOfVideos = 0;
    @SerializedName("share_url")
    @Expose
    private String shareUrl;

    @SerializedName("categories")
    @Expose
    private List<CommunityCategoryModel> categories = null;

    @SerializedName("moderator")
    @Expose
    private MembersModel moderator;

    // role => 1 = Leader, 2 = Member
    @SerializedName("role")
    @Expose
    private Integer role = 0;
    @SerializedName("logged_in_user_role")
    @Expose
    private Integer loggedInUserRole = 0;

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("setup")
    @Expose
    private CommunitySetupModel communitySetupModel = null;

    @SerializedName("guidelines")
    @Expose
    private List<GuideLineModel> guideLines = null;

    @SerializedName("is_selected")
    @Expose
    private boolean isSelected;

    public String twitterURL;
    public String twitterId;
    public String instaURL;
    public String instaId;
    public String linkedinId;
    public String linkedinURL;
    public String socialWebURL;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getDpS() {
        return dpS;
    }

    public void setDpS(String dpS) {
        this.dpS = dpS;
    }

    public String getDpM() {
        return dpM;
    }

    public void setDpM(String dpM) {
        this.dpM = dpM;
    }

    public String getDpL() {
        return dpL;
    }

    public void setDpL(String dpL) {
        this.dpL = dpL;
    }

    public Integer getNoOfMembers() {
        return noOfMembers;
    }

    public void setNoOfMembers(Integer noOfMembers) {
        this.noOfMembers = noOfMembers;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getLoggedInUserRole() {
        return loggedInUserRole;
    }

    public void setLoggedInUserRole(Integer loggedInUserRole) {
        this.loggedInUserRole = loggedInUserRole;
    }

    public Integer getNoOfLoops() {
        return noOfLoops;
    }

    public void setNoOfLoops(Integer noOfLoops) {
        this.noOfLoops = noOfLoops;
    }

    public Integer getNoOfVideos() {
        return noOfVideos;
    }

    public void setNoOfVideos(Integer noOfVideos) {
        this.noOfVideos = noOfVideos;
    }

    public List<CommunityCategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(List<CommunityCategoryModel> categories) {
        this.categories = categories;
    }

    public MembersModel getModerator() {
        return moderator;
    }

    public void setModerator(MembersModel moderator) {
        this.moderator = moderator;
    }

    public CommunitySetupModel getCommunitySetupModel() {
        return communitySetupModel;
    }

    public void setCommunitySetupModel(CommunitySetupModel communitySetupModel) {
        this.communitySetupModel = communitySetupModel;
    }

    public List<GuideLineModel> getGuideLines() {
        return guideLines;
    }

    public void setGuideLines(List<GuideLineModel> guideLines) {
        this.guideLines = guideLines;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}