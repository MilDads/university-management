package io.github.bardiakz.resource_service.dto;

import io.github.bardiakz.resource_service.model.Resource;
import io.github.bardiakz.resource_service.model.ResourceStatus;
import io.github.bardiakz.resource_service.model.ResourceType;

import java.time.LocalDateTime;

public record ResourceResponse(
        Long id,
        String name,
        String description,
        ResourceType type,
        String location,
        Integer capacity,
        ResourceStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy
) {
    public static ResourceResponse from(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.getLocation(),
                resource.getCapacity(),
                resource.getStatus(),
                resource.getCreatedAt(),
                resource.getUpdatedAt(),
                resource.getCreatedBy()
        );
    }
}