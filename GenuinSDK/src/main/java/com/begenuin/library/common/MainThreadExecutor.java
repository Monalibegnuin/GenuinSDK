package com.begenuin.library.common;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

class MainThreadExecutor implements Executor {

    private long millis;
    private final Handler handler = new Handler(Looper.getMainLooper());

    void setDelay(long millis) {
        this.millis = millis;
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        handler.postDelayed(runnable,millis);
    }
}