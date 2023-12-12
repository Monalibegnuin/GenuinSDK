package com.begenuin.library.core.enums;

public enum LayerType {
    IMAGE("image"),
    FULL_IMAGE("full_image"),
    TRANSCRIBE("transcribe"),
    GIF("gif");

    private final String value;

    LayerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
