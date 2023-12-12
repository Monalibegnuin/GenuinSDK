package com.begenuin.library.data.model;

import android.util.Size;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoModel implements Serializable {

    public VideoModel() {

    }

    public VideoModel(List<VideoFileModel> videoFileList, List<VideoFileModel> videoRetakeFileList, double duration, boolean isSelected, int thumbCount, double thumbFloatCount, double actualDuration, double actualDurationWithoutSpeed, Size previewSize, double trimDuration, long trimStartMillis, long trimEndMillis, boolean isFullTrim, double fullTrimDuration, long fullTrimStartMillis, long fullTrimEndMillis, boolean isSkipMemory, boolean isFromGallery, String transcribedText) {
        this.videoFileList = videoFileList;
        this.videoRetakeFileList = videoRetakeFileList;
        this.duration = duration;
        this.isSelected = isSelected;
        this.thumbCount = thumbCount;
        this.thumbFloatCount = thumbFloatCount;
        this.actualDuration = actualDuration;
        this.actualDurationWithoutSpeed = actualDurationWithoutSpeed;
        this.previewSize = previewSize;
        this.trimDuration = trimDuration;
        this.trimStartMillis = trimStartMillis;
        this.trimEndMillis = trimEndMillis;
        this.isFullTrim = isFullTrim;
        this.fullTrimDuration = fullTrimDuration;
        this.fullTrimStartMillis = fullTrimStartMillis;
        this.fullTrimEndMillis = fullTrimEndMillis;
        this.isSkipMemory = isSkipMemory;
        this.isFromGallery = isFromGallery;
        this.transcribedText = transcribedText;
    }

    public VideoModel(VideoModel vl) {
        this(vl.videoFileList, vl.videoRetakeFileList, vl.duration, vl.isSelected, vl.thumbCount,
                vl.thumbFloatCount, vl.actualDuration, vl.actualDurationWithoutSpeed, vl.previewSize, vl.trimDuration,
                vl.trimStartMillis, vl.trimEndMillis, vl.isFullTrim, vl.fullTrimDuration, vl.fullTrimStartMillis, vl.fullTrimEndMillis,
                vl.isSkipMemory, vl.isFromGallery, vl.transcribedText);
    }

    public List<VideoFileModel> videoFileList = new ArrayList<>();
    public List<VideoFileModel> videoRetakeFileList = new ArrayList<>();
    public double duration; // file duration for recorded video
    public boolean isSelected; // current video playing
    public int thumbCount; // actual thumb count for particular video
    public double thumbFloatCount; // float thumb count is needed for last thumb adjustment
    public double actualDuration; // actual recorded duration
    public double actualDurationWithoutSpeed;
    public Size previewSize; // not needed
    public double trimDuration; // duration for trimmed video
    public long trimStartMillis; // start millis for trimmed video
    public long trimEndMillis; // end millis for trimmed video
    public boolean isFullTrim; // trim applies from main window[multi trim]
    public double fullTrimDuration; // duration for above file
    public long fullTrimStartMillis; // start trim millis for above file
    public long fullTrimEndMillis; // end trim millis for above file
    public boolean isSkipMemory;
    public boolean isFromGallery;
    public String transcribedText;
}
