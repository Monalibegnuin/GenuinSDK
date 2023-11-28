package com.begnuine.library.core.enums;

import java.util.HashMap;

public enum VideoConvType {
    REACTION(1),
    DIRECT(2),
    GROUP(3),
    ROUND_TABLE(4),
    PUBLIC_VIDEO(5),
    COMMENT(6);

    private final int value;
    private static final HashMap<Integer, VideoConvType> map = new HashMap<>();

    VideoConvType(int value) {
        this.value = value;
    }

    static {
        for (VideoConvType convType : VideoConvType.values()) {
            map.put(convType.value, convType);
        }
    }

    public static VideoConvType valueOf(int convType) {
        return (VideoConvType) map.get(convType);
    }

    public int getValue() {
        return value;
    }
}
