package io.github.bardiakz.exam_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.exam.started}")
    private String examStartedQueue;

    @Value("${rabbitmq.routing.key.exam.started}")
    private String examStartedRoutingKey;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue examStartedQueue() {
        return QueueBuilder.durable(examStartedQueue)
                .withArgument("x-dead-letter-exchange", exchangeName + ".dlx")
                .build();
    }

    @Bean
    public Binding examStartedBinding(Queue examStartedQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(examStartedQueue)
                .to(exchange)
                .with(examStartedRoutingKey);
    }

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