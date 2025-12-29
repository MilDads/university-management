package io.github.bardiakz.booking_service.event;

import io.github.bardiakz.booking_service.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);
    private static final String EXCHANGE_NAME = "booking.events";

    private final RabbitTemplate rabbitTemplate;

    public BookingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBookingConfirmed(Booking booking) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "BookingConfirmed");
        event.put("bookingId", booking.getId());
        event.put("resourceId", booking.getResourceId());
        event.put("userId", booking.getUserId());
        event.put("startTime", booking.getStartTime().toString());
        event.put("endTime", booking.getEndTime().toString());
        event.put("timestamp", System.currentTimeMillis());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "booking.confirmed", event);
            log.info("Published BookingConfirmed event for booking ID: {}", booking.getId());
        } catch (Exception e) {
            log.error("Failed to publish BookingConfirmed event", e);
        }
    }

    public void publishBookingCancelled(Booking booking) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "BookingCancelled");
        event.put("bookingId", booking.getId());
        event.put("resourceId", booking.getResourceId());
        event.put("userId", booking.getUserId());
        event.put("timestamp", System.currentTimeMillis());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "booking.cancelled", event);
            log.info("Published BookingCancelled event for booking ID: {}", booking.getId());
        } catch (Exception e) {
            log.error("Failed to publish BookingCancelled event", e);
        }
    }
}