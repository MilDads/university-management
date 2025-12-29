package io.github.bardiakz.resource_service.service;

import io.github.bardiakz.resource_service.dto.CreateResourceRequest;
import io.github.bardiakz.resource_service.dto.ResourceResponse;
import io.github.bardiakz.resource_service.event.ResourceEventPublisher;
import io.github.bardiakz.resource_service.model.Resource;
import io.github.bardiakz.resource_service.model.ResourceStatus;
import io.github.bardiakz.resource_service.model.ResourceType;
import io.github.bardiakz.resource_service.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final ResourceEventPublisher eventPublisher;

    public ResourceService(ResourceRepository resourceRepository, ResourceEventPublisher eventPublisher) {
        this.resourceRepository = resourceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ResourceResponse createResource(CreateResourceRequest request, String createdBy) {
        log.info("Creating new resource: {} by user: {}", request.name(), createdBy);

        Resource resource = new Resource(
                request.name(),
                request.description(),
                request.type(),
                request.location(),
                request.capacity()
        );
        resource.setCreatedBy(createdBy);
        resource.setStatus(ResourceStatus.AVAILABLE);

        Resource savedResource = resourceRepository.save(resource);

        // Publish ResourceAdded event
        eventPublisher.publishResourceAdded(savedResource);

        log.info("Resource created successfully with ID: {}", savedResource.getId());
        return ResourceResponse.from(savedResource);
    }

    public List<ResourceResponse> getAllResources() {
        log.debug("Fetching all resources");
        return resourceRepository.findAll().stream()
                .map(ResourceResponse::from)
                .collect(Collectors.toList());
    }

    public ResourceResponse getResourceById(Long id) {
        log.debug("Fetching resource with ID: {}", id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
        return ResourceResponse.from(resource);
    }

    public List<ResourceResponse> getResourcesByType(ResourceType type) {
        log.debug("Fetching resources of type: {}", type);
        return resourceRepository.findByType(type).stream()
                .map(ResourceResponse::from)
                .collect(Collectors.toList());
    }

    public List<ResourceResponse> getResourcesByStatus(ResourceStatus status) {
        log.debug("Fetching resources with status: {}", status);
        return resourceRepository.findByStatus(status).stream()
                .map(ResourceResponse::from)
                .collect(Collectors.toList());
    }

    public List<ResourceResponse> getAvailableResourcesByType(ResourceType type) {
        log.debug("Fetching available resources of type: {}", type);
        return resourceRepository.findByTypeAndStatus(type, ResourceStatus.AVAILABLE).stream()
                .map(ResourceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResourceResponse updateResourceStatus(Long id, ResourceStatus newStatus) {
        log.info("Updating status of resource {} to {}", id, newStatus);

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));

        ResourceStatus oldStatus = resource.getStatus();
        resource.setStatus(newStatus);
        Resource updatedResource = resourceRepository.save(resource);

        // Publish ResourceStatusChanged event
        eventPublisher.publishResourceStatusChanged(updatedResource, oldStatus, newStatus);

        log.info("Resource status updated successfully");
        return ResourceResponse.from(updatedResource);
    }

    @Transactional
    public void deleteResource(Long id) {
        log.info("Deleting resource with ID: {}", id);

        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with ID: " + id);
        }

        resourceRepository.deleteById(id);
        log.info("Resource deleted successfully");
    }
}