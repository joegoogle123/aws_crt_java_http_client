package io.github.http;

import io.github.http.adaptors.ExecutableHttpRequest;
import io.github.http.adaptors.WrappedHttpRequest;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class AwsCrtHttpClient extends HttpClient {

    private final AwsCrtAsyncHttpClient awsCrtAsyncHttpClient;

    public AwsCrtHttpClient(AwsCrtAsyncHttpClient.Builder builder) {
        this.awsCrtAsyncHttpClient = (AwsCrtAsyncHttpClient) builder.build();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        try {
            return this.sendAsync(request, responseBodyHandler).get();
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        WrappedHttpRequest<T> wrappedHttpRequest = new WrappedHttpRequest<>(request, responseBodyHandler);
        ExecutableHttpRequest<T> asyncRequest = wrappedHttpRequest.createExecutableHttpRequest();
        this.awsCrtAsyncHttpClient.execute(asyncRequest.getAsyncExecuteRequest());
        return asyncRequest.getFuture();
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        throw new UnsupportedOperationException("Operation with Push Promise not supported");
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return null;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }
}
