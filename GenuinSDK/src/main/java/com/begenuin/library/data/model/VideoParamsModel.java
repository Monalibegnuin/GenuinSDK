package com.begenuin.library.data.model;

import java.io.Serializable;

public class VideoParamsModel implements Serializable {
    public String videoFileName = "";
    public String imageFileName = "";
    public String dpFileName = "";
    public String videoFile = "";
    public String imageFile = "";
    public String dpFile = "";
    public boolean isVideoRequired = false;
    public boolean isImageRequired = false;
    public boolean isDpRequired = false;
    public boolean isAudioRequired = false;
    public boolean isWelcomeLoop = false;
    public String link = "";
    public String duration = "";
    public String resolution = "";
    public String aspectRatio = "";
    public String size = "";
    public String selectedContacts = "";
    public String selectedQuestions = "";
    public String metaData = "";
    public String from = "";
    public String description = "";
    public String tags = "";
    public String settings = "";
    public String chatId = "";
    public String videoId = "";
    public String communityId = "";
    public String publicVideoId = ""; //This will be used in case of reaction
    public String videoURL = "";
    public String groupName = "";
    public String groupDesc = "";
    public String rtName = "";
    public String rtDesc = "";
    public String shareURL = "";
    public String qrCode = "";
    public String commentText = "";
    public String commentData = "";
    public int templateId = 0;
    public int fileType = 1;
    public int convType = 0;
    public float fileSize = 0f;
    public DiscoverModel discoverModel;
    public SettingsModel settingsModel;
//    public ChangeCoverModel changeCoverModel;
//    public ChangeProfilePhotoModel profilePhotoModel;
}
