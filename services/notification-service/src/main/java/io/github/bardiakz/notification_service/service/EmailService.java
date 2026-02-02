package io.github.bardiakz.notification_service.service;

import io.github.bardiakz.notification_service.exception.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${notification.from.email}")
    private String fromEmail;

    @Value("${notification.from.name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send email asynchronously
     */
    @Async("emailExecutor")
    public CompletableFuture<Boolean> sendEmailAsync(String to, String subject, String body, boolean isHtml) {
        try {
            sendEmail(to, subject, body, isHtml);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Async email sending failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Send email synchronously
     */
    public void sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new EmailDeliveryException("Failed to send email", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", to, e.getMessage());
            throw new EmailDeliveryException("Unexpected error sending email", e);
        }
    }

    /**
     * Send plain text email
     */
    public void sendPlainTextEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, false);
    }

    /**
     * Send HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        sendEmail(to, subject, htmlBody, true);
    }
}
