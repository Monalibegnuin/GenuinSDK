package com.begenuin.library.data.viewmodel;;

import com.begenuin.library.core.enums.ExploreVideoType;
import com.begenuin.library.core.interfaces.ExploreViewModelInterface;

import java.io.Serializable;

public class ExploreViewModel<T> implements Serializable {
    public ExploreVideoType type;

    public int uniqueId;

    public boolean isVideoPlay;

    public ExploreViewModelInterface<T> modelInterface;

    public String getImageURL() {
        return modelInterface.getImageURL();
    }

    public String getFeedThumbnail() {
        return modelInterface.getFeedThumbnail();
    }

    public String getFeedURL() {
        return modelInterface.getFeedURL();
    }

    public T getObj() {
        return modelInterface.getObj();
    }

    public String getConvId() {
        return modelInterface.getConvId();
    }

    public String getUserId() {
        return modelInterface.getOwnerId();
    }

    public String getShareURL() {
        return modelInterface.getFeedShareURL();
    }

    public String getLink() {
        return modelInterface.getFeedLink();
    }

    public String getFeedId() {
        return modelInterface.getFeedId();
    }

    public String getNickName() {
        return modelInterface.getFeedNickName();
    }

    public int getTotalDuration() {
        return modelInterface.getTotalDuration();
    }

    public String getCreatedAt() {
        return modelInterface.getCreatedAtTime();
    }

    public String getGridThumbnail() {
        return modelInterface.getGridThumbnail();
    }

    public String getRepostOwnerName() {
        return modelInterface.getRepostOwnerName();
    }

    public String getRepostOwnerId(){
        return modelInterface.getRepostOwnerId();
    }

    public boolean isRepostSourceAvailable() {
        return modelInterface.isRepostSourceAvailable();
    }
}




