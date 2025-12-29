package io.github.bardiakz.booking_service.repository;

import io.github.bardiakz.booking_service.model.Booking;
import io.github.bardiakz.booking_service.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find bookings by user
    List<Booking> findByUserId(String userId);

    // Find bookings by resource
    List<Booking> findByResourceId(Long resourceId);

    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);

    // CRITICAL: Check for overlapping bookings (Time Slot Validation)
    @Query("SELECT b FROM Booking b WHERE b.resourceId = :resourceId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findOverlappingBookings(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Find active bookings for a resource
    @Query("SELECT b FROM Booking b WHERE b.resourceId = :resourceId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.endTime > :now")
    List<Booking> findActiveBookingsForResource(
            @Param("resourceId") Long resourceId,
            @Param("now") LocalDateTime now
    );

    // Find user's upcoming bookings
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.startTime > :now " +
            "ORDER BY b.startTime ASC")
    List<Booking> findUpcomingBookingsForUser(
            @Param("userId") String userId,
            @Param("now") LocalDateTime now
    );
}