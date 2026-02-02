package io.github.bardiakz.notification_service.listener;

import io.github.bardiakz.notification_service.entity.NotificationType;
import io.github.bardiakz.notification_service.event.UserRegisteredEvent;
import io.github.bardiakz.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserEventListener {

    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);
    private final NotificationService notificationService;

    public UserEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.user.registered}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        logger.info("Received UserRegisteredEvent for user: {}", event.getEmail());

        try {
            Map<String, String> variables = Map.of(
                    "firstName", event.getFirstName() != null ? event.getFirstName() : "",
                    "lastName", event.getLastName() != null ? event.getLastName() : "",
                    "email", event.getEmail()
            );

            notificationService.createFromTemplate(
                    "welcome",
                    event.getEmail(),
                    NotificationType.USER_REGISTRATION,
                    variables,
                    event.getUserId()
            );

            logger.info("Welcome email sent to: {}", event.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send welcome email: {}", e.getMessage(), e);
        }
    }
}
