package io.github.bardiakz.booking_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Booking events exchange
    public static final String BOOKING_EXCHANGE = "booking.events";
    public static final String BOOKING_CONFIRMED_QUEUE = "booking.confirmed.queue";
    public static final String BOOKING_CANCELLED_QUEUE = "booking.cancelled.queue";

    // Listen to Resource Service events
    public static final String RESOURCE_EXCHANGE = "resource.events";
    public static final String BOOKING_RESOURCE_STATUS_QUEUE = "booking.resource.status.queue";

    // Booking Exchange
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public Queue bookingConfirmedQueue() {
        return new Queue(BOOKING_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return new Queue(BOOKING_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingConfirmedQueue)
                .to(bookingExchange)
                .with("booking.confirmed");
    }

    @Bean
    public Binding bookingCancelledBinding(Queue bookingCancelledQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingCancelledQueue)
                .to(bookingExchange)
                .with("booking.cancelled");
    }

    // Resource Exchange (listen to Resource Service)
    @Bean
    public TopicExchange resourceExchange() {
        return new TopicExchange(RESOURCE_EXCHANGE);
    }

    @Bean
    public Queue bookingResourceStatusQueue() {
        return new Queue(BOOKING_RESOURCE_STATUS_QUEUE, true);
    }

    @Bean
    public Binding bookingResourceStatusBinding(Queue bookingResourceStatusQueue, TopicExchange resourceExchange) {
        return BindingBuilder.bind(bookingResourceStatusQueue)
                .to(resourceExchange)
                .with("resource.status.changed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}