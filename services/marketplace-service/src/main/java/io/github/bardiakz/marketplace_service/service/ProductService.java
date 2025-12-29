package io.github.bardiakz.marketplace_service.service;

import io.github.bardiakz.marketplace_service.dto.*;
import io.github.bardiakz.marketplace_service.model.Product;
import io.github.bardiakz.marketplace_service.model.ProductCategory;
import io.github.bardiakz.marketplace_service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, String sellerId) {
        log.info("Creating product: {} by seller: {}", request.name(), sellerId);

        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.category(),
                sellerId
        );

        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());

        return ProductResponse.from(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        log.debug("Fetching all active products");
        return productRepository.findByActiveTrue().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return ProductResponse.from(product);
    }

    public List<ProductResponse> getProductsByCategory(ProductCategory category) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsBySeller(String sellerId) {
        log.debug("Fetching products by seller: {}", sellerId);
        return productRepository.findBySellerId(sellerId).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.decreaseStock(quantity);
        productRepository.save(product);
        log.info("Decreased stock for product {}: -{}", productId, quantity);
    }

    @Transactional
    public void increaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.increaseStock(quantity);
        productRepository.save(product);
        log.info("Increased stock for product {}: +{}", productId, quantity);
    }

    @Transactional
    public void deleteProduct(Long id, String sellerId) {
        log.info("Deleting product {} by seller {}", id, sellerId);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedException("You can only delete your own products");
        }

        product.setActive(false);
        productRepository.save(product);
        log.info("Product marked as inactive");
    }
}

class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}

class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}