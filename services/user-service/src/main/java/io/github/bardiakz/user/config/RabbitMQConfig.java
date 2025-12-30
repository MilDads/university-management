package io.github.bardiakz.user.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.user-events}")
    private String exchange;

    @Value("${rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;

    @Value("${rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(userRegisteredQueue)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .build();
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(userEventsExchange())
                .with(userRegisteredRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
