package com.smartcampus.exception;

/**
 * Thrown when a client attempts to POST a reading to a Sensor
 * that is currently in "MAINTENANCE" status.
 *
 * Mapped to HTTP 403 Forbidden.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
