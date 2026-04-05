package com.smartcampus.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global catch-all ExceptionMapper for any unhandled exceptions.
 * Acts as a "safety net" to prevent raw Java stack traces from being
 * exposed to API consumers — a critical cybersecurity practice.
 *
 * Part 5, Task 4 (5 Marks)
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // Let JAX-RS built-in exceptions (like NotFoundException, 405, etc.)
        // pass through with their standard behaviour
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            Response originalResponse = webEx.getResponse();

            Map<String, Object> errorBody = new LinkedHashMap<>();
            errorBody.put("status", originalResponse.getStatus());
            errorBody.put("error", originalResponse.getStatusInfo().getReasonPhrase());
            errorBody.put("message", exception.getMessage());

            return Response.status(originalResponse.getStatus())
                    .entity(errorBody)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Log the exception internally for debugging (server-side only)
        System.err.println("[ERROR] Unhandled exception caught by GenericExceptionMapper:");
        System.err.println("  Type   : " + exception.getClass().getName());
        System.err.println("  Message: " + exception.getMessage());
        exception.printStackTrace(System.err);

        // Return a sanitised error response — never expose internal details
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("status", 500);
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message",
                "An unexpected error occurred on the server. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
