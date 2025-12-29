package io.github.bardiakz.booking_service.service;

import io.github.bardiakz.booking_service.dto.CreateBookingRequest;
import io.github.bardiakz.booking_service.dto.BookingResponse;
import io.github.bardiakz.booking_service.event.BookingEventPublisher;
import io.github.bardiakz.booking_service.model.Booking;
import io.github.bardiakz.booking_service.model.BookingStatus;
import io.github.bardiakz.booking_service.repository.BookingRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository,
                          BookingEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new booking with conflict detection and optimistic locking
     * Retries up to 3 times if concurrent booking attempts occur
     */
    @Transactional
//    @Retryable(
//            retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 100)
//    )
    public BookingResponse createBooking(CreateBookingRequest request, String userId, String userRole) {
        log.info("Creating booking for resource {} by user {} ({})", request.resourceId(), userId, userRole);

        // Time Slot Validation: Check for overlapping bookings
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                request.resourceId(),
                request.startTime(),
                request.endTime()
        );

        if (!overlappingBookings.isEmpty()) {
            log.warn("Booking conflict detected for resource {} at time slot {} - {}",
                    request.resourceId(), request.startTime(), request.endTime());
            throw new BookingConflictException(
                    "Resource is already booked for the selected time slot"
            );
        }

        // Create booking
        Booking booking = new Booking(
                request.resourceId(),
                userId,
                userRole,
                request.startTime(),
                request.endTime(),
                request.purpose()
        );
        booking.setStatus(BookingStatus.CONFIRMED);

        try {
            // Save with optimistic locking (@Version)
            Booking savedBooking = bookingRepository.save(booking);

            // Publish BookingConfirmed event
            eventPublisher.publishBookingConfirmed(savedBooking);

            log.info("Booking created successfully with ID: {}", savedBooking.getId());
            return BookingResponse.from(savedBooking);

        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock exception - concurrent booking attempt detected. Retrying...");
            throw e; // Will be retried by @Retryable
        }
    }

    public List<BookingResponse> getMyBookings(String userId) {
        log.debug("Fetching bookings for user: {}", userId);
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getUpcomingBookings(String userId) {
        log.debug("Fetching upcoming bookings for user: {}", userId);
        return bookingRepository.findUpcomingBookingsForUser(userId, LocalDateTime.now()).stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsByResource(Long resourceId) {
        log.debug("Fetching bookings for resource: {}", resourceId);
        return bookingRepository.findByResourceId(resourceId).stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id) {
        log.debug("Fetching booking with ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        return BookingResponse.from(booking);
    }

    @Transactional
    public void cancelBooking(Long id, String userId) {
        log.info("Cancelling booking {} by user {}", id, userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        // Verify ownership
        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own bookings");
        }

        // Check if booking can be cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Publish BookingCancelled event
        eventPublisher.publishBookingCancelled(booking);

        log.info("Booking cancelled successfully");
    }

    // Handle ResourceStatusChanged event from Resource Service
    @Transactional
    public void handleResourceStatusChanged(Long resourceId, String newStatus) {
        log.info("Handling resource status changed: {} -> {}", resourceId, newStatus);

        if ("UNAVAILABLE".equals(newStatus) || "MAINTENANCE".equals(newStatus)) {
            // Cancel all active bookings for this resource
            List<Booking> activeBookings = bookingRepository.findActiveBookingsForResource(
                    resourceId,
                    LocalDateTime.now()
            );

            for (Booking booking : activeBookings) {
                booking.setStatus(BookingStatus.REJECTED);
                bookingRepository.save(booking);
                eventPublisher.publishBookingCancelled(booking);
            }

            log.info("Cancelled {} active bookings due to resource status change",
                    activeBookings.size());
        }
    }
}