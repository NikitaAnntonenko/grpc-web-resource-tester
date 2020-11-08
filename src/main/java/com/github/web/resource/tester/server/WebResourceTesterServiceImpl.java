package com.github.web.resource.tester.server;

import com.proto.web.resource.tester.TestResourceRequest;
import com.proto.web.resource.tester.TestResourceResponse;
import com.proto.web.resource.tester.WebResourceTesterServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebResourceTesterServiceImpl extends WebResourceTesterServiceGrpc.WebResourceTesterServiceImplBase {

    private static final Logger logger = Logger.getLogger(WebResourceTesterServiceImpl.class.getName());

    private URI getUriFrom(String uri) {
        try {
            var parsedUri = new URI(uri);
            return parsedUri.getScheme() == null
                    ? new URI("https://" + uri)
                    : parsedUri;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void testResource(TestResourceRequest request, StreamObserver<TestResourceResponse> responseObserver) {

        URI currentUri = getUriFrom(request.getResourceUri());

        if(currentUri == null) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Current uri is incorrect: " + request.getResourceUri())
                            .asRuntimeException()
            );
            responseObserver.onCompleted();
            return;
        }

        try {

            HttpRequest resourceRequest = HttpRequest.newBuilder(currentUri)
                    .GET()
                    .build();

            long beforeStartRequestMillisTime = System.currentTimeMillis();
            HttpResponse<String> resourceResponse = HttpClient.newHttpClient()
                    .send(resourceRequest, HttpResponse.BodyHandlers.ofString());
            int currentRequestDurationMillis = Math.toIntExact(System.currentTimeMillis() - beforeStartRequestMillisTime);

            responseObserver.onNext(TestResourceResponse.newBuilder()
                    .setStatusCode(resourceResponse.statusCode())
                    .setRequestDuration(currentRequestDurationMillis)
                    .build());

        } catch (IOException | InterruptedException | IllegalArgumentException e) {

            logger.log(Level.WARNING, e.getLocalizedMessage(), e);

            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        responseObserver.onCompleted();
    }

    @Override
    public void testResourceAsync(TestResourceRequest request, StreamObserver<TestResourceResponse> responseObserver) {
        URI currentUri = getUriFrom(request.getResourceUri());

        if (currentUri == null) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Current uri is incorrect: " + request.getResourceUri())
                            .asRuntimeException()
            );
            responseObserver.onCompleted();
            return;
        }

        HttpRequest resourceRequest = HttpRequest.newBuilder(currentUri)
                .GET()
                .build();

        long beforeStartRequestMillisTime = System.currentTimeMillis();
        HttpClient.newHttpClient()
                .sendAsync(resourceRequest, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, ex) -> {
                    int currentRequestDurationMillis = Math.toIntExact(System.currentTimeMillis() - beforeStartRequestMillisTime);

                    if (ex != null) {
                        logger.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        responseObserver.onError(
                                Status.INTERNAL
                                        .withDescription("Http client Request Error!")
                                        .augmentDescription(ex.getLocalizedMessage())
                                        .asRuntimeException()
                        );
                    } else {
                        responseObserver.onNext(TestResourceResponse.newBuilder()
                                .setStatusCode(response.statusCode())
                                .setRequestDuration(currentRequestDurationMillis)
                                .build());
                    }

                    responseObserver.onCompleted();
                });

    }
}
