package io.github.bardiakz.user.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public EventPublisher(RabbitTemplate rabbitTemplate,
                          @Value("${rabbitmq.exchange.user-events}") String exchange,
                          @Value("${rabbitmq.routing-key.profile-created}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishProfileCreated(UserProfileCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published UserProfileCreated event for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish UserProfileCreated event", e);
        }
    }
}