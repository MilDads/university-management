package io.github.bardiakz.tracking_service.event;

import io.github.bardiakz.tracking_service.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for tracking-related events from other services
 */
@Component
public class LocationEventListener {

    private static final Logger log = LoggerFactory.getLogger(LocationEventListener.class);

    /**
     * Example: Listen for shuttle maintenance events from Resource Service
     */
    @RabbitListener(queues = "#{@maintenanceQueue}")
    public void handleShuttleMaintenanceScheduled(ShuttleMaintenanceEvent event) {
        log.info("Received shuttle maintenance event: {}", event);

        // Could automatically update shuttle status to MAINTENANCE
        // and notify users via WebSocket
    }

    public record ShuttleMaintenanceEvent(
            Long shuttleId,
            String reason,
            String scheduledDate
    ) {}
}