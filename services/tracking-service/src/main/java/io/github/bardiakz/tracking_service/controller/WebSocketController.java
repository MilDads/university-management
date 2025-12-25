package io.github.bardiakz.tracking_service.controller;

import io.github.bardiakz.tracking_service.dto.ShuttleLocationResponse;
import io.github.bardiakz.tracking_service.service.TrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * WebSocket message handling controller
 * Handles incoming WebSocket messages from clients
 */
@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    private final TrackingService trackingService;

    public WebSocketController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Client requests all active shuttle locations
     * Client sends to: /app/shuttles/active
     * Response sent to: /topic/shuttles/active
     */
    @MessageMapping("/shuttles/active")
    @SendTo("/topic/shuttles/active")
    public List<ShuttleLocationResponse> requestActiveShuttles() {
        log.debug("WebSocket client requested active shuttles");
        return trackingService.getActiveShuttleLocations();
    }

    /**
     * Client requests specific shuttle location
     * Client sends to: /app/shuttle/{id}
     * Response sent to: /topic/shuttle/{id}
     */
    @MessageMapping("/shuttle/{id}")
    @SendTo("/topic/shuttle/{id}")
    public ShuttleLocationResponse requestShuttleLocation(@DestinationVariable Long id) {
        log.debug("WebSocket client requested shuttle location: {}", id);
        return trackingService.getShuttleLocation(id);
    }
}