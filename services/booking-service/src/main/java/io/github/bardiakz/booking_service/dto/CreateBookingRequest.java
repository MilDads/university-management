package io.github.bardiakz.booking_service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull(message = "Resource ID is required")
        Long resourceId,

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        LocalDateTime endTime,

        @Size(max = 500, message = "Purpose cannot exceed 500 characters")
        String purpose
) {
    public CreateBookingRequest {
        // Validation: end time must be after start time
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}