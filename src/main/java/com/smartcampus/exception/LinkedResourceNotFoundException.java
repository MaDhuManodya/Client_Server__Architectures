package com.smartcampus.exception;

/**
 * Thrown when a client attempts to create a Sensor with a roomId
 * that does not reference an existing Room.
 *
 * Mapped to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
