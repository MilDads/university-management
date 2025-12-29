package com.university.marketplace.dto;

import com.university.marketplace.entity.OrderStatus;

public class OrderStatusResponse {
    private Long orderId;
    private OrderStatus status;

    public OrderStatusResponse(Long orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
