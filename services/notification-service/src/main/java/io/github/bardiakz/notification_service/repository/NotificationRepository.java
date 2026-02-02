package io.github.bardiakz.notification_service.repository;

import io.github.bardiakz.notification_service.entity.Notification;
import io.github.bardiakz.notification_service.entity.NotificationStatus;
import io.github.bardiakz.notification_service.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByStatus(NotificationStatus status);
    
    List<Notification> findByRecipientEmail(String recipientEmail);
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByType(NotificationType type);
    
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetry);
    
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    Long countByStatus(NotificationStatus status);
}
