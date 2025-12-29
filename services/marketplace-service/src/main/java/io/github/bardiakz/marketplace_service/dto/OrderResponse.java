package io.github.bardiakz.marketplace_service.dto;

import io.github.bardiakz.marketplace_service.model.Order;
import io.github.bardiakz.marketplace_service.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse(
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
