package com.university.marketplace.event;

import com.university.marketplace.entity.Order;
import com.university.marketplace.entity.OrderStatus;
import com.university.marketplace.repository.OrderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final OrderRepository orderRepo;

    public PaymentEventListener(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @RabbitListener(queues = "payment.completed.queue")
    public void onPaymentCompleted(PaymentResultEvent event) {
        Order o = orderRepo.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        // اگر قبلاً نهایی شده، دوباره تغییر نده
        if (o.getStatus() == OrderStatus.PAID) return;

        o.setStatus(OrderStatus.PAID);
        orderRepo.save(o);
    }

    @RabbitListener(queues = "payment.failed.queue")
    public void onPaymentFailed(PaymentResultEvent event) {
        Order o = orderRepo.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        if (o.getStatus() == OrderStatus.PAID) return;

        o.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepo.save(o);
    }
}
