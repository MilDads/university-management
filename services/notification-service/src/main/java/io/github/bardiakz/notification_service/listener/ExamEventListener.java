package io.github.bardiakz.notification_service.listener;

import io.github.bardiakz.notification_service.entity.NotificationType;
import io.github.bardiakz.notification_service.event.ExamCreatedEvent;
import io.github.bardiakz.notification_service.event.ExamGradedEvent;
import io.github.bardiakz.notification_service.event.ExamSubmittedEvent;
import io.github.bardiakz.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class ExamEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ExamEventListener.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationService notificationService;

    public ExamEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.exam.created}")
    public void handleExamCreated(ExamCreatedEvent event) {
        logger.info("Received ExamCreatedEvent for exam: {}", event.getExamId());

        try {
            Map<String, String> variables = Map.of(
                    "examTitle", event.getExamTitle(),
                    "scheduledAt", event.getScheduledAt().format(DATE_FORMATTER),
                    "duration", String.valueOf(event.getDuration())
            );

            // Send to all registered students
            for (String studentEmail : event.getStudentEmails()) {
                try {
                    notificationService.createFromTemplate(
                            "exam-created",
                            studentEmail,
                            NotificationType.EXAM_CREATED,
                            variables,
                            null // We don't have individual userId in this event
                    );
                } catch (Exception e) {
                    logger.error("Failed to send exam notification to {}: {}", 
                            studentEmail, e.getMessage());
                }
            }

            logger.info("Exam notification emails sent for exam: {}", event.getExamTitle());

        } catch (Exception e) {
            logger.error("Failed to process exam created event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.exam.submitted}")
    public void handleExamSubmitted(ExamSubmittedEvent event) {
        logger.info("Received ExamSubmittedEvent for submission: {}", event.getSubmissionId());

        // This could send a confirmation to the student
        // For now, we'll just log it
        logger.info("Exam submitted by: {}", event.getStudentEmail());
    }

    @RabbitListener(queues = "${rabbitmq.queue.exam.graded}")
    public void handleExamGraded(ExamGradedEvent event) {
        logger.info("Received ExamGradedEvent for submission: {}", event.getSubmissionId());

        try {
            Map<String, String> variables = Map.of(
                    "examTitle", event.getExamTitle(),
                    "score", String.valueOf(event.getScore()),
                    "maxScore", String.valueOf(event.getMaxScore()),
                    "feedback", event.getFeedback() != null ? event.getFeedback() : "No feedback provided"
            );

            notificationService.createFromTemplate(
                    "exam-graded",
                    event.getStudentEmail(),
                    NotificationType.EXAM_GRADED,
                    variables,
                    null
            );

            logger.info("Exam graded email sent to: {}", event.getStudentEmail());

        } catch (Exception e) {
            logger.error("Failed to send exam graded email: {}", e.getMessage(), e);
        }
    }
}
