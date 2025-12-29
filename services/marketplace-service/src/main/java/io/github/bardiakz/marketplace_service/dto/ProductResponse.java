package io.github.bardiakz.marketplace_service.dto;

import io.github.bardiakz.marketplace_service.model.Product;
import io.github.bardiakz.marketplace_service.model.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        ProductCategory category,
        String sellerId,
        Boolean active,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getSellerId(),
                product.getActive(),
                product.getCreatedAt()
        );
    }
}
