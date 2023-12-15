package com.begenuin.library.data.eventbus;

public class PublicVideoStatusChangedEvent {
    public String videoId = "-101";
    public String videoLocalPath;
    public boolean isRetry = false;
    public int videoUploadStatus = 0;
    public int imageUploadStatus = 0;
    public int apiUploadStatus = 0;
    public int compressionStatus = 1;
}
