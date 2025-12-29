package io.github.bardiakz.marketplace_service.repository;

import io.github.bardiakz.marketplace_service.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByActiveTrue();
    List<Product> findBySellerId(String sellerId);
    List<Product> findByNameContainingIgnoreCase(String name);
}

@Repository
interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}