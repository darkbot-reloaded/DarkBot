package eu.darkbot.api.managers;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public interface BackpageManager {

    URI getInstanceURI(String query);

    <T> HttpResponse<T> sendSync(HttpRequest request,
                                 HttpResponse.BodyHandler<T> responseBody);

    <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                     HttpResponse.BodyHandler<T> responseBody);
}
