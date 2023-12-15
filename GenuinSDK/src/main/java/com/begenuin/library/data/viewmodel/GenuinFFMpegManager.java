package com.begenuin.library.data.viewmodel;

import com.arthenica.ffmpegkit.FFmpegSession;

import java.util.HashMap;

public class GenuinFFMpegManager {

    private static GenuinFFMpegManager mInstance;
    private HashMap<String, Boolean> hashMapPaths = new HashMap<>();
    private FFmpegSession lastMergeSession;

    public static GenuinFFMpegManager getInstance() {
        if (mInstance == null) {
            mInstance = new GenuinFFMpegManager();
        }
        return mInstance;
    }

    public boolean isNeedToUpload(String path){
        return (hashMapPaths.containsKey(path) && hashMapPaths.get(path));
    }

    public void addValueToHashmap(String path, boolean isNeedToUpload){
        hashMapPaths.remove(path);
        hashMapPaths.putIfAbsent(path, isNeedToUpload);
    }

    public FFmpegSession getLastMergeSession() {
        return lastMergeSession;
    }

    public void setLastMergeSession(FFmpegSession lastMergeSession) {
        this.lastMergeSession = lastMergeSession;
    }
}
