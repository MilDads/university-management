package io.github.bardiakz.payment_service.event;

import io.github.bardiakz.payment_service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventHandler.class);

    private final PaymentService paymentService;

    public PaymentEventHandler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Listen to OrderCreated events from Marketplace Service
     * Saga Step 2 (T2): Process payment
     */
    @RabbitListener(queues = "payment.order.created.queue")
    public void handleOrderCreated(Map<String, Object> event) {
        try {
            log.info("Received OrderCreated event: {}", event);

            Long orderId = ((Number) event.get("orderId")).longValue();
            String userId = (String) event.get("userId");
            BigDecimal amount = new BigDecimal(event.get("totalAmount").toString());

            paymentService.handleOrderCreated(orderId, userId, amount);

        } catch (Exception e) {
            log.error("Error handling OrderCreated event", e);
            // In production: send to DLQ
        }
    }
}