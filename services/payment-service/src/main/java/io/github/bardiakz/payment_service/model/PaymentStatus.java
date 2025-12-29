package io.github.bardiakz.payment_service.model;

public enum PaymentStatus {
    PENDING,      // Payment initiated
    PROCESSING,   // Payment being processed
    COMPLETED,    // Payment successful (T2 in Saga)
    FAILED,       // Payment failed
    REFUNDED      // Payment refunded (Compensation)
}