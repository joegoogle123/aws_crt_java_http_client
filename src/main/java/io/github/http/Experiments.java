package io.github.http;

import io.github.http.adaptors.ExecutableHttpRequest;
import io.github.http.adaptors.WrappedHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Experiments {

    private static final Logger logger = LoggerFactory.getLogger(Experiments.class);

    private static final SdkAsyncHttpClient crtHttpClient = AwsCrtAsyncHttpClient.create();

    public static <T> CompletableFuture<HttpResponse<T>> executeOnCrtClient(WrappedHttpRequest<T> wrappedHttpRequest) {
        ExecutableHttpRequest<T> asyncRequest = wrappedHttpRequest.createExecutableHttpRequest();
        crtHttpClient.execute(asyncRequest.getAsyncExecuteRequest());
        return asyncRequest.getFuture();
    }

    public static void runOnJavaHttpClient() {
        HttpClient javaHttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        var request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("hello world")).uri(URI.create("https://httpbin.org/post")).build();
        CompletableFuture<HttpResponse<String>> httpClientResponse = javaHttpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        httpClientResponse.whenComplete((r,t)-> {
            if (t == null) {
                logger.info("body {}", r.body());
            }
        }).join();
    }

    public static void runOnAwsCrtHttpClient() {
        HttpClient awsCrtClient = new AwsCrtHttpClient(AwsCrtAsyncHttpClient.builder());
        var request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("hello world")).uri(URI.create("https://httpbin.org/anything")).build();
        CompletableFuture<HttpResponse<String>> httpClientResponse = awsCrtClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        httpClientResponse.whenComplete((r,t)-> {
            if (t == null) {
                logger.info("body {}", r.body());
            }
        }).join();
    }

    public static void main(String[] args) {
        runOnAwsCrtHttpClient();
    }

}
