package com.university.exam.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallback")
    public void sendExamNotification(String message) {
        NotificationRequest request = new NotificationRequest(message);

        restTemplate.postForObject(
                "http://notification-service:8088/api/notifications",
                request,
                Void.class
        );
    }

    public void fallback(String message, Throwable t) {
        System.err.println("Circuit Breaker فعال شد - سرویس نوتیفیکیشن در دسترس نیست. پیام از دست رفت: " + message);
        System.err.println("علت: " + t.getMessage());
    }
}