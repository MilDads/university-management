package com.university.marketplace.service;

import com.university.marketplace.dto.CreateProductRequest;
import com.university.marketplace.dto.ProductResponse;
import com.university.marketplace.entity.Product;
import com.university.marketplace.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public ProductResponse create(CreateProductRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.setActive(true);

        Product saved = repo.save(p);

        return new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getStock(),
                saved.getActive(),
                saved.getCreatedAt()
        );
    }

    public List<ProductResponse> listActive() {
        return repo.findByActiveTrueOrderByIdDesc()
                .stream()
                .map(p -> new ProductResponse(
                        p.getId(), p.getName(), p.getDescription(),
                        p.getPrice(), p.getStock(), p.getActive(), p.getCreatedAt()
                ))
                .toList();
    }

    public ProductResponse getById(Long id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(),
                p.getPrice(), p.getStock(), p.getActive(), p.getCreatedAt()
        );
    }
}
