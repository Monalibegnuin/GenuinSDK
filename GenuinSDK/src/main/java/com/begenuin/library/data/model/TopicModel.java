package com.begenuin.library.data.model;

public class TopicModel {
    private String name;
    private int icon;
    private int sticker;
    private int stickerSelected;
    private boolean isSelected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getSticker() {
        return sticker;
    }

    public void setSticker(int sticker) {
        this.sticker = sticker;
    }

    public int getStickerSelected() {
        return stickerSelected;
    }

    public void setStickerSelected(int stickerSelected) {
        this.stickerSelected = stickerSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
