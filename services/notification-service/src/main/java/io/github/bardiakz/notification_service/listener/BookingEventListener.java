package io.github.bardiakz.notification_service.listener;

import io.github.bardiakz.notification_service.entity.NotificationType;
import io.github.bardiakz.notification_service.event.BookingCancelledEvent;
import io.github.bardiakz.notification_service.event.BookingConfirmedEvent;
import io.github.bardiakz.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationService notificationService;

    public BookingEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.booking.confirmed}")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        logger.info("Received BookingConfirmedEvent for booking: {}", event.getBookingId());

        try {
            Map<String, String> variables = Map.of(
                    "bookingId", String.valueOf(event.getBookingId()),
                    "resourceName", event.getResourceName(),
                    "startTime", event.getStartTime().format(DATE_FORMATTER),
                    "endTime", event.getEndTime().format(DATE_FORMATTER)
            );

            notificationService.createFromTemplate(
                    "booking-confirmation",
                    event.getUserEmail(),
                    NotificationType.BOOKING_CONFIRMATION,
                    variables,
                    event.getUserId()
            );

            logger.info("Booking confirmation email sent to: {}", event.getUserEmail());

        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.booking.cancelled}")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        logger.info("Received BookingCancelledEvent for booking: {}", event.getBookingId());

        try {
            Map<String, String> variables = Map.of(
                    "bookingId", String.valueOf(event.getBookingId()),
                    "resourceName", event.getResourceName()
            );

            notificationService.createFromTemplate(
                    "booking-cancellation",
                    event.getUserEmail(),
                    NotificationType.BOOKING_CANCELLATION,
                    variables,
                    event.getUserId()
            );

            logger.info("Booking cancellation email sent to: {}", event.getUserEmail());

        } catch (Exception e) {
            logger.error("Failed to send booking cancellation email: {}", e.getMessage(), e);
        }
    }
}
