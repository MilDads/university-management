package io.github.bardiakz.payment_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Payment events exchange
    public static final String PAYMENT_EXCHANGE = "payment.events";
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";

    // Listen to Marketplace events
    public static final String MARKETPLACE_EXCHANGE = "marketplace.events";
    public static final String PAYMENT_ORDER_CREATED_QUEUE = "payment.order.created.queue";

    // Payment Exchange
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(PAYMENT_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue,
                                           TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentCompletedQueue)
                .to(paymentExchange)
                .with("payment.completed");
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue,
                                        TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentFailedQueue)
                .to(paymentExchange)
                .with("payment.failed");
    }

    // Marketplace Exchange (listen to Marketplace)
    @Bean
    public TopicExchange marketplaceExchange() {
        return new TopicExchange(MARKETPLACE_EXCHANGE);
    }

    @Bean
    public Queue paymentOrderCreatedQueue() {
        return new Queue(PAYMENT_ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding paymentOrderCreatedBinding(Queue paymentOrderCreatedQueue,
                                              TopicExchange marketplaceExchange) {
        return BindingBuilder.bind(paymentOrderCreatedQueue)
                .to(marketplaceExchange)
                .with("order.created");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}