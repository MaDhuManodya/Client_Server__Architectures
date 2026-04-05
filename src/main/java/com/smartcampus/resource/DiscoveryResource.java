package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint — the API's root entry point.
 * Returns metadata about the API and HATEOAS links to primary resource collections.
 *
 * Part 1, Task 2 (5 Marks)
 */
@Path("/api/v1")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo(@Context UriInfo uriInfo) {
        String baseUri = uriInfo.getBaseUri().toString();

        Map<String, Object> apiInfo = new LinkedHashMap<>();
        apiInfo.put("name", "Smart Campus Sensor & Room Management API");
        apiInfo.put("version", "1.0");
        apiInfo.put("description",
                "RESTful API for managing rooms, sensors, and sensor readings across the university campus.");
        apiInfo.put("administrator", "Smart Campus Facilities Management");
        apiInfo.put("contact", "admin@smartcampus.westminster.ac.uk");

        // HATEOAS links — enabling client discovery of resources
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", baseUri + "api/v1");
        links.put("rooms", baseUri + "api/v1/rooms");
        links.put("sensors", baseUri + "api/v1/sensors");
        apiInfo.put("links", links);

        return Response.ok(apiInfo).build();
    }
}
