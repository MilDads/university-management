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

        // Attempt to send
        try {
            sendNotification(notification);
            return toResponse(notification);
        } catch (Exception e) {
            logger.error("Failed to send notification immediately: {}", e.getMessage());
            // Will be retried by scheduler
            return toResponse(notification);
        }
    }

    /**
     * Send notification using email service
     */
    @Transactional
    public void sendNotification(Notification notification) {
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

            logger.info("Notification {} sent successfully", notification.getId());

        } catch (EmailDeliveryException e) {
            handleSendFailure(notification, e);
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
            EmailTemplate template = templateService.loadTemplate(templateName, variables);

            NotificationRequest request = new NotificationRequest(
                    recipientEmail,
                    template.getSubject(),
                    template.getBody(),
                    type
            );
            request.setUserId(userId);

            return createAndSendNotification(request);

        } catch (Exception e) {
            logger.error("Failed to create notification from template {}: {}", templateName, e.getMessage());
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
            notification.setStatus(NotificationStatus.FAILED);
            logger.error("Notification {} failed after {} attempts", 
                    notification.getId(), maxRetryAttempts);
        } else {
            notification.setStatus(NotificationStatus.RETRY);
            logger.warn("Notification {} failed, will retry. Attempt {}/{}",
                    notification.getId(), notification.getRetryCount(), maxRetryAttempts);
        }

        notificationRepository.save(notification);
    }

    /**
     * Retry failed notifications (scheduled task)
     */
    @Scheduled(fixedDelayString = "${notification.retry.delay-ms}")
    @Transactional
    public void retryFailedNotifications() {
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
                notification.getType(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getRetryCount()
        );
    }
}
