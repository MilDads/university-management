package io.github.bardiakz.iot_service.repository;

import io.github.bardiakz.iot_service.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    
    List<SensorReading> findBySensorIdOrderByTimestampDesc(String sensorId);
    
    List<SensorReading> findBySensorIdAndTimestampBetween(
        String sensorId, Instant start, Instant end
    );
    
    @Query("SELECT sr FROM SensorReading sr WHERE sr.sensorId = :sensorId ORDER BY sr.timestamp DESC LIMIT 1")
    Optional<SensorReading> findLatestBySensorId(String sensorId);
    
    @Query("SELECT sr FROM SensorReading sr WHERE sr.sensorId = :sensorId ORDER BY sr.timestamp DESC LIMIT :limit")
    List<SensorReading> findRecentBySensorId(String sensorId, int limit);
}
