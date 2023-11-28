package com.begenuin.library.data.remote;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ProgressInterceptor implements Interceptor {
    private final ProgressListener listener;

    public ProgressInterceptor(ProgressListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request progressRequest = originalRequest.newBuilder()
                .method(originalRequest.method(), new ProgressRequestBody(originalRequest.body(), listener))
                .build();

        return chain.proceed(progressRequest);
    }

    public interface ProgressListener {
        void onProgress(long bytesWritten, long contentLength, boolean done);
    }
}
