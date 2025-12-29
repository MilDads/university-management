package io.github.bardiakz.resource_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RESOURCE_EXCHANGE = "resource.events";
    public static final String RESOURCE_ADDED_QUEUE = "resource.added.queue";
    public static final String RESOURCE_STATUS_CHANGED_QUEUE = "resource.status.changed.queue";

    @Bean
    public TopicExchange resourceExchange() {
        return new TopicExchange(RESOURCE_EXCHANGE);
    }

    @Bean
    public Queue resourceAddedQueue() {
        return new Queue(RESOURCE_ADDED_QUEUE, true);
    }

    @Bean
    public Queue resourceStatusChangedQueue() {
        return new Queue(RESOURCE_STATUS_CHANGED_QUEUE, true);
    }

    @Bean
    public Binding resourceAddedBinding(Queue resourceAddedQueue, TopicExchange resourceExchange) {
        return BindingBuilder.bind(resourceAddedQueue)
                .to(resourceExchange)
                .with("resource.added");
    }

    @Bean
    public Binding resourceStatusChangedBinding(Queue resourceStatusChangedQueue, TopicExchange resourceExchange) {
        return BindingBuilder.bind(resourceStatusChangedQueue)
                .to(resourceExchange)
                .with("resource.status.changed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // JacksonJsonMessageConverter uses Jackson 3.x (no constructor parameters needed)
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}