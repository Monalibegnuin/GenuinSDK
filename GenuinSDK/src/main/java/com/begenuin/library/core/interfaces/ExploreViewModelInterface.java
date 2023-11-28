package com.begenuin.library.core.interfaces;

public interface ExploreViewModelInterface<T> {
    String getImageURL();

    String getVideoURL();

    String getVideoM3U8URL();

    String getFeedThumbnail();

    String getGridThumbnail();

    String getFeedURL();

    String getConvId();

    String getOwnerId();

    String getFeedShareURL();

    String getFeedLink();

    String getFeedId();

    String getFeedNickName();

    String getRecordedByText();

    int getTotalDuration();

    String getCreatedAtTime();

    String getRepostOwnerName();

    String getRepostOwnerId();

    Boolean isRepostSourceAvailable();

    T getObj();
}