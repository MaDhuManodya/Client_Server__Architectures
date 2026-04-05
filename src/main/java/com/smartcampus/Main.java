package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

/**
 * Main entry point for the Smart Campus API.
 * Starts an embedded Grizzly HTTP server with Jersey configured
 * to scan for JAX-RS resource classes.
 */
public class Main {

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";

    /**
     * Creates and configures the Grizzly HTTP server.
     *
     * @return the configured HttpServer instance
     */
    public static HttpServer startServer() {
        // Create a ResourceConfig that scans the com.smartcampus package
        // for JAX-RS resource classes, exception mappers, and providers
        final ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus.resource", "com.smartcampus.exception");

        // Create and start a new instance of Grizzly HTTP server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws Exception {
        final HttpServer server = startServer();

        System.out.println("==============================================");
        System.out.println("  Smart Campus API is running!");
        System.out.println("  Base URL : " + BASE_URI);
        System.out.println("  API Root : " + BASE_URI + "api/v1");
        System.out.println("==============================================");
        System.out.println("  Endpoints:");
        System.out.println("    GET    /api/v1              - API Discovery");
        System.out.println("    GET    /api/v1/rooms        - List all rooms");
        System.out.println("    POST   /api/v1/rooms        - Create a room");
        System.out.println("    GET    /api/v1/sensors      - List all sensors");
        System.out.println("    POST   /api/v1/sensors      - Register a sensor");
        System.out.println("==============================================");
        System.out.println("  Press ENTER to stop the server...");

        System.in.read();
        server.shutdownNow();
        System.out.println("Server stopped.");
    }
}
