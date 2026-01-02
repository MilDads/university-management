package io.github.bardiakz.iot_service.repository;

import io.github.bardiakz.iot_service.entity.Sensor;
import io.github.bardiakz.iot_service.entity.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findBySensorId(String sensorId);
    List<Sensor> findByType(SensorType type);
    List<Sensor> findByLocation(String location);
    List<Sensor> findByActive(boolean active);
}
