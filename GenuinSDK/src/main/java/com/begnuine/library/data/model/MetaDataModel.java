package com.begnuine.library.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MetaDataModel implements Serializable {
    @SerializedName("topic")
    @Expose
    private String topic;

    @SerializedName("contains_external_videos")
    @Expose
    private Boolean containsExternalVideos = false;

    @SerializedName("media_type")
    @Expose
    private String mediaType;

    /**
     * Added duration, resolution, size and aspect_ration fields to MetaDataModel
     */
    @SerializedName("duration")
    @Expose
    private String duration;

    @SerializedName("resolution")
    @Expose
    private String resolution;

    @SerializedName("size")
    @Expose
    private String size;

    @SerializedName("aspect_ratio")
    @Expose
    private String aspectRatio;

    public Boolean getContainsExternalVideos() {
        return containsExternalVideos;
    }

    public void setContainsExternalVideos(Boolean containsExternalVideos) {
        this.containsExternalVideos = containsExternalVideos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
}
