package io.github.bardiakz.marketplace_service.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// Order DTOs
public record CreateOrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> items
) {}
