package io.github.bardiakz.tracking_service.service;

import io.github.bardiakz.tracking_service.dto.LocationUpdateRequest;
import io.github.bardiakz.tracking_service.dto.ShuttleLocationResponse;
import io.github.bardiakz.tracking_service.dto.ShuttleResponse;
import io.github.bardiakz.tracking_service.event.LocationEventPublisher;
import io.github.bardiakz.tracking_service.model.Location;
import io.github.bardiakz.tracking_service.model.Shuttle;
import io.github.bardiakz.tracking_service.model.ShuttleStatus;
import io.github.bardiakz.tracking_service.repository.ShuttleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);
    private static final String LOCATION_CACHE_PREFIX = "shuttle:location:";
    private static final long CACHE_TTL_MINUTES = 10;

    private final ShuttleRepository shuttleRepository;
    private final LocationEventPublisher eventPublisher;
    private final LocationBroadcastService broadcastService;
    private final RedisTemplate<String, Object> redisTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public TrackingService(ShuttleRepository shuttleRepository,
                           LocationEventPublisher eventPublisher,
                           LocationBroadcastService broadcastService,
                           RedisTemplate<String, Object> redisTemplate) {
        this.shuttleRepository = shuttleRepository;
        this.eventPublisher = eventPublisher;
        this.broadcastService = broadcastService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Update shuttle location - called by GPS device or simulator
     */
    @Transactional
    public ShuttleLocationResponse updateLocation(LocationUpdateRequest request) {
        log.info("Updating location for shuttle {}: [{}, {}]",
                request.shuttleId(), request.latitude(), request.longitude());

        Shuttle shuttle = shuttleRepository.findById(request.shuttleId())
                .orElseThrow(() -> {
                    log.error("Shuttle not found: {}", request.shuttleId());
                    return new IllegalArgumentException("Shuttle not found with ID: " + request.shuttleId());
                });

        try {
            // Create location history record
            Location location = new Location(shuttle, request.latitude(), request.longitude());
            location.setSpeed(request.speed());
            location.setHeading(request.heading());
            location.setAccuracy(request.accuracy());

            entityManager.persist(location);

            // Update shuttle's current location (denormalized for quick access)
            shuttle.updateLocation(request.latitude(), request.longitude());
            shuttleRepository.save(shuttle);

            // Build response first
            ShuttleLocationResponse response = ShuttleLocationResponse.from(shuttle);

            // Try to cache location in Redis (don't fail if Redis is down)
            try {
                cacheLocation(response);
                log.debug("Location cached successfully in Redis");
            } catch (Exception e) {
                log.warn("Failed to cache location in Redis (continuing anyway): {}", e.getMessage());
            }

            // Broadcast to WebSocket clients
            try {
                broadcastService.broadcastLocationUpdate(response);
                log.debug("Location broadcasted to WebSocket clients");
            } catch (Exception e) {
                log.warn("Failed to broadcast location via WebSocket: {}", e.getMessage());
            }

            // Publish event to RabbitMQ for other services
            try {
                eventPublisher.publishLocationUpdated(shuttle);
                log.debug("Location event published to RabbitMQ");
            } catch (Exception e) {
                log.warn("Failed to publish location event to RabbitMQ: {}", e.getMessage());
            }

            log.info("Location updated successfully for shuttle {}", shuttle.getId());
            return response;

        } catch (Exception e) {
            log.error("Error updating location for shuttle {}: {}", request.shuttleId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update shuttle location: " + e.getMessage(), e);
        }
    }

    /**
     * Get all active shuttles with their current locations
     */
    public List<ShuttleLocationResponse> getActiveShuttleLocations() {
        log.debug("Fetching all active shuttle locations");

        return shuttleRepository.findByStatusAndLastLocationUpdateIsNotNull(ShuttleStatus.ACTIVE)
                .stream()
                .map(ShuttleLocationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get specific shuttle location
     */
    public ShuttleLocationResponse getShuttleLocation(Long shuttleId) {
        log.debug("Fetching location for shuttle: {}", shuttleId);

        // Try cache first
        try {
            String cacheKey = LOCATION_CACHE_PREFIX + shuttleId;
            ShuttleLocationResponse cached = (ShuttleLocationResponse) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Location found in cache");
                return cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve from Redis cache: {}", e.getMessage());
        }

        // Fall back to database
        Shuttle shuttle = shuttleRepository.findById(shuttleId)
                .orElseThrow(() -> new IllegalArgumentException("Shuttle not found: " + shuttleId));

        ShuttleLocationResponse response = ShuttleLocationResponse.from(shuttle);

        // Try to cache for next time
        try {
            cacheLocation(response);
        } catch (Exception e) {
            log.warn("Failed to cache location: {}", e.getMessage());
        }

        return response;
    }

    /**
     * Get all shuttles (admin view)
     */
    public List<ShuttleResponse> getAllShuttles() {
        log.debug("Fetching all shuttles");
        return shuttleRepository.findAll().stream()
                .map(ShuttleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Register a new shuttle
     */
    @Transactional
    public ShuttleResponse registerShuttle(String vehicleNumber, String routeName, Integer capacity) {
        log.info("Registering new shuttle: {}", vehicleNumber);

        if (shuttleRepository.findByVehicleNumber(vehicleNumber).isPresent()) {
            throw new IllegalArgumentException("Shuttle already exists with vehicle number: " + vehicleNumber);
        }

        Shuttle shuttle = new Shuttle(vehicleNumber, routeName, capacity);
        shuttle = shuttleRepository.save(shuttle);

        log.info("Shuttle registered successfully with ID: {}", shuttle.getId());
        return ShuttleResponse.from(shuttle);
    }

    /**
     * Update shuttle status
     */
    @Transactional
    public void updateShuttleStatus(Long shuttleId, ShuttleStatus status) {
        log.info("Updating shuttle {} status to {}", shuttleId, status);

        Shuttle shuttle = shuttleRepository.findById(shuttleId)
                .orElseThrow(() -> new IllegalArgumentException("Shuttle not found: " + shuttleId));

        shuttle.setStatus(status);
        shuttleRepository.save(shuttle);

        // Broadcast status change
        try {
            broadcastService.broadcastShuttleStatusChange(shuttleId, status);
        } catch (Exception e) {
            log.warn("Failed to broadcast status change: {}", e.getMessage());
        }

        log.info("Shuttle status updated successfully");
    }

    /**
     * Cache location in Redis
     */
    private void cacheLocation(ShuttleLocationResponse response) {
        String cacheKey = LOCATION_CACHE_PREFIX + response.shuttleId();
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }
}