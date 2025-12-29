package com.university.marketplace.event;

import com.university.marketplace.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public MarketplaceEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MARKETPLACE_EXCHANGE,
                "order.created",
                event
        );
    }
}
