package io.github.bardiakz.marketplace_service.event;

import io.github.bardiakz.marketplace_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    private final OrderService orderService;

    public OrderEventHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Listen to PaymentCompleted events from Payment Service
     * Saga Step 2 (T2)
     */
    @RabbitListener(queues = "marketplace.payment.completed.queue")
    public void handlePaymentCompleted(Map<String, Object> event) {
        try {
            log.info("Received PaymentCompleted event: {}", event);

            // Extract orderId - handle both Integer and Long
            Long orderId;
            Object orderIdObj = event.get("orderId");
            if (orderIdObj instanceof Integer) {
                orderId = ((Integer) orderIdObj).longValue();
            } else if (orderIdObj instanceof Long) {
                orderId = (Long) orderIdObj;
            } else if (orderIdObj instanceof Number) {
                orderId = ((Number) orderIdObj).longValue();
            } else {
                throw new IllegalArgumentException("Invalid orderId type: " + orderIdObj.getClass());
            }

            // Extract paymentId - handle potential null or different types
            String paymentId = String.valueOf(event.get("paymentId"));

            orderService.handlePaymentCompleted(orderId, paymentId);

        } catch (Exception e) {
            log.error("Error handling PaymentCompleted event", e);
            // In production, send to DLQ (Dead Letter Queue)
        }
    }

    /**
     * Listen to PaymentFailed events from Payment Service
     * Saga Compensation (C1)
     */
    @RabbitListener(queues = "marketplace.payment.failed.queue")
    public void handlePaymentFailed(Map<String, Object> event) {
        try {
            log.info("Received PaymentFailed event: {}", event);

            // Extract orderId - handle both Integer and Long
            Long orderId;
            Object orderIdObj = event.get("orderId");
            if (orderIdObj instanceof Integer) {
                orderId = ((Integer) orderIdObj).longValue();
            } else if (orderIdObj instanceof Long) {
                orderId = (Long) orderIdObj;
            } else if (orderIdObj instanceof Number) {
                orderId = ((Number) orderIdObj).longValue();
            } else {
                throw new IllegalArgumentException("Invalid orderId type: " + orderIdObj.getClass());
            }

            String reason = (String) event.getOrDefault("reason", "Payment failed");

            orderService.handlePaymentFailed(orderId, reason);

        } catch (Exception e) {
            log.error("Error handling PaymentFailed event", e);
            // In production, send to DLQ
        }
    }
}