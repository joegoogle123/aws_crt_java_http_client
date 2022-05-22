package io.github.http.adaptors;

import io.github.http.adaptors.HttpModels;
import io.github.http.adaptors.WrappedHttpRequest;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.core.internal.http.TransformingAsyncResponseHandler;
import software.amazon.awssdk.http.SdkHttpResponse;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AdaptedAsyncTransformer<T> implements TransformingAsyncResponseHandler<HttpResponse<T>> {

    private final HttpRequest httpRequest;
    private final HttpResponse.BodyHandler<T> bodyHandler;
    private final int bufferSize;

    private volatile CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();
    private volatile HttpResponse.BodySubscriber<T> bodySubscriber;
    private volatile HttpResponse.ResponseInfo responseInfo;

    public AdaptedAsyncTransformer(HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler, int bufferSize) {
        this.httpRequest = httpRequest;
        this.bodyHandler = bodyHandler;
        this.bufferSize = bufferSize;
    }

    @Override
    public CompletableFuture<HttpResponse<T>> prepare() {
        return this.future;
    }

    @Override
    public void onHeaders(SdkHttpResponse headers) {
        this.responseInfo = HttpModels.createCrtResponse(headers);
        bodySubscriber = bodyHandler.apply(this.responseInfo);
    }

    @Override
    public void onStream(Publisher<ByteBuffer> stream) {

        SdkPublisher<List<ByteBuffer>> mapped = SdkPublisher.adapt(stream).buffer(bufferSize);
        Subscriber<List<ByteBuffer>> adapted = FlowAdapters.toSubscriber(Objects.requireNonNull(this.bodySubscriber, "bodySubscriber cannot be null!"));
        mapped.subscribe(adapted);
        this.bodySubscriber.getBody().whenComplete((entry, error) -> {
            if (error != null || this.responseInfo == null) {
                this.future.completeExceptionally(error);
            } else  {
                HttpResponse<T> httpResponse = HttpModels.createHttpResponse(entry, this.httpRequest, this.responseInfo);
                this.future.complete(httpResponse);
            }
        });
    }

    @Override
    public void onError(Throwable error) {
        this.future.completeExceptionally(error);
    }
}
