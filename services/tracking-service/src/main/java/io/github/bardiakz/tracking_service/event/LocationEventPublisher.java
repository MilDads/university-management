package io.github.bardiakz.tracking_service.event;

import io.github.bardiakz.tracking_service.config.RabbitMQConfig;
import io.github.bardiakz.tracking_service.model.Shuttle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Publishes tracking events to RabbitMQ
 */
@Component
public class LocationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public LocationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish LocationUpdated event when shuttle location changes
     */
    public void publishLocationUpdated(Shuttle shuttle) {
        LocationUpdatedEvent event = new LocationUpdatedEvent(
                shuttle.getId(),
                shuttle.getVehicleNumber(),
                shuttle.getCurrentLatitude(),
                shuttle.getCurrentLongitude(),
                LocalDateTime.now()
        );

        log.debug("Publishing LocationUpdated event for shuttle {}", shuttle.getId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LOCATION_EXCHANGE,
                RabbitMQConfig.LOCATION_UPDATED_ROUTING_KEY,
                event
        );
    }

    /**
     * Event payload for location updates
     */
    public record LocationUpdatedEvent(
            Long shuttleId,
            String vehicleNumber,
            Double latitude,
            Double longitude,
            LocalDateTime timestamp
    ) {}
}
