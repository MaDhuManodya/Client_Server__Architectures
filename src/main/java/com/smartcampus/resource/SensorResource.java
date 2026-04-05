package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource class for managing Sensors on the Smart Campus.
 * Handles CRUD, filtered retrieval, and acts as a sub-resource locator
 * for sensor readings.
 *
 * Part 3 (20 Marks) + Part 4 Sub-Resource Locator (10 Marks)
 */
@Path("/api/v1/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * Returns all sensors, with optional filtering by type query parameter.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(dataStore.getSensors().values());

        // Apply type filter if provided
        if (type != null && !type.isBlank()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns detailed data for a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson(404, "Sensor Not Found",
                            "No sensor exists with ID: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the referenced roomId exists.
     *
     * @throws LinkedResourceNotFoundException if roomId does not exist (→ 422)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson(400, "Bad Request",
                            "Sensor ID is required and cannot be empty."))
                    .build();
        }

        if (dataStore.getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorJson(409, "Conflict",
                            "A sensor with ID '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // Validate that the linked room exists
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new LinkedResourceNotFoundException(
                    "The 'roomId' field is required when registering a new sensor.");
        }

        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor: the specified roomId '" + sensor.getRoomId() +
                    "' does not exist in the system. Please create the room first.");
        }

        // Register the sensor and link it to the room
        dataStore.addSensor(sensor);
        room.addSensorId(sensor.getId());

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(createdUri).entity(sensor).build();
    }

    /**
     * PUT /api/v1/sensors/{sensorId}
     * Updates an existing sensor's metadata.
     */
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existingSensor = dataStore.getSensor(sensorId);
        if (existingSensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson(404, "Sensor Not Found",
                            "No sensor exists with ID: " + sensorId))
                    .build();
        }

        // If roomId is changing, validate the new room exists and update linkages
        if (updatedSensor.getRoomId() != null &&
                !updatedSensor.getRoomId().equals(existingSensor.getRoomId())) {

            Room newRoom = dataStore.getRoom(updatedSensor.getRoomId());
            if (newRoom == null) {
                throw new LinkedResourceNotFoundException(
                        "Cannot update sensor: the specified roomId '" +
                        updatedSensor.getRoomId() + "' does not exist.");
            }

            // Remove from old room and add to new room
            Room oldRoom = dataStore.getRoom(existingSensor.getRoomId());
            if (oldRoom != null) {
                oldRoom.removeSensorId(sensorId);
            }
            newRoom.addSensorId(sensorId);
            existingSensor.setRoomId(updatedSensor.getRoomId());
        }

        // Update other fields
        if (updatedSensor.getType() != null) existingSensor.setType(updatedSensor.getType());
        if (updatedSensor.getStatus() != null) existingSensor.setStatus(updatedSensor.getStatus());
        existingSensor.setCurrentValue(updatedSensor.getCurrentValue());

        dataStore.addSensor(existingSensor);
        return Response.ok(existingSensor).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Removes a sensor and unlinks it from its parent room.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.noContent().build();
        }

        // Unlink from the parent room
        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room != null) {
            room.removeSensorId(sensorId);
        }

        dataStore.removeSensor(sensorId);
        return Response.noContent().build();
    }

    /**
     * Sub-Resource Locator for Sensor Readings.
     * Delegates /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     *
     * Part 4: Sub-Resource Locator Pattern
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
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
