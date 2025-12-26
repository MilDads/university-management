package io.github.bardiakz.payment_service.controller;

import io.github.bardiakz.payment_service.dto.PaymentResponse;
import io.github.bardiakz.payment_service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/my-payments")
    public ResponseEntity<?> getMyPayments(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<PaymentResponse> payments = paymentService.getMyPayments(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            PaymentResponse response = paymentService.getPaymentById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}