package com.begenuin.library.data.model;

import android.graphics.Bitmap;

public class VideoFileModel {

    public VideoFileModel(){

    }

    public VideoFileModel(String filePath, String fileName, boolean isFront, Bitmap bmp, float videoZoomLevel, float videoSpeed, long trimStartMillis, long trimEndMillis) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.isFront = isFront;
        this.bmp = bmp;
        this.videoZoomLevel = videoZoomLevel;
        this.videoSpeed = videoSpeed;
        this.trimStartMillis = trimStartMillis;
        this.trimEndMillis = trimEndMillis;
    }

    public VideoFileModel(VideoFileModel vfl){
        this(vfl.filePath, vfl.fileName, vfl.isFront, vfl.bmp, vfl.videoZoomLevel, vfl.videoSpeed, vfl.trimStartMillis, vfl.trimEndMillis);
    }

    public String filePath; // file path for actual video
    public String fileName; // file name for actual video
    public boolean isFront; // recorded video with front or back
    public Bitmap bmp;
    public float videoZoomLevel;
    public float videoSpeed;
    public long trimStartMillis; // start millis for trimmed video
    public long trimEndMillis; // end millis for trimmed video
}
