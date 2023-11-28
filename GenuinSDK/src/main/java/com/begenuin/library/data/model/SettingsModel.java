package com.begenuin.library.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SettingsModel implements Serializable {
    private final static long serialVersionUID = -7515381838731039614L;
    @SerializedName("discoverable")
    @Expose
    private Boolean discoverable;

    @SerializedName("topic")
    @Expose
    private String topic;

    @SerializedName("contains_external_videos")
    @Expose
    private Boolean containsExternalVideos;

    @SerializedName("media_type")
    @Expose
    private String mediaType;

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

    public Boolean getDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(Boolean discoverable) {
        this.discoverable = discoverable;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}