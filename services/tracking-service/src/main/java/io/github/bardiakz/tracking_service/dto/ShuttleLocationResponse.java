package io.github.bardiakz.tracking_service.dto;

import io.github.bardiakz.tracking_service.model.Shuttle;
import io.github.bardiakz.tracking_service.model.ShuttleStatus;

import java.time.LocalDateTime;

public record ShuttleLocationResponse(
        Long shuttleId,
        String vehicleNumber,
        String routeName,
        ShuttleStatus status,
        Double latitude,
        Double longitude,
        LocalDateTime lastUpdate
) {
    public static ShuttleLocationResponse from(Shuttle shuttle) {
        return new ShuttleLocationResponse(
                shuttle.getId(),
                shuttle.getVehicleNumber(),
                shuttle.getRouteName(),
                shuttle.getStatus(),
                shuttle.getCurrentLatitude(),
                shuttle.getCurrentLongitude(),
                shuttle.getLastLocationUpdate()
        );
    }
}