package io.github.bardiakz.tracking_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for tracking service
 * Uses JacksonJsonMessageConverter (Jackson 3.x) for Spring Boot 4.0
 */
@Configuration
public class RabbitMQConfig {

    public static final String LOCATION_EXCHANGE = "tracking.location.exchange";
    public static final String LOCATION_UPDATED_QUEUE = "tracking.location.updated";
    public static final String LOCATION_UPDATED_ROUTING_KEY = "location.updated";

    // Queue names for event listeners
    public static final String MAINTENANCE_QUEUE_NAME = "tracking.maintenance.queue";
    public static final String ALERT_QUEUE_NAME = "tracking.alert.queue";

    @Bean
    public Exchange locationExchange() {
        return ExchangeBuilder
                .topicExchange(LOCATION_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue locationUpdatedQueue() {
        return QueueBuilder
                .durable(LOCATION_UPDATED_QUEUE)
                .build();
    }

    @Bean
    public Binding locationUpdatedBinding(Queue locationUpdatedQueue, Exchange locationExchange) {
        return BindingBuilder
                .bind(locationUpdatedQueue)
                .to(locationExchange)
                .with(LOCATION_UPDATED_ROUTING_KEY)
                .noargs();
    }

    /**
     * Queue for maintenance events from resource service
     */
    @Bean
    public Queue maintenanceQueue() {
        return QueueBuilder
                .durable(MAINTENANCE_QUEUE_NAME)
                .build();
    }

    /**
     * Queue for alert events
     */
    @Bean
    public Queue alertQueue() {
        return QueueBuilder
                .durable(ALERT_QUEUE_NAME)
                .build();
    }

    /**
     * Message converter using Jackson 3.x
     * Note: JacksonJsonMessageConverter is the replacement for
     * the deprecated Jackson2JsonMessageConverter in Spring Boot 4.0
     */
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}