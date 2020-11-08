package com.github;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WebRequestTesterServer {

    /**
     * Launch the server.
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        final WebRequestTesterServer server = new WebRequestTesterServer();
        server.start();
        server.blockUntilShutdown();
    }

    private static final Logger logger = Logger.getLogger(WebRequestTesterServer.class.getName());

    /**
     * Current server.
     */
    private Server server;

    /**
     * Start current server
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
                WebRequestTesterServer.this.stop();
                System.err.println("*** Successfully stopped the server");
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

        }));
    }

    /**
     * Await termination.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

}
