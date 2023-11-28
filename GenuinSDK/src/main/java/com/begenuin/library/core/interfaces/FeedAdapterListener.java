package com.begenuin.library.core.interfaces;

import com.begenuin.library.common.DownloadVideo;
import com.begenuin.library.data.model.DiscoverModel;

public interface FeedAdapterListener {
    void onProfileClick(int pos);

    void onVolumeClick();

    void onReplyClick();

    void onSaveClick();

    void onShareClick();

    void onLinkClick();

    void onEditClicked();

    void onMoreOptionsClicked();

    void onDownloadVideoClick(int position, DiscoverModel model, DownloadVideo downloadVideoTask);

    void onCoverPhotoClick(int position);

    void onDetailsClicked();

    void onCommentsClicked();

    void onSubscribeClicked();

    void onDescriptionClicked();

    void onOverlayClicked();

    void onUnlistedClicked();

    void onFlagVideoClicked();

    void onRepostClicked();

    void onRepostOwnerClicked(int pos);

    void onParticipateClicked();

    void onAskQuestionClicked();
    void onSparkClicked();
}