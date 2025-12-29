package io.github.bardiakz.marketplace_service.service;

import io.github.bardiakz.marketplace_service.dto.*;
import io.github.bardiakz.marketplace_service.event.OrderEventPublisher;
import io.github.bardiakz.marketplace_service.model.*;
import io.github.bardiakz.marketplace_service.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create order and start Saga
     * Step 1 (T1): Create order and decrease stock
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userId) {
        log.info("Creating order for user: {}", userId);

        // Validate and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order(userId, totalAmount);

        for (OrderItemRequest itemReq : request.items()) {
            // Get product and check stock
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Product not found: " + itemReq.productId()));

            if (!product.getActive()) {
                throw new IllegalStateException("Product is not available");
            }

            if (product.getStock() < itemReq.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName());
            }

            // Decrease stock (T1 - Transaction step)
            product.decreaseStock(itemReq.quantity());
            productRepository.save(product);

            // Create order item
            BigDecimal itemTotal = product.getPrice().multiply(
                    BigDecimal.valueOf(itemReq.quantity()));
            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    itemReq.quantity(),
                    product.getPrice()
            );
            order.addItem(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        order.markAsPaymentPending();

        Order savedOrder = orderRepository.save(order);

        // Publish OrderCreated event (start Saga)
        eventPublisher.publishOrderCreated(savedOrder);

        log.info("Order created with ID: {} - Waiting for payment", savedOrder.getId());
        return OrderResponse.from(savedOrder);
    }

    /**
     * Handle PaymentCompleted event from Payment Service
     * Step 2 (T2): Mark order as completed
     */
    @Transactional
    public void handlePaymentCompleted(Long orderId, String paymentId) {
        log.info("Handling payment completed for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        order.markAsCompleted(paymentId);
        orderRepository.save(order);

        log.info("Order {} completed successfully", orderId);
    }

    /**
     * Handle PaymentFailed event from Payment Service
     * Compensation (C1): Restore stock
     */
    @Transactional
    public void handlePaymentFailed(Long orderId, String reason) {
        log.warn("Handling payment failed for order: {} - Reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        // Compensation: Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));

            product.increaseStock(item.getQuantity());
            productRepository.save(product);
            log.info("Compensated: Restored {} units of product {}",
                    item.getQuantity(), product.getName());
        }

        order.markAsFailed(reason);
        orderRepository.save(order);

        log.info("Order {} failed and compensated", orderId);
    }

    public List<OrderResponse> getMyOrders(String userId) {
        log.debug("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return OrderResponse.from(order);
    }

    @Transactional
    public void cancelOrder(Long id, String userId) {
        log.info("Cancelling order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }

        order.markAsCancelled();
        orderRepository.save(order);

        log.info("Order cancelled successfully");
    }
}

class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}

class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}