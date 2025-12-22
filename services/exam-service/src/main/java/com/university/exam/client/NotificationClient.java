@Component
public class NotificationClient {


private final RestTemplate restTemplate;


public NotificationClient(RestTemplate restTemplate) {
this.restTemplate = restTemplate;
}


@CircuitBreaker(name = "notificationService", fallbackMethod = "fallback")
public void sendExamNotification(String examTitle) {
restTemplate.postForObject(
"http://notification-service/notify",
examTitle,
Void.class
);
}


public void fallback(String examTitle, Throwable t) {
System.out.println("Notification service unavailable for exam: " + examTitle);
}
}