package com.github.web.resource.tester;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WebResourceTesterServer {

    private static final Logger logger = Logger.getLogger(WebResourceTesterServer.class.getName());

    /**
     * Launch the server.
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        final WebResourceTesterServer server = new WebResourceTesterServer();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Current server instance.
     */
    private Server server;

    /**
     * Start current server.
     * @throws IOException
     */
    private void start() throws IOException {

        int port = 50055;
        server = ServerBuilder.forPort(port)
                .build();
        logger.info("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {

            System.err.println("*** Received Shutdown Request");
            try {
                WebResourceTesterServer.this.stop();
                System.err.println("*** Successfully stopped the server");
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

        }));
    }

    /**
     * Await termination.
     * @throws InterruptedException
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Stop current server.
     * @throws InterruptedException
     */
    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

}
