package io.github.bardiakz.resource_service.controller;

import io.github.bardiakz.resource_service.dto.CreateResourceRequest;
import io.github.bardiakz.resource_service.dto.ResourceResponse;
import io.github.bardiakz.resource_service.model.ResourceStatus;
import io.github.bardiakz.resource_service.model.ResourceType;
import io.github.bardiakz.resource_service.service.ResourceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * Create a new resource - FACULTY only
     * API Gateway forwards user info via headers: X-User-Id, X-User-Role
     */
    @PostMapping
    public ResponseEntity<?> createResource(
            @Valid @RequestBody CreateResourceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Create resource request received from user: {} with role: {}", userId, userRole);

        // RBAC: Only FACULTY can create resources
        if (!"FACULTY".equals(userRole)) {
            log.warn("Unauthorized attempt to create resource by user: {} with role: {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only FACULTY members can create resources"));
        }

        try {
            ResourceResponse response = resourceService.createResource(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating resource", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create resource"));
        }
    }

    /**
     * Get all resources - Available to all authenticated users
     */
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources() {
        log.debug("Get all resources request received");
        List<ResourceResponse> resources = resourceService.getAllResources();
        return ResponseEntity.ok(resources);
    }

    /**
     * Get resource by ID - Available to all authenticated users
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getResourceById(@PathVariable Long id) {
        log.debug("Get resource by ID request: {}", id);
        try {
            ResourceResponse response = resourceService.getResourceById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching resource", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get resources by type - Available to all authenticated users
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ResourceResponse>> getResourcesByType(@PathVariable ResourceType type) {
        log.debug("Get resources by type request: {}", type);
        List<ResourceResponse> resources = resourceService.getResourcesByType(type);
        return ResponseEntity.ok(resources);
    }

    /**
     * Get available resources by type - Available to all authenticated users
     */
    @GetMapping("/type/{type}/available")
    public ResponseEntity<List<ResourceResponse>> getAvailableResourcesByType(@PathVariable ResourceType type) {
        log.debug("Get available resources by type request: {}", type);
        List<ResourceResponse> resources = resourceService.getAvailableResourcesByType(type);
        return ResponseEntity.ok(resources);
    }

    /**
     * Get resources by status - Available to all authenticated users
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ResourceResponse>> getResourcesByStatus(@PathVariable ResourceStatus status) {
        log.debug("Get resources by status request: {}", status);
        List<ResourceResponse> resources = resourceService.getResourcesByStatus(status);
        return ResponseEntity.ok(resources);
    }

    /**
     * Update resource status - FACULTY only
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateResourceStatus(
            @PathVariable Long id,
            @RequestParam ResourceStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Update resource status request for ID: {} to status: {}", id, status);

        // RBAC: Only FACULTY can update resource status
        if (!"FACULTY".equals(userRole)) {
            log.warn("Unauthorized attempt to update resource status");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only FACULTY members can update resource status"));
        }

        try {
            ResourceResponse response = resourceService.updateResourceStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating resource status", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete resource - FACULTY only
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Delete resource request for ID: {}", id);

        // RBAC: Only FACULTY can delete resources
        if (!"FACULTY".equals(userRole)) {
            log.warn("Unauthorized attempt to delete resource");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only FACULTY members can delete resources"));
        }

        try {
            resourceService.deleteResource(id);
            return ResponseEntity.ok(Map.of("message", "Resource deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting resource", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}