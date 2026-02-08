package io.github.bardiakz.exam_service.service;

import io.github.bardiakz.exam_service.event.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EventPublisher eventPublisher;

    public NotificationService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Sends exam start notification with Circuit Breaker pattern.
     * If notification service fails, circuit opens and fallback method is called.
     *
     * @param event ExamStartedEvent to be published
     */
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyExamStartFallback")
    public void notifyExamStart(ExamStartedEvent event) {
        log.info("Attempting to send exam start notification for exam ID: {}", event.examId());
        eventPublisher.publishExamStartedEvent(event);
        log.info("Successfully sent exam start notification for exam ID: {}", event.examId());
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyExamCreatedFallback")
    public void notifyExamCreated(ExamCreatedEvent event) {
        log.info("Attempting to send exam created notification for exam ID: {}", event.examId());
        eventPublisher.publishExamCreatedEvent(event);
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyExamSubmittedFallback")
    public void notifyExamSubmitted(ExamSubmittedEvent event) {
        log.info("Attempting to send exam submitted notification for submission ID: {}", event.submissionId());
        eventPublisher.publishExamSubmittedEvent(event);
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyExamGradedFallback")
    public void notifyExamGraded(ExamGradedEvent event) {
        log.info("Attempting to send exam graded notification for submission ID: {}", event.submissionId());
        eventPublisher.publishExamGradedEvent(event);
    }

    /**
     * Fallback method when circuit breaker is open or service fails.
     * Logs the failure and allows the system to continue.
     *
     * @param event ExamStartedEvent that failed to send
     * @param throwable Exception that triggered the fallback
     */
    private void notifyExamStartFallback(ExamStartedEvent event, Throwable throwable) {
        log.warn("Circuit breaker OPEN or notification failed for exam ID: {}. Fallback triggered. Reason: {}",
                event.examId(), throwable.getMessage());
        log.info("Exam {} will proceed without notification. Notification will be retried when service recovers.",
                event.examId());
    }

    private void notifyExamCreatedFallback(ExamCreatedEvent event, Throwable throwable) {
        log.warn("Fallback: Failed to publish ExamCreatedEvent for exam {}: {}", event.examId(), throwable.getMessage());
    }

    private void notifyExamSubmittedFallback(ExamSubmittedEvent event, Throwable throwable) {
        log.warn("Fallback: Failed to publish ExamSubmittedEvent for submission {}: {}", event.submissionId(), throwable.getMessage());
    }

    private void notifyExamGradedFallback(ExamGradedEvent event, Throwable throwable) {
        log.warn("Fallback: Failed to publish ExamGradedEvent for submission {}: {}", event.submissionId(), throwable.getMessage());
    }
}