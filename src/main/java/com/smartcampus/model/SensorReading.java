package com.smartcampus.model;

import java.util.UUID;

/**
 * Represents a single reading event captured by a sensor.
 * Readings form a historical log for each sensor.
 */
public class SensorReading {

    private String id;        // Unique reading event ID (UUID recommended)
    private long timestamp;   // Epoch time (ms) when the reading was captured
    private double value;     // The actual metric value recorded by the hardware

    // Default constructor (required for JSON deserialization)
    public SensorReading() {
    }

    // Parameterized constructor
    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    /**
     * Factory method to create a new SensorReading with an auto-generated UUID
     * and the current timestamp.
     */
    public static SensorReading create(double value) {
        return new SensorReading(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                value
        );
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
