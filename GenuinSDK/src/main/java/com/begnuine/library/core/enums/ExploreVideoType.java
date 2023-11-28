package com.begnuine.library.core.enums;

public enum ExploreVideoType {
    PUBLIC_VIDEO("public_video"),
    RT("rt"),
    CONVERSATION_VIDEO("conversation_video"),
    COMMENT("comment"),
    LAZY_LOADING("lazy_loading"),
    END_OF_FEED("end_of_feed");
    private final String value;

    ExploreVideoType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}