package io.github.bardiakz.iot_service.dto;

import io.github.bardiakz.iot_service.entity.SensorType;

public class SensorUpdateRequest {
    private String name;
    private SensorType type;
    private String location;
    private String unit;
    private Boolean active;

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SensorType getType() { return type; }
    public void setType(SensorType type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
