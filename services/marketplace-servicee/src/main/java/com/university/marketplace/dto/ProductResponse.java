package com.university.marketplace.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
    private Instant createdAt;

    public ProductResponse(Long id, String name, String description, BigDecimal price,
                           Integer stock, Boolean active, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public Boolean getActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
