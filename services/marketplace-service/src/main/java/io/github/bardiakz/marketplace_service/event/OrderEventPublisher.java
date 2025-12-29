package io.github.bardiakz.marketplace_service.event;

import io.github.bardiakz.marketplace_service.model.Order;
import io.github.bardiakz.marketplace_service.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private static final String EXCHANGE_NAME = "marketplace.events";

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish OrderCreated event to start Saga
     * Payment Service will listen to this event
     */
    public void publishOrderCreated(Order order) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "OrderCreated");
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("totalAmount", order.getTotalAmount());
        event.put("items", serializeItems(order.getItems()));
        event.put("timestamp", System.currentTimeMillis());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "order.created", event);
            log.info("Published OrderCreated event for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreated event", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    private List<Map<String, Object>> serializeItems(List<OrderItem> items) {
        return items.stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("productName", item.getProductName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unitPrice", item.getUnitPrice());
            itemMap.put("totalPrice", item.getTotalPrice());
            return itemMap;
        }).collect(Collectors.toList());
    }
}