package io.github.bardiakz.payment_service.service;

import io.github.bardiakz.payment_service.dto.*;
import io.github.bardiakz.payment_service.event.PaymentEventPublisher;
import io.github.bardiakz.payment_service.model.*;
import io.github.bardiakz.payment_service.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handle OrderCreated event from Marketplace
     * Saga Step 2 (T2): Process payment
     */
    @Transactional
    public void handleOrderCreated(Long orderId, String userId, java.math.BigDecimal amount) {
        log.info("Handling OrderCreated for order: {} - Processing payment", orderId);

        // Create payment record
        Payment payment = new Payment(orderId, userId, amount, PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PROCESSING);

        Payment savedPayment = paymentRepository.save(payment);

        // Simulate payment processing
        boolean paymentSuccess = processPayment(savedPayment);

        if (paymentSuccess) {
            // Payment successful - generate transaction ID
            String transactionId = UUID.randomUUID().toString();
            savedPayment.markAsCompleted(transactionId);
            paymentRepository.save(savedPayment);

            // Publish PaymentCompleted event
            eventPublisher.publishPaymentCompleted(savedPayment);

            log.info("Payment completed for order: {} with transaction: {}",
                    orderId, transactionId);
        } else {
            // Payment failed
            savedPayment.markAsFailed("Insufficient funds");
            paymentRepository.save(savedPayment);

            // Publish PaymentFailed event (triggers compensation in Marketplace)
            eventPublisher.publishPaymentFailed(savedPayment);

            log.warn("Payment failed for order: {}", orderId);
        }
    }

    /**
     * Simulate payment processing with external payment provider
     * In production, this would call real payment gateway APIs
     */
    private boolean processPayment(Payment payment) {
        try {
            // Simulate external payment API call delay
            Thread.sleep(1000);

            // 90% success rate for simulation
            // In production: call actual payment provider API
            return Math.random() > 0.1;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public List<PaymentResponse> getMyPayments(String userId) {
        log.debug("Fetching payments for user: {}", userId);
        return paymentRepository.findByUserId(userId).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long id) {
        log.debug("Fetching payment with ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.debug("Fetching payment for order: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order"));
        return PaymentResponse.from(payment);
    }
}