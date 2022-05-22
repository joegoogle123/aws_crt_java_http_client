package io.github.http.adaptors;

import software.amazon.awssdk.http.async.AsyncExecuteRequest;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public final class ExecutableHttpRequest<T> {


    private final AsyncExecuteRequest asyncExecuteRequest;
    private final CompletableFuture<HttpResponse<T>> future;

    public ExecutableHttpRequest(AsyncExecuteRequest asyncExecuteRequest, CompletableFuture<HttpResponse<T>> future) {
        this.asyncExecuteRequest = asyncExecuteRequest;
        this.future = future;
    }

    public AsyncExecuteRequest getAsyncExecuteRequest() {
        return asyncExecuteRequest;
    }

    public CompletableFuture<HttpResponse<T>> getFuture() {
        return future;
    }

}
