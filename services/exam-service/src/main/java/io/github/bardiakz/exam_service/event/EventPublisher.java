package io.github.bardiakz.exam_service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.exam.started}")
    private String examStartedRoutingKey;

    public void publishExamStartedEvent(ExamStartedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchangeName, examStartedRoutingKey, eventJson);
            log.info("Published ExamStartedEvent for exam ID: {}", event.getExamId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ExamStartedEvent: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to publish ExamStartedEvent: {}", e.getMessage(), e);
        }
    }
}