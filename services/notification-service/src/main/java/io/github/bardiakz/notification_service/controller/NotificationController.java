package io.github.bardiakz.notification_service.controller;

import io.github.bardiakz.notification_service.dto.NotificationRequest;
import io.github.bardiakz.notification_service.dto.NotificationResponse;
import io.github.bardiakz.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for notification management
 * Accessible only via internal API (validated by InternalApiValidator filter)
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Send a notification manually
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        logger.info("Manual notification request for: {}", request.getRecipientEmail());
        
        NotificationResponse response = notificationService.createAndSendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        logger.info("Fetching notification with id: {}", id);
        
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all notifications for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(
            @PathVariable Long userId) {
        logger.info("Fetching notifications for user: {}", userId);
        
        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get all notifications for an email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByEmail(
            @PathVariable String email) {
        logger.info("Fetching notifications for email: {}", email);
        
        List<NotificationResponse> notifications = notificationService.getNotificationsByEmail(email);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        logger.info("Fetching notification statistics");
        
        Map<String, Long> stats = notificationService.getNotificationStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-service"
        ));
    }

    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        logger.error("Controller exception: {}", e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
        );
    }
}
