package io.github.bardiakz.tracking_service.controller;

import io.github.bardiakz.tracking_service.dto.LocationUpdateRequest;
import io.github.bardiakz.tracking_service.dto.ShuttleLocationResponse;
import io.github.bardiakz.tracking_service.dto.ShuttleResponse;
import io.github.bardiakz.tracking_service.model.ShuttleStatus;
import io.github.bardiakz.tracking_service.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for shuttle tracking
 */
@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = "*") // Configure properly in production
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Update shuttle location (called by GPS device/simulator)
     * POST /api/tracking/location
     */
    @PostMapping("/location")
    public ResponseEntity<ShuttleLocationResponse> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request) {

        ShuttleLocationResponse response = trackingService.updateLocation(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active shuttle locations
     * GET /api/tracking/shuttles/active
     */
    @GetMapping("/shuttles/active")
    public ResponseEntity<List<ShuttleLocationResponse>> getActiveShuttles() {
        List<ShuttleLocationResponse> shuttles = trackingService.getActiveShuttleLocations();
        return ResponseEntity.ok(shuttles);
    }

    /**
     * Get specific shuttle location
     * GET /api/tracking/shuttles/{id}/location
     */
    @GetMapping("/shuttles/{id}/location")
    public ResponseEntity<ShuttleLocationResponse> getShuttleLocation(@PathVariable Long id) {
        ShuttleLocationResponse location = trackingService.getShuttleLocation(id);
        return ResponseEntity.ok(location);
    }

    /**
     * Get all shuttles (admin)
     * GET /api/tracking/shuttles
     */
    @GetMapping("/shuttles")
    public ResponseEntity<List<ShuttleResponse>> getAllShuttles() {
        List<ShuttleResponse> shuttles = trackingService.getAllShuttles();
        return ResponseEntity.ok(shuttles);
    }

    /**
     * Register new shuttle (admin)
     * POST /api/tracking/shuttles
     */
    @PostMapping("/shuttles")
    public ResponseEntity<ShuttleResponse> registerShuttle(
            @RequestParam String vehicleNumber,
            @RequestParam String routeName,
            @RequestParam Integer capacity) {

        ShuttleResponse shuttle = trackingService.registerShuttle(vehicleNumber, routeName, capacity);
        return ResponseEntity.status(HttpStatus.CREATED).body(shuttle);
    }

    /**
     * Update shuttle status (admin)
     * PATCH /api/tracking/shuttles/{id}/status
     */
    @PatchMapping("/shuttles/{id}/status")
    public ResponseEntity<Void> updateShuttleStatus(
            @PathVariable Long id,
            @RequestParam ShuttleStatus status) {

        trackingService.updateShuttleStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
