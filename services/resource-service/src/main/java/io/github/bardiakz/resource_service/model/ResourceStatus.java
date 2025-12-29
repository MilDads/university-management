package io.github.bardiakz.resource_service.model;

public enum ResourceStatus {
    AVAILABLE,      // Resource is available for booking
    BOOKED,         // Resource is currently booked
    MAINTENANCE,    // Resource is under maintenance
    UNAVAILABLE     // Resource is not available
}