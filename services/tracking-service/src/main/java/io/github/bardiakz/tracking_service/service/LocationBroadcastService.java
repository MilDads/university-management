package io.github.bardiakz.tracking_service.service;

import io.github.bardiakz.tracking_service.dto.ShuttleLocationResponse;
import io.github.bardiakz.tracking_service.model.ShuttleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcasts location updates to WebSocket clients
 */
@Service
public class LocationBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(LocationBroadcastService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public LocationBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast location update to all subscribed clients
     */
    public void broadcastLocationUpdate(ShuttleLocationResponse location) {
        log.debug("Broadcasting location update for shuttle {}", location.shuttleId());

        // Send to topic /topic/locations - all clients receive all updates
        messagingTemplate.convertAndSend("/topic/locations", location);

        // Also send to specific shuttle topic /topic/shuttle/{id}
        messagingTemplate.convertAndSend("/topic/shuttle/" + location.shuttleId(), location);
    }

    /**
     * Broadcast shuttle status change
     */
    public void broadcastShuttleStatusChange(Long shuttleId, ShuttleStatus status) {
        log.debug("Broadcasting status change for shuttle {}: {}", shuttleId, status);

        var statusUpdate = new StatusChangeMessage(shuttleId, status);
        messagingTemplate.convertAndSend("/topic/shuttle/" + shuttleId + "/status", statusUpdate);
    }

    private record StatusChangeMessage(Long shuttleId, ShuttleStatus newStatus) {}
}