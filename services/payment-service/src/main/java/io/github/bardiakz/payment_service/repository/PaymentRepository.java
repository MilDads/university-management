package io.github.bardiakz.payment_service.repository;

import io.github.bardiakz.payment_service.model.Payment;
import io.github.bardiakz.payment_service.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByUserId(String userId);
    List<Payment> findByStatus(PaymentStatus status);
}