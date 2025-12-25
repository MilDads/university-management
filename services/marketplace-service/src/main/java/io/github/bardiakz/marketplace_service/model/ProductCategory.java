package io.github.bardiakz.marketplace_service.model;

public enum ProductCategory {
    WORKSHOP_TICKET,
    EVENT_TICKET,
    BOOK,
    MERCHANDISE,
    SERVICE,
    OTHER
}

public enum OrderStatus {
    PENDING,
    PAYMENT_PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}