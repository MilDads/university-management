package io.github.bardiakz.resource_service.event;

import io.github.bardiakz.resource_service.model.Resource;
import io.github.bardiakz.resource_service.model.ResourceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResourceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResourceEventPublisher.class);
    private static final String EXCHANGE_NAME = "resource.events";

    private final RabbitTemplate rabbitTemplate;

    public ResourceEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishResourceAdded(Resource resource) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ResourceAdded");
        event.put("resourceId", resource.getId());
        event.put("name", resource.getName());
        event.put("type", resource.getType().toString());
        event.put("status", resource.getStatus().toString());
        event.put("createdBy", resource.getCreatedBy());
        event.put("timestamp", System.currentTimeMillis());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "resource.added", event);
            log.info("Published ResourceAdded event for resource ID: {}", resource.getId());
        } catch (Exception e) {
            log.error("Failed to publish ResourceAdded event", e);
        }
    }

    public void publishResourceStatusChanged(Resource resource, ResourceStatus oldStatus, ResourceStatus newStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ResourceStatusChanged");
        event.put("resourceId", resource.getId());
        event.put("name", resource.getName());
        event.put("type", resource.getType().toString());
        event.put("oldStatus", oldStatus.toString());
        event.put("newStatus", newStatus.toString());
        event.put("timestamp", System.currentTimeMillis());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "resource.status.changed", event);
            log.info("Published ResourceStatusChanged event for resource ID: {} ({} -> {})",
                    resource.getId(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Failed to publish ResourceStatusChanged event", e);
        }
    }
}