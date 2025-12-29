package io.github.bardiakz.tracking_service.dto;

import jakarta.validation.constraints.NotNull;

public record LocationUpdateRequest(
        @NotNull Long shuttleId,
        @NotNull Double latitude,
        @NotNull Double longitude,
        Double speed,
        Double heading,
        Double accuracy
) {}