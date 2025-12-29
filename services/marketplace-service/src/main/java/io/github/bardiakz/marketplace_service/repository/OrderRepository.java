package io.github.bardiakz.marketplace_service.repository;

import io.github.bardiakz.marketplace_service.model.Order;
import io.github.bardiakz.marketplace_service.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);
}
