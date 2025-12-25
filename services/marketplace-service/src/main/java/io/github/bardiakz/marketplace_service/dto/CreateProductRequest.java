package io.github.bardiakz.marketplace_service.dto;

import io.github.bardiakz.marketplace_service.model.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Product DTOs
public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(min = 3, max = 200)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock,

        @NotNull(message = "Category is required")
        ProductCategory category
) {}

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        ProductCategory category,
        String sellerId,
        Boolean active,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getSellerId(),
                product.getActive(),
                product.getCreatedAt()
        );
    }
}

// Order DTOs
public record CreateOrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> items
) {}

record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}

record OrderResponse(
        Long id,
        String userId,
        BigDecimal totalAmount,
        OrderStatus status,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        String paymentId
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()),
                order.getCreatedAt(),
                order.getPaymentId()
        );
    }
}

record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}