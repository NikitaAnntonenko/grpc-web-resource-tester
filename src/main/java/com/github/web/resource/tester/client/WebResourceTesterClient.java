package com.github.web.resource.tester.client;

import com.proto.web.resource.tester.TestResourceRequest;
import com.proto.web.resource.tester.TestResourceResponse;
import com.proto.web.resource.tester.WebResourceTesterServiceGrpc;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WebResourceTesterClient {

    private static final Logger logger = Logger.getLogger(WebResourceTesterClient.class.getName());

    public static void main(String[] args) throws SSLException {
        logger.info("WebResourceTesterClient started ...");

        WebResourceTesterClient main = new WebResourceTesterClient();
        main.run();
    }

    private void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50055)
                .usePlaintext()
                .build();

        doUnaryCall(channel);

        logger.info("Shutting down WebResourceTesterClient channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {

        WebResourceTesterServiceGrpc.WebResourceTesterServiceBlockingStub client =
                WebResourceTesterServiceGrpc.newBlockingStub(channel)
                        .withDeadline(Deadline.after(5000, TimeUnit.MILLISECONDS));

        TestResourceResponse syncResult = client.testResource(TestResourceRequest.newBuilder()
                .setResourceUri("market.hankuper.com")
                .build());

        logger.info("Sync Google status code: " + syncResult.getStatusCode());
        logger.info(String.format("Sync Google duration millis: %d", syncResult.getRequestDuration()));

        TestResourceResponse asyncResult = client.testResource(TestResourceRequest.newBuilder()
                .setResourceUri("market.hankuper.com")
                .build());

        logger.info("Async Google status code: " + asyncResult.getStatusCode());
        logger.info(String.format("Async Google duration millis: %d", asyncResult.getRequestDuration()));

    }


}
