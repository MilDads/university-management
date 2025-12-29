package io.github.bardiakz.resource_service.repository;

import io.github.bardiakz.resource_service.model.Resource;
import io.github.bardiakz.resource_service.model.ResourceStatus;
import io.github.bardiakz.resource_service.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByType(ResourceType type);

    List<Resource> findByStatus(ResourceStatus status);

    List<Resource> findByTypeAndStatus(ResourceType type, ResourceStatus status);

    List<Resource> findByNameContainingIgnoreCase(String name);
}