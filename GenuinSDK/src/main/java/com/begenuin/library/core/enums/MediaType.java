package com.begenuin.library.core.enums;

import com.begenuin.library.views.activities.CameraNewActivity;

public enum MediaType {
    VIDEO("video"),
    PHOTO("photo"),
    AUDIO("audio"),
    TEXT("text");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String getMediaType(final CameraNewActivity.ReplyOptions replyType) {
        String mediaType;
        switch (replyType){
            case AUDIO:
                mediaType = MediaType.AUDIO.getValue();
                break;
            case TEXT:
                mediaType = MediaType.TEXT.getValue();
                break;
//            case IMAGE:
//                mediaType = MediaType.PHOTO.getValue();
//                break;
            default:
                mediaType = MediaType.VIDEO.getValue();
                break;
        }
        return mediaType;
    }
}
