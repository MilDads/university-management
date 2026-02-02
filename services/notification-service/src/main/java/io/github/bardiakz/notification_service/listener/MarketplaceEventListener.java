package io.github.bardiakz.notification_service.listener;

import io.github.bardiakz.notification_service.entity.NotificationType;
import io.github.bardiakz.notification_service.event.OrderCreatedEvent;
import io.github.bardiakz.notification_service.event.PaymentCompletedEvent;
import io.github.bardiakz.notification_service.event.PaymentFailedEvent;
import io.github.bardiakz.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MarketplaceEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MarketplaceEventListener.class);
    private final NotificationService notificationService;

    public MarketplaceEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.order.created}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("Received OrderCreatedEvent for order: {}", event.getOrderId());

        try {
            Map<String, String> variables = Map.of(
                    "orderId", String.valueOf(event.getOrderId()),
                    "totalAmount", event.getTotalAmount().toString()
            );

            notificationService.createFromTemplate(
                    "order-confirmation",
                    event.getUserEmail(),
                    NotificationType.ORDER_CONFIRMATION,
                    variables,
                    event.getUserId()
            );

            logger.info("Order confirmation email sent to: {}", event.getUserEmail());

        } catch (Exception e) {
            logger.error("Failed to send order confirmation email: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.payment.completed}")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        logger.info("Received PaymentCompletedEvent for payment: {}", event.getPaymentId());

        try {
            Map<String, String> variables = Map.of(
                    "orderId", String.valueOf(event.getOrderId()),
                    "amount", event.getAmount().toString(),
                    "transactionId", event.getTransactionId()
            );

            notificationService.createFromTemplate(
                    "payment-success",
                    event.getUserEmail(),
                    NotificationType.PAYMENT_SUCCESS,
                    variables,
                    event.getUserId()
            );

            logger.info("Payment success email sent to: {}", event.getUserEmail());

        } catch (Exception e) {
            logger.error("Failed to send payment success email: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.payment.failed}")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        logger.info("Received PaymentFailedEvent for order: {}", event.getOrderId());

        try {
            Map<String, String> variables = Map.of(
                    "orderId", String.valueOf(event.getOrderId()),
                    "reason", event.getReason()
            );

            notificationService.createFromTemplate(
                    "payment-failed",
                    event.getUserEmail(),
                    NotificationType.PAYMENT_FAILURE,
                    variables,
                    event.getUserId()
            );

            logger.info("Payment failed email sent to: {}", event.getUserEmail());

        } catch (Exception e) {
            logger.error("Failed to send payment failed email: {}", e.getMessage(), e);
        }
    }
}
