package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource class for managing Rooms on the Smart Campus.
 * Handles CRUD operations and enforces business logic constraints.
 *
 * Part 2 (20 Marks)
 */
@Path("/api/v1/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms.
     */
    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(dataStore.getRooms().values());
        return Response.ok(rooms).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson(404, "Room Not Found",
                            "No room exists with ID: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with the location header.
     */
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson(400, "Bad Request",
                            "Room ID is required and cannot be empty."))
                    .build();
        }

        if (dataStore.getRoom(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorJson(409, "Conflict",
                            "A room with ID '" + room.getId() + "' already exists."))
                    .build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        dataStore.addRoom(room);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(createdUri).entity(room).build();
    }

    /**
     * PUT /api/v1/rooms/{roomId}
     * Updates an existing room's metadata.
     */
    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        Room existingRoom = dataStore.getRoom(roomId);
        if (existingRoom == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson(404, "Room Not Found",
                            "No room exists with ID: " + roomId))
                    .build();
        }

        // Update fields but preserve the existing sensor linkage
        existingRoom.setName(updatedRoom.getName());
        existingRoom.setCapacity(updatedRoom.getCapacity());
        dataStore.addRoom(existingRoom);

        return Response.ok(existingRoom).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room ONLY if it has no sensors assigned.
     * Business Logic: Prevents data orphans.
     *
     * @throws RoomNotEmptyException if the room still has sensors (→ 409 Conflict)
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        if (room == null) {
            // Idempotent: deleting a non-existent room returns 204
            return Response.noContent().build();
        }

        // Business rule: cannot delete a room that still has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "' because it still has " +
                    room.getSensorIds().size() + " sensor(s) assigned: " +
                    room.getSensorIds() + ". Please relocate or remove all sensors first.");
        }

        dataStore.removeRoom(roomId);
        return Response.noContent().build();
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
