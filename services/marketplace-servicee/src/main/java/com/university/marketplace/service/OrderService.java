package com.university.marketplace.service;

import com.university.marketplace.dto.OrderResponse;
import com.university.marketplace.dto.PlaceOrderRequest;
import com.university.marketplace.entity.Order;
import com.university.marketplace.entity.OrderItem;
import com.university.marketplace.entity.OrderStatus;
import com.university.marketplace.entity.Product;
import com.university.marketplace.event.MarketplaceEventPublisher;
import com.university.marketplace.event.OrderCreatedEvent;
import com.university.marketplace.repository.OrderRepository;
import com.university.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final MarketplaceEventPublisher publisher;   // ✅ اضافه شد

    public OrderService(OrderRepository orderRepo,
                        ProductRepository productRepo,
                        MarketplaceEventPublisher publisher) { // ✅ اضافه شد
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.publisher = publisher; // ✅ اضافه شد
    }

    @Transactional
    public OrderResponse placeOrder(String buyerId, PlaceOrderRequest req) {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;

        for (PlaceOrderRequest.Item it : req.getItems()) {
            Product p = productRepo.findById(it.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + it.getProductId()));

            if (!Boolean.TRUE.equals(p.getActive())) {
                throw new RuntimeException("Product is inactive: " + p.getId());
            }

            int qty = it.getQuantity();
            if (p.getStock() < qty) {
                throw new RuntimeException("Not enough stock for product " + p.getId());
            }

            // کم کردن موجودی
            p.setStock(p.getStock() - qty);
            productRepo.save(p);

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setProductNameSnapshot(p.getName());
            oi.setQuantity(qty);
            oi.setUnitPrice(p.getPrice());

            order.addItem(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        Order saved = orderRepo.save(order);

        // ✅ بعد از اینکه سفارش ذخیره شد (id داریم)، event می‌فرستیم
        publisher.publishOrderCreated(new OrderCreatedEvent(
                saved.getId(),
                saved.getBuyerId(),
                saved.getTotalAmount(),
                System.currentTimeMillis()
        ));

        return toResponse(saved);
    }

    public List<OrderResponse> myOrders(String buyerId) {
        return orderRepo.findByBuyerIdOrderByIdDesc(buyerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrder(Long orderId, String requesterId, boolean isAdmin) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!isAdmin && !o.getBuyerId().equals(requesterId)) {
            throw new RuntimeException("Forbidden");
        }

        return toResponse(o);
    }

    private OrderResponse toResponse(Order o) {
        List<OrderResponse.Item> items = o.getItems().stream()
                .map(i -> new OrderResponse.Item(
                        i.getProductId(),
                        i.getProductNameSnapshot(),
                        i.getQuantity(),
                        i.getUnitPrice()
                ))
                .toList();

        return new OrderResponse(
                o.getId(),
                o.getBuyerId(),
                o.getStatus(),
                o.getTotalAmount(),
                o.getCreatedAt(),
                items
        );
    }

    public OrderStatus getOrderStatus(Long orderId, String requesterId, boolean isAdmin) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!isAdmin && !o.getBuyerId().equals(requesterId)) {
            throw new RuntimeException("Forbidden");
        }

        return o.getStatus();
    }

}
