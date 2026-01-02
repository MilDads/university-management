package io.github.bardiakz.iot_service.dto;

import java.time.Instant;

public class SensorReadingDTO {
    private String sensorId;
    private Double value;
    private Instant timestamp;
    private String unit;

    public SensorReadingDTO() {}

    public SensorReadingDTO(String sensorId, Double value, Instant timestamp, String unit) {
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
        this.unit = unit;
    }

    // Getters & Setters
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
