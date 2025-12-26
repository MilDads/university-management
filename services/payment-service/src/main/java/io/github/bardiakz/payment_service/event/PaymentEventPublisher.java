package io.github.bardiakz.payment_service.event;

import io.github.bardiakz.payment_service.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private static final String EXCHANGE_NAME = "payment.events";

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish PaymentCompleted event - Saga T2 success
     * Marketplace will listen and mark order as COMPLETED
     */
    public void publishPaymentCompleted(Payment payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PaymentCompleted");
        event.put("paymentId", payment.getId());
        event.put("orderId", payment.getOrderId());
        event.put("userId", payment.getUserId());
        event.put("amount", payment.getAmount());
        event.put("transactionId", payment.getTransactionId());
        event.put("timestamp", System.currentTimeMillis());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "payment.completed", event);
            log.info("Published PaymentCompleted event for order: {}", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentCompleted event", e);
        }
    }

    /**
     * Publish PaymentFailed event - Saga compensation trigger
     * Marketplace will listen and restore stock (C1)
     */
    public void publishPaymentFailed(Payment payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PaymentFailed");
        event.put("paymentId", payment.getId());
        event.put("orderId", payment.getOrderId());
        event.put("userId", payment.getUserId());
        event.put("amount", payment.getAmount());
        event.put("reason", payment.getFailureReason());
        event.put("timestamp", System.currentTimeMillis());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "payment.failed", event);
            log.info("Published PaymentFailed event for order: {} - Triggering compensation",
                    payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailed event", e);
        }
    }
}