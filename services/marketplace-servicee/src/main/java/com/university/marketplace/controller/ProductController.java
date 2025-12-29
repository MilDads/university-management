package com.university.marketplace.controller;

import com.university.marketplace.dto.CreateProductRequest;
import com.university.marketplace.dto.ProductResponse;
import com.university.marketplace.security.RequestAuth;
import com.university.marketplace.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/products")
public class ProductController {

    private final ProductService service;
    private final RequestAuth auth;

    public ProductController(ProductService service, RequestAuth auth) {
        this.service = service;
        this.auth = auth;
    }

    @PostMapping
    public ProductResponse create(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CreateProductRequest req
    ) {
        try {
            auth.requireUserId(userId);
            auth.requireRole(role, "ADMIN", "INSTRUCTOR");
            return service.create(req);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("Missing")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
    }

    @GetMapping
    public List<ProductResponse> list() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return service.getById(id);
    }
}
