package com.university.marketplace.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PlaceOrderRequest {

    @NotNull
    @Size(min = 1)
    private List<Item> items;

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        @NotNull
        private Long productId;

        @NotNull
        @Min(1)
        private Integer quantity;

        public Long getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
        public void setProductId(Long productId) { this.productId = productId; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
