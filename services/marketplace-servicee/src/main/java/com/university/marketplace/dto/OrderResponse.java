package com.university.marketplace.dto;

import com.university.marketplace.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private Long id;
    private String buyerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<Item> items;

    public OrderResponse(Long id, String buyerId, OrderStatus status,
                         BigDecimal totalAmount, Instant createdAt, List<Item> items) {
        this.id = id;
        this.buyerId = buyerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getId() { return id; }
    public String getBuyerId() { return buyerId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Item> getItems() { return items; }

    public static class Item {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        public Item(Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
    }
}
