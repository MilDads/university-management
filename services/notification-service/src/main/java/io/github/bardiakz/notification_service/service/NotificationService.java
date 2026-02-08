package io.github.bardiakz.notification_service.service;

import io.github.bardiakz.notification_service.dto.EmailTemplate;
import io.github.bardiakz.notification_service.dto.NotificationRequest;
import io.github.bardiakz.notification_service.dto.NotificationResponse;
import io.github.bardiakz.notification_service.entity.Notification;
import io.github.bardiakz.notification_service.entity.NotificationStatus;
import io.github.bardiakz.notification_service.entity.NotificationType;
import io.github.bardiakz.notification_service.exception.EmailDeliveryException;
import io.github.bardiakz.notification_service.exception.NotificationException;
import io.github.bardiakz.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main notification service - orchestrates notification creation and delivery
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TemplateService templateService;

    @Value("${notification.retry.max-attempts}")
    private Integer maxRetryAttempts;

    @Value("${notification.retry.delay-ms}")
    private Long retryDelayMs;

    // NEW: Allow disabling email sending (default: true)
    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService,
                               TemplateService templateService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.templateService = templateService;
    }

    /**
     * Create and send a notification
     */
    @Transactional
    public NotificationResponse createAndSendNotification(NotificationRequest request) {
        logger.info("Creating notification for: {}", request.getRecipientEmail());

        // Create notification entity
        Notification notification = new Notification(
                request.getRecipientEmail(),
                request.getSubject(),
                request.getBody(),
                request.getType()
        );
        notification.setUserId(request.getUserId());
        notification.setStatus(NotificationStatus.PENDING);

        // Save to database
        notification = notificationRepository.save(notification);

        // Attempt to send (gracefully handles SMTP not configured)
        try {
            sendNotification(notification);
        } catch (Exception e) {
            logger.warn("Failed to send notification immediately: {}", e.getMessage());
            // Notification is still saved, just not sent via email
        }

        return toResponse(notification);
    }

    /**
     * Send notification using email service
     * UPDATED: Works even without SMTP configured
     */
    @Transactional
    public void sendNotification(Notification notification) {

        if (!emailEnabled) {
            // Email sending disabled - just mark as sent (in-app only)
            logger.info("Email sending disabled - notification {} stored for in-app display only",
                    notification.getId());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            return;
        }

        // Email enabled - try to send
        try {
            emailService.sendHtmlEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getBody()
            );

            // Update status to SENT
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            logger.info("Notification {} sent successfully via email", notification.getId());

        } catch (EmailDeliveryException e) {
            // Email failed but notification is still in DB for in-app display
            logger.warn("Email delivery failed for notification {}: {}",
                    notification.getId(), e.getMessage());
            handleSendFailure(notification, e);
        } catch (Exception e) {
            // SMTP not configured or other error
            logger.warn("Email service unavailable for notification {}: {}. " +
                            "Notification saved for in-app display only.",
                    notification.getId(), e.getMessage());

            // Mark as sent anyway (user can see it in-app)
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage("Email not sent: " + e.getMessage());
            notificationRepository.save(notification);
        }
    }

    /**
     * Create notification from template
     */
    @Transactional
    public NotificationResponse createFromTemplate(String templateName, String recipientEmail,
                                                   NotificationType type, Map<String, String> variables,
                                                   Long userId) {
        try {
            EmailTemplate template;

            try {
                template = templateService.loadTemplate(templateName, variables);
            } catch (Exception e) {
                // Template loading failed - use simple template
                logger.warn("Failed to load template {}, using simple template: {}",
                        templateName, e.getMessage());
                String message = variables.getOrDefault("message",
                        "You have a new notification");
                template = templateService.createSimpleTemplate(
                        "Notification from University", message);
            }

            NotificationRequest request = new NotificationRequest(
                    recipientEmail,
                    template.getSubject(),
                    template.getBody(),
                    type
            );
            request.setUserId(userId);

            return createAndSendNotification(request);

        } catch (Exception e) {
            logger.error("Failed to create notification from template {}: {}",
                    templateName, e.getMessage());
            throw new NotificationException("Template processing failed", e);
        }
    }

    /**
     * Handle notification send failure
     */
    private void handleSendFailure(Notification notification, Exception e) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setErrorMessage(e.getMessage());

        if (notification.getRetryCount() >= maxRetryAttempts) {
            // Max retries reached - mark as sent anyway (user can see in-app)
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            logger.info("Notification {} marked as sent after {} failed email attempts. " +
                    "Available in-app.", notification.getId(), maxRetryAttempts);
        } else {
            notification.setStatus(NotificationStatus.RETRY);
            logger.warn("Notification {} will retry email. Attempt {}/{}",
                    notification.getId(), notification.getRetryCount(), maxRetryAttempts);
        }

        notificationRepository.save(notification);
    }

    /**
     * Retry failed notifications (scheduled task)
     * Only retries if email is enabled
     */
    @Scheduled(fixedDelayString = "${notification.retry.delay-ms}")
    @Transactional
    public void retryFailedNotifications() {
        if (!emailEnabled) {
            return; // Skip retries if email disabled
        }

        List<Notification> failedNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.RETRY, maxRetryAttempts);

        if (!failedNotifications.isEmpty()) {
            logger.info("Retrying {} failed notifications", failedNotifications.size());

            for (Notification notification : failedNotifications) {
                try {
                    sendNotification(notification);
                } catch (Exception e) {
                    logger.error("Retry failed for notification {}: {}",
                            notification.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Get notification by ID
     */
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification not found with id: " + id));
        return toResponse(notification);
    }

    /**
     * Get notifications by user ID
     */
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications by email
     */
    public List<NotificationResponse> getNotificationsByEmail(String email) {
        return notificationRepository.findByRecipientEmail(email).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get notification statistics
     */
    public Map<String, Long> getNotificationStats() {
        return Map.of(
                "total", notificationRepository.count(),
                "sent", notificationRepository.countByStatus(NotificationStatus.SENT),
                "pending", notificationRepository.countByStatus(NotificationStatus.PENDING),
                "failed", notificationRepository.countByStatus(NotificationStatus.FAILED),
                "retry", notificationRepository.countByStatus(NotificationStatus.RETRY)
        );
    }

    /**
     * Convert entity to response DTO
     */
    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientEmail(),
                notification.getSubject(),
                notification.getBody(),
                notification.getType(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getRetryCount()
        );
    }
}