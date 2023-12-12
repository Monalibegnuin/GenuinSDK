package com.begenuin.library.common;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;

    PriorityThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable t) {
                    // Empty catch
                }
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }
}