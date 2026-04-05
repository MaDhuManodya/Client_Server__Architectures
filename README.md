# Smart Campus Sensor & Room Management API

A **JAX-RS RESTful API** for managing rooms, sensors, and sensor readings across a university campus. Built with **Jersey 3.1.5** and an embedded **Grizzly HTTP server**, using in-memory data structures for persistence.

> **Module:** 5COSC022W — Client-Server Architectures  
> **Author:** [Your Name]  
> **Student ID:** [Your ID]

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [How to Build & Run](#how-to-build--run)
3. [API Endpoints](#api-endpoints)
4. [Sample curl Commands](#sample-curl-commands)
5. [Report — Question Answers](#report--question-answers)

---

## API Design Overview

### Architecture

The API follows **RESTful architectural principles** with a clear resource hierarchy reflecting the physical structure of the campus:

```
/api/v1                          → Discovery (HATEOAS entry point)
/api/v1/rooms                    → Room collection
/api/v1/rooms/{roomId}           → Individual room
/api/v1/sensors                  → Sensor collection
/api/v1/sensors/{sensorId}       → Individual sensor
/api/v1/sensors/{sensorId}/readings  → Sensor reading sub-resource
```

### Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | JAX-RS (Jakarta RESTful Web Services) |
| Implementation | Jersey 3.1.5 |
| HTTP Server | Grizzly 2 (embedded) |
| JSON Serialization | Jackson (via jersey-media-json-jackson) |
| Build Tool | Apache Maven |
| Data Storage | In-memory (ConcurrentHashMap) |

### Data Models

- **Room**: Represents a physical room (`id`, `name`, `capacity`, `sensorIds`)
- **Sensor**: Represents a sensor device (`id`, `type`, `status`, `currentValue`, `roomId`)
- **SensorReading**: Represents a historical reading event (`id`, `timestamp`, `value`)

### Error Handling Strategy

| Exception | HTTP Code | Scenario |
|-----------|-----------|----------|
| `RoomNotEmptyException` | 409 Conflict | Deleting a room with active sensors |
| `LinkedResourceNotFoundException` | 422 Unprocessable Entity | Creating a sensor with invalid roomId |
| `SensorUnavailableException` | 403 Forbidden | Posting a reading to a MAINTENANCE sensor |
| `GenericExceptionMapper` | 500 Internal Server Error | Catch-all for unexpected errors |

---

## How to Build & Run

### Prerequisites

- **Java 17+** (tested with Java 25)
- **Maven 3.8+**

### Step 1: Clone the Repository

```bash
git clone https://github.com/[your-username]/Smart_Campus_API.git
cd Smart_Campus_API
```

### Step 2: Build the Project

```bash
mvn clean package
```

This compiles the source code, runs any tests, and creates an executable fat JAR using the Maven Shade Plugin.

### Step 3: Run the Server

```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

The server will start on **http://localhost:8080/**. You should see:

```
==============================================
  Smart Campus API is running!
  Base URL : http://localhost:8080/
  API Root : http://localhost:8080/api/v1
==============================================
```

### Step 4: Test the API

Open another terminal and use `curl` or Postman to interact with the endpoints.

---

## API Endpoints

### Discovery

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1` | API metadata and HATEOAS links |

### Rooms

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| PUT | `/api/v1/rooms/{roomId}` | Update a room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (if no sensors) |

### Sensors

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor |
| PUT | `/api/v1/sensors/{sensorId}` | Update a sensor |
| DELETE | `/api/v1/sensors/{sensorId}` | Remove a sensor |

### Sensor Readings (Sub-Resource)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading |

---

## Sample curl Commands

### 1. API Discovery

```bash
curl -s http://localhost:8080/api/v1 | json_pp
```

### 2. List All Rooms

```bash
curl -s http://localhost:8080/api/v1/rooms | json_pp
```

### 3. Create a New Room

```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"MED-101","name":"Medical Sciences Lab","capacity":25}' | json_pp
```

### 4. Get a Specific Room

```bash
curl -s http://localhost:8080/api/v1/rooms/LIB-301 | json_pp
```

### 5. Register a New Sensor (with valid roomId)

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"HUMIDITY-001","type":"Humidity","status":"ACTIVE","currentValue":45.2,"roomId":"LIB-301"}' | json_pp
```

### 6. Filter Sensors by Type

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=Temperature" | json_pp
```

### 7. Post a Sensor Reading

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}' | json_pp
```

### 8. Get Sensor Reading History

```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-001/readings | json_pp
```

### 9. Attempt to Delete a Room with Sensors (triggers 409)

```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301 | json_pp
```

### 10. Register Sensor with Invalid roomId (triggers 422)

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"FAKE-001","type":"Wind","status":"ACTIVE","currentValue":0,"roomId":"NONEXISTENT"}' | json_pp
```

### 11. Post Reading to MAINTENANCE Sensor (triggers 403)

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":19.5}' | json_pp
```

---

## Report — Question Answers

### Part 1: Service Architecture & Setup

#### Q1: Default Lifecycle of a JAX-RS Resource Class

In JAX-RS, the default lifecycle of a resource class is **per-request**. This means a **new instance of the resource class is created for every incoming HTTP request** and is discarded after the response is sent. The runtime does NOT treat it as a singleton by default.

This architectural decision has significant implications for managing in-memory data structures:

- Since each request creates a new resource instance, any data stored as **instance variables** would be lost between requests. Therefore, shared state (such as our maps of rooms and sensors) must be stored **externally** to the resource class.
- In this implementation, we use a **Singleton `DataStore` class** that holds all our `ConcurrentHashMap` collections. Each resource instance accesses the same `DataStore.getInstance()`, ensuring data consistency.
- We use **`ConcurrentHashMap`** and **`CopyOnWriteArrayList`** instead of regular `HashMap`/`ArrayList` to prevent **race conditions** when multiple requests attempt to read and write data simultaneously. Without thread-safe collections, concurrent requests could cause data corruption, lost updates, or `ConcurrentModificationException` errors.

#### Q2: Hypermedia (HATEOAS) and Its Benefits

**HATEOAS** (Hypermedia As The Engine Of Application State) is considered a hallmark of advanced RESTful design because it makes the API **self-describing and navigable**. In our Discovery endpoint (`GET /api/v1`), we return links like:

```json
{
  "links": {
    "self": "http://localhost:8080/api/v1",
    "rooms": "http://localhost:8080/api/v1/rooms",
    "sensors": "http://localhost:8080/api/v1/sensors"
  }
}
```

**Benefits over static documentation:**

1. **Client Decoupling**: Clients do not need to hard-code URLs. They discover endpoints dynamically by following links, meaning the server can evolve its URL structure without breaking clients.
2. **Reduced Documentation Dependency**: New developers can explore the API by simply following links, reducing reliance on external documentation that may become stale.
3. **State-Driven Navigation**: The server can conditionally include or exclude links based on the current state of a resource (e.g., only showing a "delete" link when deletion is permitted), guiding clients toward valid actions.
4. **Evolvability**: The API can add new resources or change paths without requiring client updates, as long as clients follow links rather than constructing URLs manually.

---

### Part 2: Room Management

#### Q3: Returning Full Objects vs. IDs Only

When returning a list of rooms, there are trade-offs between returning full objects versus only IDs:

| Aspect | Full Objects | IDs Only |
|--------|-------------|----------|
| **Network Bandwidth** | Higher — each response includes all fields for every room | Lower — only a list of short string identifiers |
| **Client-Side Processing** | Simpler — the client has all data immediately | More complex — the client must make additional `GET /rooms/{id}` requests for each room's details |
| **Number of Requests** | 1 request to get everything | 1 + N requests (one for the list, one per room) |
| **Latency** | Lower overall latency | Higher cumulative latency due to multiple round-trips |
| **Scalability** | Can become problematic with thousands of rooms (large payloads) | Better for very large collections, especially with pagination |

In this implementation, we return **full room objects** because the dataset is relatively small (campus rooms), and it provides a better developer experience by reducing the number of API calls needed.

#### Q4: Idempotency of DELETE

Yes, the `DELETE` operation in our implementation is **idempotent**. Idempotency means that making the same request multiple times produces the same result as making it once.

Here is what happens if a client sends the same `DELETE /api/v1/rooms/LIB-301` request multiple times:

1. **First request**: The room exists → it is deleted → HTTP `204 No Content` is returned.
2. **Second request (and subsequent)**: The room no longer exists → the method detects this and returns HTTP `204 No Content` without any error.

The server does not return a `404 Not Found` for repeated deletions because that would mean the response changes between the first and subsequent calls, which would violate idempotency. By consistently returning `204`, the client can safely retry delete operations without worrying about errors — which is especially important in unreliable network environments where a response may be lost and the client cannot be sure if the first request succeeded.

---

### Part 3: Sensor Operations & Linking

#### Q5: @Consumes and Content-Type Mismatch

When we annotate a `POST` method with `@Consumes(MediaType.APPLICATION_JSON)`, we are explicitly declaring that the method **only accepts** `application/json` content.

If a client sends data in a different format (e.g., `text/plain` or `application/xml`):

1. **JAX-RS automatically rejects the request** before it even reaches our method code.
2. The framework returns an **HTTP 415 Unsupported Media Type** response.
3. Our business logic code is **never executed** — the framework handles this at the container level.

This is a powerful feature because it acts as an automatic input validation layer. Without `@Consumes`, the framework might attempt to deserialize incompatible data, leading to cryptic runtime errors. The annotation ensures a clear, standards-compliant error response that immediately tells the client developer what format is expected.

#### Q6: @QueryParam vs. Path-Based Filtering

We implement sensor type filtering using `@QueryParam`:
```
GET /api/v1/sensors?type=CO2
```

An alternative design would use path-based filtering:
```
GET /api/v1/sensors/type/CO2
```

**Why `@QueryParam` is superior for filtering:**

1. **REST Semantics**: In REST, the **path** identifies a **resource**, while **query parameters** represent **optional modifiers** on a resource collection. `type=CO2` does not identify a unique resource — it filters an existing collection.
2. **Composability**: Query parameters can be easily combined: `?type=CO2&status=ACTIVE`. Path segments cannot be composed as flexibly.
3. **Optionality**: Query parameters are naturally optional — omitting `?type=` returns all sensors. With path-based filtering, you would need a separate route for "all sensors" vs. "filtered sensors".
4. **Cacheability**: Query strings are part of the URL and can be cached independently. Each unique combination of parameters is treated as a distinct cacheable resource.
5. **Convention**: Industry-standard APIs (Google, GitHub, AWS) consistently use query parameters for filtering, sorting, and pagination.

---

### Part 4: Deep Nesting with Sub-Resources

#### Q7: Sub-Resource Locator Pattern Benefits

The **Sub-Resource Locator pattern** is implemented in `SensorResource` where the method `getSensorReadings()` returns a `SensorReadingResource` instance rather than a response directly:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

**Architectural benefits:**

1. **Separation of Concerns**: Each resource class manages a single entity type. `SensorResource` handles sensor CRUD, while `SensorReadingResource` handles reading history. This prevents "god classes" with hundreds of methods.
2. **Code Organisation**: In large APIs with dozens of nested paths, having all endpoints in one controller class becomes unmaintainable. Sub-resources allow logical grouping of related operations.
3. **Reusability**: The `SensorReadingResource` class could potentially be reused in other contexts if needed (e.g., batch reading imports).
4. **Testability**: Smaller, focused classes are easier to unit test in isolation.
5. **Scalability**: As the API grows (e.g., adding `/readings/statistics`, `/readings/export`), these can be added to `SensorReadingResource` without bloating the parent class.

---

### Part 5: Error Handling & Exception Mapping

#### Q8: HTTP 422 vs. 404 for Missing References

HTTP **422 Unprocessable Entity** is more semantically accurate than **404 Not Found** when the issue is a missing reference inside a valid JSON payload, for the following reasons:

1. **404 implies the request URL is wrong**: A `404 Not Found` traditionally means "the resource you are trying to access at this URL does not exist." But in our case, the URL (`POST /api/v1/sensors`) is perfectly valid — it is the *data inside the request body* that references a non-existent room.
2. **422 means "I understand your request, but cannot process it"**: The server successfully parsed the JSON, validated its structure, but determined that the business rules cannot be satisfied because the referenced `roomId` does not exist.
3. **Client debugging**: A `404` response might confuse developers into thinking they have the wrong endpoint URL. A `422` clearly communicates that the endpoint is correct but the *content* has a semantic error.
4. **Consistency with REST conventions**: Modern APIs (GitHub, Stripe) use `422` for validation errors within a well-formed request body, reserving `404` strictly for URL-path-level resource lookup failures.

#### Q9: Cybersecurity Risks of Exposing Stack Traces

Exposing internal Java stack traces to external API consumers is a significant security vulnerability. Our `GenericExceptionMapper` prevents this by returning a sanitised error message. Here is what an attacker could gather from a raw stack trace:

1. **Technology Stack**: The trace reveals that the server uses Java, the specific JDK version, Jersey, Grizzly, and other libraries — allowing attackers to search for known vulnerabilities (CVEs) in those exact versions.
2. **Internal Package Structure**: Package names like `com.smartcampus.resource.RoomResource` expose the internal code organisation, class names, and method names.
3. **File Paths**: Stack traces may include absolute file paths (e.g., `C:\Users\dev\projects\...`), revealing the operating system and directory structure.
4. **Business Logic**: Exception messages and method names can reveal how data is validated, stored, and processed — exposing potential attack vectors.
5. **Database Details**: If database-related exceptions leak, they may expose table names, column names, SQL queries, and connection strings.
6. **Third-Party Dependencies**: The trace lists all libraries in the call stack, each of which could have known exploits.

By catching all exceptions with our `GenericExceptionMapper<Throwable>`, we ensure that only a generic, non-revealing `500 Internal Server Error` message is ever sent to the client, while the full stack trace is logged server-side for debugging.

---

## Pre-Seeded Test Data

The API starts with sample data for immediate testing:

### Rooms

| ID | Name | Capacity |
|----|------|----------|
| LIB-301 | Library Quiet Study | 50 |
| ENG-102 | Engineering Lab A | 30 |
| SCI-205 | Science Lecture Hall | 120 |

### Sensors

| ID | Type | Status | Room | Current Value |
|----|------|--------|------|---------------|
| TEMP-001 | Temperature | ACTIVE | LIB-301 | 22.5 |
| CO2-001 | CO2 | ACTIVE | LIB-301 | 415.0 |
| OCC-001 | Occupancy | ACTIVE | ENG-102 | 18.0 |
| TEMP-002 | Temperature | MAINTENANCE | ENG-102 | 0.0 |
| LIGHT-001 | Light | ACTIVE | SCI-205 | 750.0 |
