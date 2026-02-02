package io.github.bardiakz.notification_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderCreatedEvent {
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("orderId")
    private Long orderId;
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("userEmail")
    private String userEmail;
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public OrderCreatedEvent() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
