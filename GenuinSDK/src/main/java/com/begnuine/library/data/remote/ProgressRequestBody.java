package com.begnuine.library.data.remote;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

public class ProgressRequestBody extends RequestBody {
    private final RequestBody requestBody;
    private final ProgressInterceptor.ProgressListener listener;

    public ProgressRequestBody(RequestBody requestBody, ProgressInterceptor.ProgressListener listener) {
        this.requestBody = requestBody;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return requestBody.contentLength();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink progressSink = Okio.buffer(new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                listener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
            }
        });
        requestBody.writeTo(progressSink);
        progressSink.flush();
    }
}
