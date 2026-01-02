package io.github.bardiakz.iot_service.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sensor_readings", indexes = {
    @Index(name = "idx_sensor_timestamp", columnList = "sensor_id,timestamp")
})
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private String sensorId;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp;

    @Column
    private String metadata; // JSON string for additional data

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
