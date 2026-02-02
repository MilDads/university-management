package io.github.bardiakz.notification_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentCompletedEvent {
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("paymentId")
    private Long paymentId;
    @JsonProperty("orderId")
    private Long orderId;
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("userEmail")
    private String userEmail;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("transactionId")
    private String transactionId;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public PaymentCompletedEvent() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
