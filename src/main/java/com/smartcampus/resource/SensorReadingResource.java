package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * Sub-resource class for managing historical readings of a specific sensor.
 * This class is instantiated by the SensorResource sub-resource locator — it does NOT
 * have a @Path annotation at the class level.
 *
 * Part 4 (20 Marks)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * Constructor receives the parent sensor ID from the sub-resource locator.
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full historical log of readings for this sensor.
     */
    @GET
    public Response getReadings() {
        List<SensorReading> sensorReadings = dataStore.getReadings(sensorId);
        return Response.ok(sensorReadings).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings/{readingId}
     * Returns a specific reading by its ID.
     */
    @GET
    @Path("/{readingId}")
    public Response getReading(@PathParam("readingId") String readingId) {
        List<SensorReading> sensorReadings = dataStore.getReadings(sensorId);
        for (SensorReading reading : sensorReadings) {
            if (reading.getId().equals(readingId)) {
                return Response.ok(reading).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorJson(404, "Reading Not Found",
                        "No reading with ID '" + readingId + "' found for sensor '" + sensorId + "'."))
                .build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to the sensor's history.
     *
     * Side Effect: Updates the parent Sensor's currentValue to maintain data consistency.
     *
     * @throws SensorUnavailableException if the sensor is in MAINTENANCE status (→ 403)
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);

        // Business rule: sensors in MAINTENANCE cannot accept new readings
        if (sensor != null && "MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot accept " +
                    "new readings. Please restore the sensor to ACTIVE status before submitting data.");
        }

        // Auto-generate ID and timestamp if not provided
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Store the reading
        dataStore.addReading(sensorId, reading);

        // Side effect: update the parent sensor's currentValue
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    /**
     * Helper to create a consistent error JSON structure.
     */
    private java.util.Map<String, Object> errorJson(int status, String error, String message) {
        java.util.Map<String, Object> err = new java.util.LinkedHashMap<>();
        err.put("status", status);
        err.put("error", error);
        err.put("message", message);
        return err;
    }
}
