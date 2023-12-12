package com.begenuin.library.data.model;

import java.io.Serializable;

public class GetProfileModel implements Serializable {

    private int code;
    private String message;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable{
        private String nickname;
        private String name;
        private String bio;
        private String views;
        private int videos;
        private int replies;
        private int no_of_communities;
        private boolean is_avatar;
        private String profile_image;
        private String profile_image_s;
        private String profile_image_m;
        private String profile_image_l;
        private String share_url;
        private String share_qr_code_url;
        private String birthday;

        private String email;
        private boolean marketing_subscription;
        private boolean is_email_verified;
        private String twitter_url;
        private String twitter_id;
        private String insta_url;
        private String insta_id;
        private String linkedin_id;
        private String linkedin_url;
        private String tiktok_id;
        private String tiktok_url;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isMarketing_subscription() {
            return marketing_subscription;
        }

        public void setMarketing_subscription(boolean marketing_subscription) {
            this.marketing_subscription = marketing_subscription;
        }

        public boolean getIsEmailVerified(){return is_email_verified;}

        public void setIsEmailVerified(boolean isEmailVerified){this.is_email_verified = isEmailVerified;}

        public String getTwitter_url() {
            return twitter_url;
        }

        public void setTwitter_url(String twitter_url) {
            this.twitter_url = twitter_url;
        }

        public String getTwitter_id() {
            return twitter_id;
        }

        public void setTwitter_id(String twitter_id) {
            this.twitter_id = twitter_id;
        }

        public String getInsta_url() {
            return insta_url;
        }

        public void setInsta_url(String insta_url) {
            this.insta_url = insta_url;
        }

        public String getInsta_id() {
            return insta_id;
        }

        public void setInsta_id(String insta_id) {
            this.insta_id = insta_id;
        }

        public String getLinkedin_id() {
            return linkedin_id;
        }

        public void setLinkedin_id(String linkedin_id) {
            this.linkedin_id = linkedin_id;
        }

        public String getLinkedin_url() {
            return linkedin_url;
        }

        public void setLinkedin_url(String linkedin_url) {
            this.linkedin_url = linkedin_url;
        }

        public String getTiktok_id() {
            return tiktok_id;
        }

        public void setTiktok_id(String tiktok_id) {
            this.tiktok_id = tiktok_id;
        }

        public String getTiktok_url() {
            return tiktok_url;
        }

        public void setTiktok_url(String tiktok_url) {
            this.tiktok_url = tiktok_url;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public int getVideos() {
            return videos;
        }

        public void setVideos(int videos) {
            this.videos = videos;
        }

        public int getReplies() {
            return replies;
        }

        public void setReplies(int replies) {
            this.replies = replies;
        }

        public int getNo_of_communities() {
            return no_of_communities;
        }

        public void setNo_of_communities(int no_of_communities) {
            this.no_of_communities = no_of_communities;
        }

        public String getShare_url() {
            return share_url;
        }

        public void setShare_url(String share_url) {
            this.share_url = share_url;
        }

        public String getProfile_image_s() {
            return profile_image_s;
        }

        public void setProfile_image_s(String profile_image_s) {
            this.profile_image_s = profile_image_s;
        }

        public String getProfile_image_m() {
            return profile_image_m;
        }

        public void setProfile_image_m(String profile_image_m) {
            this.profile_image_m = profile_image_m;
        }

        public String getProfile_image_l() {
            return profile_image_l;
        }

        public void setProfile_image_l(String profile_image_l) {
            this.profile_image_l = profile_image_l;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getViews() {
            return views;
        }

        public void setViews(String views) {
            this.views = views;
        }

        public boolean isIs_avatar() {
            return is_avatar;
        }

        public void setIs_avatar(boolean is_avatar) {
            this.is_avatar = is_avatar;
        }

        public String getProfile_image() {
            return profile_image;
        }

        public void setProfile_image(String profile_image) {
            this.profile_image = profile_image;
        }

        public String getShare_qr_code_url() {
            return share_qr_code_url;
        }

        public void setShare_qr_code_url(String share_qr_code_url) {
            this.share_qr_code_url = share_qr_code_url;
        }
    }
}
