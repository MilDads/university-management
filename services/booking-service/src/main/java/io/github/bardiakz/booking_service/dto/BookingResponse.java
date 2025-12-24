package io.github.bardiakz.booking_service.dto;

import io.github.bardiakz.booking_service.model.Booking;
import io.github.bardiakz.booking_service.model.BookingStatus;
import java.time.LocalDateTime;

public record BookingResponse(

        Long id,

        Long resourceId,

        String userId,

        String userRole,

        LocalDateTime startTime,

        LocalDateTime endTime,

        BookingStatus status,

        String purpose,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {

    public static BookingResponse from(Booking booking) {

        return new BookingResponse(

                booking.getId(),

                booking.getResourceId(),

                booking.getUserId(),

                booking.getUserRole(),

                booking.getStartTime(),

                booking.getEndTime(),

                booking.getStatus(),

                booking.getPurpose(),

                booking.getCreatedAt(),

                booking.getUpdatedAt()

        );

    }

}
