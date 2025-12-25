package io.github.bardiakz.tracking_service.repository;

import io.github.bardiakz.tracking_service.model.Shuttle;
import io.github.bardiakz.tracking_service.model.ShuttleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShuttleRepository extends JpaRepository<Shuttle, Long> {

    Optional<Shuttle> findByVehicleNumber(String vehicleNumber);

    List<Shuttle> findByStatus(ShuttleStatus status);

    List<Shuttle> findByRouteName(String routeName);

    // Find shuttles that have recently updated locations (active)
    List<Shuttle> findByStatusAndLastLocationUpdateIsNotNull(ShuttleStatus status);
}