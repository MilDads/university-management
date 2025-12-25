package io.github.bardiakz.tracking_service.dto;

import io.github.bardiakz.tracking_service.model.Shuttle;
import io.github.bardiakz.tracking_service.model.ShuttleStatus;

import java.time.LocalDateTime;

public record ShuttleResponse(
        Long id,
        String vehicleNumber,
        String routeName,
        String driver,
        Integer capacity,
        ShuttleStatus status,
        Double currentLatitude,
        Double currentLongitude,
        LocalDateTime lastLocationUpdate
) {
    public static ShuttleResponse from(Shuttle shuttle) {
        return new ShuttleResponse(
                shuttle.getId(),
                shuttle.getVehicleNumber(),
                shuttle.getRouteName(),
                shuttle.getDriver(),
                shuttle.getCapacity(),
                shuttle.getStatus(),
                shuttle.getCurrentLatitude(),
                shuttle.getCurrentLongitude(),
                shuttle.getLastLocationUpdate()
        );
    }
}