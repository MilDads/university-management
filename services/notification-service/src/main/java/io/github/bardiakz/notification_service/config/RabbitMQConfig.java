package io.github.bardiakz.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Notification Service
 * Implements Observer pattern - listens to events from other services
 */
@Configuration
public class RabbitMQConfig {

    // User Service Events
    @Value("${rabbitmq.exchange.user}")
    private String userExchange;

    @Value("${rabbitmq.queue.user.registered}")
    private String userRegisteredQueue;

    // Booking Service Events
    @Value("${rabbitmq.exchange.booking}")
    private String bookingExchange;

    @Value("${rabbitmq.queue.booking.confirmed}")
    private String bookingConfirmedQueue;

    @Value("${rabbitmq.queue.booking.cancelled}")
    private String bookingCancelledQueue;

    // Marketplace Service Events
    @Value("${rabbitmq.exchange.marketplace}")
    private String marketplaceExchange;

    @Value("${rabbitmq.queue.order.created}")
    private String orderCreatedQueue;

    // Payment Service Events
    @Value("${rabbitmq.exchange.payment}")
    private String paymentExchange;

    @Value("${rabbitmq.queue.payment.completed}")
    private String paymentCompletedQueue;

    @Value("${rabbitmq.queue.payment.failed}")
    private String paymentFailedQueue;

    // Exam Service Events
    @Value("${rabbitmq.exchange.exam}")
    private String examExchange;

    @Value("${rabbitmq.queue.exam.created}")
    private String examCreatedQueue;

    @Value("${rabbitmq.queue.exam.submitted}")
    private String examSubmittedQueue;

    @Value("${rabbitmq.queue.exam.graded}")
    private String examGradedQueue;

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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // ==================== USER EXCHANGE ====================
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(userExchange);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(userRegisteredQueue, true);
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(userExchange())
                .with("user.registered");
    }

    // ==================== BOOKING EXCHANGE ====================
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(bookingExchange);
    }

    @Bean
    public Queue bookingConfirmedQueue() {
        return new Queue(bookingConfirmedQueue, true);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return new Queue(bookingCancelledQueue, true);
    }

    @Bean
    public Binding bookingConfirmedBinding() {
        return BindingBuilder.bind(bookingConfirmedQueue())
                .to(bookingExchange())
                .with("booking.confirmed");
    }

    @Bean
    public Binding bookingCancelledBinding() {
        return BindingBuilder.bind(bookingCancelledQueue())
                .to(bookingExchange())
                .with("booking.cancelled");
    }

    // ==================== MARKETPLACE EXCHANGE ====================
    @Bean
    public TopicExchange marketplaceExchange() {
        return new TopicExchange(marketplaceExchange);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueue, true);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(marketplaceExchange())
                .with("order.created");
    }

    // ==================== PAYMENT EXCHANGE ====================
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(paymentExchange);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(paymentCompletedQueue, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(paymentFailedQueue, true);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(paymentCompletedQueue())
                .to(paymentExchange())
                .with("payment.completed");
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(paymentExchange())
                .with("payment.failed");
    }

    // ==================== EXAM EXCHANGE ====================
    @Bean
    public TopicExchange examExchange() {
        return new TopicExchange(examExchange);
    }

    @Bean
    public Queue examCreatedQueue() {
        return new Queue(examCreatedQueue, true);
    }

    @Bean
    public Queue examSubmittedQueue() {
        return new Queue(examSubmittedQueue, true);
    }

    @Bean
    public Queue examGradedQueue() {
        return new Queue(examGradedQueue, true);
    }

    @Bean
    public Binding examCreatedBinding() {
        return BindingBuilder.bind(examCreatedQueue())
                .to(examExchange())
                .with("exam.created");
    }

    @Bean
    public Binding examSubmittedBinding() {
        return BindingBuilder.bind(examSubmittedQueue())
                .to(examExchange())
                .with("exam.submitted");
    }

    @Bean
    public Binding examGradedBinding() {
        return BindingBuilder.bind(examGradedQueue())
                .to(examExchange())
                .with("exam.graded");
    }
}
