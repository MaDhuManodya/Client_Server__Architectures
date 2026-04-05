package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException to an HTTP 403 Forbidden response.
 *
 * Part 5, Task 3 (5 Marks)
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("status", 403);
        errorBody.put("error", "Forbidden");
        errorBody.put("message", exception.getMessage());

        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
