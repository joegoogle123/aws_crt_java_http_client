package io.github.http.adaptors;


import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkHttpContentPublisher;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.concurrent.CompletableFuture;

public class WrappedHttpRequest<T> {

    private final HttpRequest httpRequest;
    private final BodyHandler<T> responseHandler;

    public WrappedHttpRequest(HttpRequest httpRequest, BodyHandler<T> responseHandler) {
        this.httpRequest = httpRequest;
        this.responseHandler = responseHandler;
    }

    public ExecutableHttpRequest<T> createExecutableHttpRequest() {

        AsyncExecuteRequest.Builder builder = AsyncExecuteRequest.builder().request(this.toCrtHttpRequest());
        AdaptedAsyncTransformer<T> asyncResponseHandler = new AdaptedAsyncTransformer<>(this.httpRequest, responseHandler, 8);
        SdkHttpContentPublisher contentPublisher = this.createSdkHttpContentPublisherAdapter();

        CompletableFuture<HttpResponse<T>> future = asyncResponseHandler.prepare();


        AsyncExecuteRequest asyncExecuteRequest = builder.responseHandler(asyncResponseHandler)
                .requestContentPublisher(contentPublisher)
                .build();

        return new ExecutableHttpRequest<>(asyncExecuteRequest, future);
    }

    public SdkHttpContentPublisher createSdkHttpContentPublisherAdapter() {
        AsyncRequestBody asyncRequestBody = httpRequest.bodyPublisher().map(HttpModels::fromBodyPublisher).orElseGet(AsyncRequestBody::empty);
        return new AdaptedSdkHttpContentPublisher(asyncRequestBody);
    }

    public SdkHttpRequest toCrtHttpRequest() {
        return SdkHttpRequest.builder().method(SdkHttpMethod.fromValue(httpRequest.method()))
                .uri(httpRequest.uri())
                .headers(httpRequest.headers().map()).build();
    }
}
