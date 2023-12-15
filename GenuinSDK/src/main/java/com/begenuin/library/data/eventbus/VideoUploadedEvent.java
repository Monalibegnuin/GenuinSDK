package com.begenuin.library.data.eventbus;


import com.begenuin.library.data.model.DiscoverModel;
import com.begenuin.library.data.model.VideoParamsModel;

public class VideoUploadedEvent {
    public String from;
    public VideoParamsModel videoParamsModel;
    public DiscoverModel discoverModel;
    public String chatId;
    public String videoId;
}
