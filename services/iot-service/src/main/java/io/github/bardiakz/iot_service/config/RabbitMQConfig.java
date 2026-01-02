package io.github.bardiakz.iot_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "iot.events";
    public static final String QUEUE_SENSOR_DATA = "iot.sensor.data.queue";
    public static final String ROUTING_KEY_SENSOR_DATA = "iot.sensor.data";

    @Bean
    public TopicExchange iotEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue sensorDataQueue() {
        return QueueBuilder.durable(QUEUE_SENSOR_DATA).build();
    }

    @Bean
    public Binding sensorDataBinding() {
        return BindingBuilder.bind(sensorDataQueue())
                .to(iotEventsExchange())
                .with(ROUTING_KEY_SENSOR_DATA);
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
