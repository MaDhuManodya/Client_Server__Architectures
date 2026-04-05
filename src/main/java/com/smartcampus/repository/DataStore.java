package com.smartcampus.repository;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton in-memory data store for the Smart Campus API.
 * Uses ConcurrentHashMap for thread safety across concurrent requests.
 * Pre-seeded with sample data for demonstration purposes.
 */
public class DataStore {

    // Singleton instance
    private static DataStore instance;

    // In-memory collections
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Private constructor — pre-seeds sample data
    private DataStore() {
        seedData();
    }

    /**
     * Returns the singleton instance of DataStore.
     */
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // ─── Room Operations ───────────────────────────────────────────────

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room removeRoom(String id) {
        return rooms.remove(id);
    }

    // ─── Sensor Operations ─────────────────────────────────────────────

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Initialize an empty readings list for this sensor
        readings.putIfAbsent(sensor.getId(), new CopyOnWriteArrayList<>());
    }

    public Sensor removeSensor(String id) {
        readings.remove(id);
        return sensors.remove(id);
    }

    // ─── Reading Operations ────────────────────────────────────────────

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>()).add(reading);
    }

    // ─── Seed Data ─────────────────────────────────────────────────────

    private void seedData() {
        // --- Rooms ---
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("ENG-102", "Engineering Lab A", 30);
        Room room3 = new Room("SCI-205", "Science Lecture Hall", 120);

        // --- Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 415.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "ACTIVE", 18.0, "ENG-102");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "ENG-102");
        Sensor s5 = new Sensor("LIGHT-001", "Light", "ACTIVE", 750.0, "SCI-205");

        // Link sensors to rooms
        room1.addSensorId("TEMP-001");
        room1.addSensorId("CO2-001");
        room2.addSensorId("OCC-001");
        room2.addSensorId("TEMP-002");
        room3.addSensorId("LIGHT-001");

        // Store rooms
        addRoom(room1);
        addRoom(room2);
        addRoom(room3);

        // Store sensors
        addSensor(s1);
        addSensor(s2);
        addSensor(s3);
        addSensor(s4);
        addSensor(s5);

        // --- Sample Readings ---
        long now = System.currentTimeMillis();

        addReading("TEMP-001", new SensorReading(UUID.randomUUID().toString(), now - 60000, 22.1));
        addReading("TEMP-001", new SensorReading(UUID.randomUUID().toString(), now - 30000, 22.3));
        addReading("TEMP-001", new SensorReading(UUID.randomUUID().toString(), now, 22.5));

        addReading("CO2-001", new SensorReading(UUID.randomUUID().toString(), now - 60000, 410.0));
        addReading("CO2-001", new SensorReading(UUID.randomUUID().toString(), now, 415.0));

        addReading("OCC-001", new SensorReading(UUID.randomUUID().toString(), now, 18.0));
    }
}
