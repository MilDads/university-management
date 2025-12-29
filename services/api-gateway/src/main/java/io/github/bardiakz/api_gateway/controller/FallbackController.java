package io.github.bardiakz.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<?> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Auth Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/user")
    public ResponseEntity<?> userFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "User Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/resource")
    public ResponseEntity<?> resourceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Resource Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/booking")
    public ResponseEntity<?> bookingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Booking Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/marketplace")
    public ResponseEntity<?> marketplaceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Marketplace Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/payment")
    public ResponseEntity<?> paymentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Payment Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/exam")
    public ResponseEntity<?> examFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Exam Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/notification")
    public ResponseEntity<?> notificationFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Notification Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/iot")
    public ResponseEntity<?> iotFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "IoT Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/tracking")
    public ResponseEntity<?> trackingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Tracking Service is currently unavailable",
                        "message", "Please try again later"
                ));
    }
}