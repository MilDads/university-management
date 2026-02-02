package io.github.bardiakz.notification_service.dto;

import io.github.bardiakz.notification_service.entity.NotificationStatus;
import io.github.bardiakz.notification_service.entity.NotificationType;
import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String recipientEmail;
    private String subject;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Integer retryCount;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, String recipientEmail, String subject, 
                               NotificationType type, NotificationStatus status,
                               LocalDateTime createdAt, LocalDateTime sentAt, Integer retryCount) {
        this.id = id;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.retryCount = retryCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}
