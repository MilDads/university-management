package io.github.bardiakz.booking_service.event;

import io.github.bardiakz.booking_service.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BookingEventHandler {

    private static final Logger log = LoggerFactory.getLogger(BookingEventHandler.class);

    private final BookingService bookingService;

    public BookingEventHandler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Listen to ResourceStatusChanged events from Resource Service
     * Cancel bookings if resource becomes unavailable
     */
    @RabbitListener(queues = "booking.resource.status.queue")
    public void handleResourceStatusChanged(Map<String, Object> event) {
        try {
            log.info("Received ResourceStatusChanged event: {}", event);

            Long resourceId = ((Number) event.get("resourceId")).longValue();
            String newStatus = (String) event.get("newStatus");

            bookingService.handleResourceStatusChanged(resourceId, newStatus);

        } catch (Exception e) {
            log.error("Error handling ResourceStatusChanged event", e);
        }
    }
}