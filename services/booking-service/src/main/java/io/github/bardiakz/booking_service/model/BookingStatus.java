package io.github.bardiakz.booking_service.model;

public enum BookingStatus {
    PENDING,      // Booking request submitted
    CONFIRMED,    // Booking confirmed
    CANCELLED,    // Booking cancelled by user
    REJECTED,     // Booking rejected (resource unavailable)
    COMPLETED     // Booking time has passed
}