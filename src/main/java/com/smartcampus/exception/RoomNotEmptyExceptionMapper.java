package com.smartcampus.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException to an HTTP 409 Conflict response.
 *
 * Part 5, Task 1 (10 Marks)
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("status", 409);
        errorBody.put("error", "Conflict");
        errorBody.put("message", exception.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
