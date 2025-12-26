package io.github.bardiakz.payment_service.dto;

import io.github.bardiakz.payment_service.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String userId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod method,
        String transactionId,
        String failureReason,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getTransactionId(),
                payment.getFailureReason(),
                payment.getCreatedAt()
        );
    }
}