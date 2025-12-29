package com.university.marketplace.event;

public class PaymentResultEvent {
    private Long orderId;
    private boolean success;
    private String reason;

    public PaymentResultEvent() {}

    public PaymentResultEvent(Long orderId, boolean success, String reason) {
        this.orderId = orderId;
        this.success = success;
        this.reason = reason;
    }

    public Long getOrderId() { return orderId; }
    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setReason(String reason) { this.reason = reason; }
}
