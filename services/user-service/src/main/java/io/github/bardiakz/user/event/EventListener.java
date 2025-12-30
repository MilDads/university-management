package io.github.bardiakz.user.event;

import io.github.bardiakz.user.service.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);
    private final UserProfileService userProfileService;

    public EventListener(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.user-registered}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegistered event for username: {}", event.getUsername());

        try {
            userProfileService.createProfileFromEvent(event);
            log.info("Profile created successfully for username: {}", event.getUsername());
        } catch (Exception e) {
            log.error("Failed to create profile for username: {}", event.getUsername(), e);
            // Event will go to DLQ for retry/inspection
            throw new RuntimeException("Profile creation failed", e);
        }
    }
}
