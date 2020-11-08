package com.github.web.resource.tester;

import com.github.web.resource.tester.server.WebResourceTesterServiceImpl;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.proto.web.resource.tester.TestResourceRequest;
import com.proto.web.resource.tester.TestResourceResponse;
import com.proto.web.resource.tester.WebResourceTesterServiceGrpc;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import io.grpc.testing.GrpcCleanupRule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class WebResourceTesterServerTest {

    private final Logger logger = Logger.getLogger(WebResourceTesterServerTest.class.getName());

    /**
     * This rule manages automatic graceful shutdown
     * for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final String[] webResources = new String[] {
            "google.com", "facebook.com", "youtube.com", "twitter.com", "instagram.com",
            "linkedin.com", "microsoft.com", "apple.com", "wikipedia.com", "adobe.com",
            "vimeo.com", "pinterest.com", "wordpress.com", "github.com", "amazon.com",
            "yahoo.com", "mozilla.com", "tumblr.com", "apache.com", "reddit.com"
    };

    private void runNext(StreamObserver<TestResourceResponse> streamObserver,
                         WebResourceTesterServiceGrpc.WebResourceTesterServiceStub stub,
                         int currentResourceNumber) {

        stub.testResourceAsync(TestResourceRequest.newBuilder().setResourceUri(webResources[currentResourceNumber]).build(),
                streamObserver);
    }

    @Test
    public void singleThreadBlockingCallTest() throws Exception {

        String currentServerName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder
                .forName(currentServerName).directExecutor()
                .addService(new WebResourceTesterServiceImpl()).
                        build().start());

        var blockingStub
                = WebResourceTesterServiceGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(currentServerName)
                        .directExecutor().build()));


        for (String resource: webResources) {
            TestResourceResponse requestResult = blockingStub.testResource(TestResourceRequest.newBuilder()
                    .setResourceUri(resource)
                    .build());

            assertTrue(requestResult.getStatusCode() >= 200 &&
                    requestResult.getStatusCode() < 400);

            logger.info(String.format("Gets result from %s resource with code %d and duration %d",
                    resource, requestResult.getStatusCode(), requestResult.getRequestDuration()));
        }

    }

    @Test
    public void singleThreadNonBlockingStub() throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        String currentServerName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder
                .forName(currentServerName).directExecutor()
                .addService(new WebResourceTesterServiceImpl()).
                        build().start());

        var stub
                = WebResourceTesterServiceGrpc.newStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(currentServerName)
                        .directExecutor().build()));

        StreamObserver<TestResourceResponse> observer = new StreamObserver<>() {

            private int currentResource = 0;

            @Override
            public void onNext(TestResourceResponse value) {
                assertTrue(value.getStatusCode() >= 200 &&
                        value.getStatusCode() < 400);

                logger.info(String.format("Gets result with code %d and duration %d. In thread - %d",
                        value.getStatusCode(), value.getRequestDuration(), Thread.currentThread().getId()));
            }

            @Override
            public void onError(Throwable t) {
                fail();
            }

            @Override
            public void onCompleted() {
                if(++currentResource < webResources.length)
                    stub.testResourceAsync(TestResourceRequest.newBuilder().setResourceUri(webResources[currentResource]).build(),
                            this);
                else
                    latch.countDown();
            }
        };

        stub.testResourceAsync(TestResourceRequest.newBuilder().setResourceUri(webResources[0]).build(),
                observer);

        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void multiThreadBlockingCall() throws Exception {

        String currentServerName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder
                .forName(currentServerName).directExecutor()
                .addService(new WebResourceTesterServiceImpl()).
                        build().start());

        var futureStub
                = WebResourceTesterServiceGrpc.newFutureStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(currentServerName)
                        .directExecutor().build()));


        List<ListenableFuture<TestResourceResponse>> futuresList = new ArrayList<>();

        for (String resource: webResources) {

            ListenableFuture<TestResourceResponse> futureResult = futureStub.testResourceAsync(TestResourceRequest.newBuilder()
                    .setResourceUri(resource)
                    .build());

            futuresList.add(futureResult);
        }

        for (ListenableFuture<TestResourceResponse> futureResult: futuresList) {
            TestResourceResponse response = futureResult.get();
            assertTrue(response.getStatusCode() >= 200 &&
                    response.getStatusCode() < 400);

            logger.info(String.format("Gets result with code %d and duration %d",
                    response.getStatusCode(), response.getRequestDuration()));
        }

        logger.info("Complete multiThread blocking call!");
    }

    @Test
    public void multiThreadNotBlockingCallTest() throws Exception {

        String currentServerName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder
                .forName(currentServerName).directExecutor()
                .addService(new WebResourceTesterServiceImpl()).
                        build().start());

        var futureStub
                = WebResourceTesterServiceGrpc.newFutureStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(currentServerName)
                        .directExecutor().build()));

        List<ListenableFuture<TestResourceResponse>> futuresList = new ArrayList<>();

        for (String resource: webResources) {

            ListenableFuture<TestResourceResponse> futureResult = futureStub.testResourceAsync(TestResourceRequest.newBuilder()
                    .setResourceUri(resource)
                    .build());

            futuresList.add(futureResult);
        }

        Futures.addCallback(Futures.allAsList(futuresList), new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable List<TestResourceResponse> result) {

                for (TestResourceResponse response: result) {
                    assertTrue(response.getStatusCode() >= 200 &&
                            response.getStatusCode() < 400);

                    logger.info(String.format("Gets result with code %d and duration %d",
                            response.getStatusCode(), response.getRequestDuration()));
                }
            }

            @Override
            public void onFailure(@NotNull Throwable t) {
                fail();
            }

        }, MoreExecutors.directExecutor());

        logger.info("Thread not blocker!");
    }
}
