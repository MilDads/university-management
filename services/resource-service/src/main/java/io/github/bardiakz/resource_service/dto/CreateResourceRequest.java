package io.github.bardiakz.resource_service.dto;

import io.github.bardiakz.resource_service.model.ResourceType;
import jakarta.validation.constraints.*;

public record CreateResourceRequest(
        @NotBlank(message = "Resource name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Resource type is required")
        ResourceType type,

        @NotBlank(message = "Location is required")
        @Size(max = 200, message = "Location cannot exceed 200 characters")
        String location,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 10000, message = "Capacity cannot exceed 10000")
        Integer capacity
) {}