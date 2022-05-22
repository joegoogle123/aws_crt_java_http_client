package io.github.http.adaptors;

import org.reactivestreams.Subscriber;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.http.async.SdkHttpContentPublisher;

import java.nio.ByteBuffer;
import java.util.Optional;

public class AdaptedSdkHttpContentPublisher implements SdkHttpContentPublisher {

    private final AsyncRequestBody asyncRequestBody;

    public AdaptedSdkHttpContentPublisher(AsyncRequestBody asyncRequestBody) {
        this.asyncRequestBody = asyncRequestBody;
    }

    @Override
    public Optional<Long> contentLength() {
        return asyncRequestBody.contentLength();
    }

    @Override
    public void subscribe(Subscriber<? super ByteBuffer> s) {
        asyncRequestBody.subscribe(s);
    }

}
