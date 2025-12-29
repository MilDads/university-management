package io.github.bardiakz.tracking_service.model;

public enum ShuttleStatus {
    ACTIVE,      // Shuttle is currently running
    INACTIVE,    // Shuttle is not in service
    MAINTENANCE, // Shuttle is under maintenance
    OUT_OF_SERVICE
}