package io.github.http.adaptors;

import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Subscriber;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HttpModels {

    private HttpModels() {}

    public static AsyncRequestBody fromBodyPublisher(HttpRequest.BodyPublisher bodyPublisher) {
        return new CustomAsyncRequestBody(bodyPublisher);
    }

    public static <T> HttpResponse<T> createHttpResponse(T body, HttpRequest httpRequest, ResponseInfo responseInfo) {
        return new HttpResponseImpl<>(body, httpRequest, responseInfo);
    }

    public static ResponseInfo createCrtResponse(SdkHttpResponse crtResponse) {
        return new ResponseInfoImpl(crtResponse);
    }

    private static final class ResponseInfoImpl implements ResponseInfo {

        private final SdkHttpResponse crtResponse;

        public ResponseInfoImpl(SdkHttpResponse crtResponse) {
            this.crtResponse = crtResponse;
        }

        @Override
        public int statusCode() {
            return crtResponse.statusCode();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(crtResponse.headers(), (a, b) -> true);
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    private static final class HttpResponseImpl<T> implements HttpResponse<T> {

        private final T body;
        private final HttpRequest httpRequest;
        private final ResponseInfo responseInfo;

        public HttpResponseImpl(T body, HttpRequest httpRequest, ResponseInfo responseInfo) {
            this.body = body;
            this.httpRequest = httpRequest;
            this.responseInfo = responseInfo;
        }

        @Override
        public int statusCode() {
            return responseInfo.statusCode();
        }

        @Override
        public HttpRequest request() {
            return httpRequest;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return responseInfo.headers();
        }

        @Override
        public T body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return httpRequest.uri();
        }

        @Override
        public HttpClient.Version version() {
            return responseInfo.version();
        }
    }

    private static class CustomAsyncRequestBody implements AsyncRequestBody {

        private final HttpRequest.BodyPublisher bodyPublisher;

        CustomAsyncRequestBody(HttpRequest.BodyPublisher bodyPublisher) {
            this.bodyPublisher = bodyPublisher;
        }

        @Override
        public Optional<Long> contentLength() {
            return Optional.of(bodyPublisher.contentLength());
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> s) {
            FlowAdapters.toPublisher(this.bodyPublisher).subscribe(s);
        }
    }
}
