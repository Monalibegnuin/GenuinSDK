package com.begenuin.library.common;

import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
* Singleton class for default executor supplier
*/
public class DefaultExecutorSupplier {

    /*
    * thread pool executor for light weight background tasks
    */
    private final ThreadPoolExecutor mForLightWeightBackgroundTasks;

    /*
    * thread pool executor for main thread tasks
    */
    private Executor mMainThreadExecutor;
    /*
    * an instance of DefaultExecutorSupplier
    */
    private static DefaultExecutorSupplier sInstance;

    /*
    * returns the instance of DefaultExecutorSupplier
    */
    public static DefaultExecutorSupplier getInstance() {
        if (sInstance == null) {
            synchronized (DefaultExecutorSupplier.class) {
                sInstance = new DefaultExecutorSupplier();
            }
        }
        return sInstance;
    }
    /*
    * constructor for  DefaultExecutorSupplier
    */
    private DefaultExecutorSupplier() {
        // setting the thread factory
        ThreadFactory backgroundPriorityThreadFactory = new
                PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);

        // setting the thread pool executor for mForLightWeightBackgroundTasks;
        mForLightWeightBackgroundTasks = new ThreadPoolExecutor(
                4,
                4,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory
        );

        // setting the thread pool executor for mMainThreadExecutor;
        mMainThreadExecutor = new MainThreadExecutor();
    }

    /*
    * returns the thread pool executor for light weight background task
    */
    public ThreadPoolExecutor forLightWeightBackgroundTasks() {
        return mForLightWeightBackgroundTasks;
    }

    /*
    * returns the thread pool executor for main thread task
    */
    public Executor forMainThreadTasks(long millis) {
        ((MainThreadExecutor)mMainThreadExecutor).setDelay(millis);
        return mMainThreadExecutor;
    }
}
