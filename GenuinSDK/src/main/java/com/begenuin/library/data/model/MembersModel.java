package com.begenuin.library.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MembersModel implements Serializable {
    @SerializedName("member_id")
    @Expose
    private String userId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("nickname")
    @Expose
    private String nickname;

    // added username field to MembersModel
    @SerializedName("username")
    @Expose
    private String userName;
    @SerializedName("is_avatar")
    @Expose
    private boolean isAvatar = false;
    @SerializedName("profile_image")
    @Expose
    private String profileImage;
    @SerializedName("profile_image_l")
    @Expose
    private String profileImageL;
    @SerializedName("profile_image_m")
    @Expose
    private String profileImageM;
    @SerializedName("profile_image_s")
    @Expose
    private String profileImageS;
    @SerializedName("status")
    @Expose
    private String memberStatus;
    @SerializedName("role")
    @Expose
    private String memberRole;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("text")
    @Expose
    private String text;

    private String videoURL = "";
    private boolean isRemovable = true;
    private boolean isHeader = false;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isRemovable() {
        return isRemovable;
    }

    public void setRemovable(boolean removable) {
        isRemovable = removable;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvatar() {
        return isAvatar;
    }

    public void setAvatar(boolean avatar) {
        isAvatar = avatar;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileImageL() {
        return profileImageL;
    }

    public void setProfileImageL(String profileImageL) {
        this.profileImageL = profileImageL;
    }

    public String getProfileImageM() {
        return profileImageM;
    }

    public void setProfileImageM(String profileImageM) {
        this.profileImageM = profileImageM;
    }

    public String getProfileImageS() {
        return profileImageS;
    }

    public void setProfileImageS(String profileImageS) {
        this.profileImageS = profileImageS;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("memberId");
        sb.append('=');
        sb.append(((this.userId == null)?"<null>":this.userId));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("bio");
        sb.append('=');
        sb.append(((this.bio == null)?"<null>":this.bio));
        sb.append(',');
        sb.append("nickname");
        sb.append('=');
        sb.append(((this.nickname == null)?"<null>":this.nickname));
        sb.append(',');
        sb.append("phone");
        sb.append('=');
        sb.append(((this.phone == null)?"<null>":this.phone));
        sb.append(',');
        sb.append("isAvatar");
        sb.append('=');
        sb.append(this.isAvatar);
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.memberStatus == null)?"<null>":this.memberStatus));
        sb.append(',');
        sb.append("role");
        sb.append('=');
        sb.append(((this.memberRole == null)?"<null>":this.memberRole));
        sb.append(',');
        sb.append("profileImage");
        sb.append('=');
        sb.append(((this.profileImage == null)?"<null>":this.profileImage));
        sb.append(',');
        sb.append("profileImageS");
        sb.append('=');
        sb.append(((this.profileImageS == null)?"<null>":this.profileImageS));
        sb.append(',');
        sb.append("profileImageM");
        sb.append('=');
        sb.append(((this.profileImageM == null)?"<null>":this.profileImageM));
        sb.append(',');
        sb.append("profileImageL");
        sb.append('=');
        sb.append(((this.profileImageL == null)?"<null>":this.profileImageL));
        sb.append(',');
        sb.append("videoURL");
        sb.append('=');
        sb.append(((this.videoURL == null)?"<null>":this.videoURL));
        sb.append(',');
        sb.append("isRemovable");
        sb.append('=');
        sb.append(this.isRemovable);
        sb.append(',');
        sb.append("isHeader");
        sb.append('=');
        sb.append(this.isHeader);
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}
