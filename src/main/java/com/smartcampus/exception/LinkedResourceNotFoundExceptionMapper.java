package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException to an HTTP 422 Unprocessable Entity response.
 *
 * Part 5, Task 2 (10 Marks)
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("status", 422);
        errorBody.put("error", "Unprocessable Entity");
        errorBody.put("message", exception.getMessage());

        return Response.status(422)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
