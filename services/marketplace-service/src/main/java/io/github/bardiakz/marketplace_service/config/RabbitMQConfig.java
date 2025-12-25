package io.github.bardiakz.marketplace_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Marketplace events exchange
    public static final String MARKETPLACE_EXCHANGE = "marketplace.events";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";

    // Listen to Payment Service events
    public static final String PAYMENT_EXCHANGE = "payment.events";
    public static final String MARKETPLACE_PAYMENT_COMPLETED_QUEUE = "marketplace.payment.completed.queue";
    public static final String MARKETPLACE_PAYMENT_FAILED_QUEUE = "marketplace.payment.failed.queue";

    // Marketplace Exchange
    @Bean
    public TopicExchange marketplaceExchange() {
        return new TopicExchange(MARKETPLACE_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange marketplaceExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(marketplaceExchange)
                .with("order.created");
    }

    // Payment Exchange (listen to Payment Service)
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue marketplacePaymentCompletedQueue() {
        return new Queue(MARKETPLACE_PAYMENT_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue marketplacePaymentFailedQueue() {
        return new Queue(MARKETPLACE_PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public Binding marketplacePaymentCompletedBinding(
            Queue marketplacePaymentCompletedQueue,
            TopicExchange paymentExchange) {
        return BindingBuilder.bind(marketplacePaymentCompletedQueue)
                .to(paymentExchange)
                .with("payment.completed");
    }

    @Bean
    public Binding marketplacePaymentFailedBinding(
            Queue marketplacePaymentFailedQueue,
            TopicExchange paymentExchange) {
        return BindingBuilder.bind(marketplacePaymentFailedQueue)
                .to(paymentExchange)
                .with("payment.failed");
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