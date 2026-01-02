package io.github.bardiakz.iot_service.dto;

import io.github.bardiakz.iot_service.entity.SensorType;

public class SensorDTO {
    private Long id;
    private String sensorId;
    private String name;
    private SensorType type;
    private String location;
    private String unit;
    private boolean active;
    private Double lastValue;
    private String lastUpdate;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SensorType getType() { return type; }
    public void setType(SensorType type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Double getLastValue() { return lastValue; }
    public void setLastValue(Double lastValue) { this.lastValue = lastValue; }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
}
