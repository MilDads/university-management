package io.github.bardiakz.booking_service.controller;

import io.github.bardiakz.booking_service.dto.CreateBookingRequest;
import io.github.bardiakz.booking_service.dto.BookingResponse;
import io.github.bardiakz.booking_service.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Create a new booking
     */
    @PostMapping
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User authentication required"));
        }

        log.info("Create booking request from user: {}", userId);

        try {
            BookingResponse response = bookingService.createBooking(request, userId, userRole != null ? userRole : "STUDENT");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating booking", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get my bookings
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Get my bookings request from user: {}", userId);
        List<BookingResponse> bookings = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get upcoming bookings
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Get upcoming bookings request from user: {}", userId);
        List<BookingResponse> bookings = bookingService.getUpcomingBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        log.debug("Get booking by ID request: {}", id);
        try {
            BookingResponse response = bookingService.getBookingById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching booking", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get bookings for a resource (Available to all users to check availability)
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByResource(
            @PathVariable Long resourceId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        // Allow all users to view bookings to see availability
        // Privacy note: In a real production system, we might want to return a DTO
        // without the userId for non-admin users.

        log.debug("Get bookings for resource: {}", resourceId);
        List<BookingResponse> bookings = bookingService.getBookingsByResource(resourceId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User authentication required"));
        }

        log.info("Cancel booking request for ID: {} by user: {}", id, userId);

        try {
            bookingService.cancelBooking(id, userId);
            return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
        } catch (Exception e) {
            log.error("Error cancelling booking", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}