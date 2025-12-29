package com.university.marketplace.event;

import java.math.BigDecimal;

public class OrderCreatedEvent {
    private Long orderId;
    private String buyerId;
    private BigDecimal totalAmount;
    private long timestamp;

    public OrderCreatedEvent() {}

    public OrderCreatedEvent(Long orderId, String buyerId, BigDecimal totalAmount, long timestamp) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
    }

    public Long getOrderId() { return orderId; }
    public String getBuyerId() { return buyerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public long getTimestamp() { return timestamp; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
