package com.university.marketplace.controller;

import com.university.marketplace.dto.OrderResponse;
import com.university.marketplace.dto.OrderStatusResponse;
import com.university.marketplace.dto.PlaceOrderRequest;
import com.university.marketplace.security.RequestAuth;
import com.university.marketplace.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/orders")
public class OrderController {

    private final OrderService service;
    private final RequestAuth auth;

    public OrderController(OrderService service, RequestAuth auth) {
        this.service = service;
        this.auth = auth;
    }

    // Place order - فقط دانشجو
    @PostMapping
    public OrderResponse place(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody PlaceOrderRequest req
    ) {
        try {
            auth.requireUserId(userId);
            auth.requireRole(role, "STUDENT", "ROLE_STUDENT");
            return service.placeOrder(userId, req);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Missing")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
            }
            if ("Forbidden".equals(ex.getMessage()) || (ex.getMessage() != null && ex.getMessage().startsWith("Forbidden"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    // My orders - هر کاربر فقط سفارش‌های خودش
    @GetMapping("/my")
    public List<OrderResponse> my(
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        try {
            auth.requireUserId(userId);
            return service.myOrders(userId);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    // Get order by id - مالک یا admin
    @GetMapping("/{id}")
    public OrderResponse get(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        try {
            auth.requireUserId(userId);
            boolean isAdmin = role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ROLE_ADMIN"));
            return service.getOrder(id, userId, isAdmin);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Missing")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
            }
            if ("Forbidden".equals(ex.getMessage()) || (ex.getMessage() != null && ex.getMessage().startsWith("Forbidden"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    // Track order status - مالک یا admin
    @GetMapping("/{id}/status")
    public OrderStatusResponse status(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        try {
            auth.requireUserId(userId);
            boolean isAdmin = role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ROLE_ADMIN"));
            return new OrderStatusResponse(id, service.getOrderStatus(id, userId, isAdmin));
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Missing")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
            }
            if ("Forbidden".equals(ex.getMessage()) || (ex.getMessage() != null && ex.getMessage().startsWith("Forbidden"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
