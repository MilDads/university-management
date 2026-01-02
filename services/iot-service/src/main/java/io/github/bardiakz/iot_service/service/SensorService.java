package io.github.bardiakz.iot_service.service;

import io.github.bardiakz.iot_service.dto.SensorDTO;
import io.github.bardiakz.iot_service.dto.SensorReadingDTO;
import io.github.bardiakz.iot_service.dto.SensorRegisterRequest;
import io.github.bardiakz.iot_service.dto.SensorUpdateRequest;
import io.github.bardiakz.iot_service.entity.Sensor;
import io.github.bardiakz.iot_service.entity.SensorReading;
import io.github.bardiakz.iot_service.repository.SensorReadingRepository;
import io.github.bardiakz.iot_service.repository.SensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensorService {

    private static final Logger log = LoggerFactory.getLogger(SensorService.class);

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SensorService(SensorRepository sensorRepository,
                         SensorReadingRepository readingRepository,
                         SimpMessagingTemplate messagingTemplate) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @CacheEvict(value = "sensors", key = "#request.sensorId")
    public SensorDTO registerSensor(SensorRegisterRequest request) {
        if (sensorRepository.findBySensorId(request.getSensorId()).isPresent()) {
            throw new RuntimeException("Sensor already exists: " + request.getSensorId());
        }

        Sensor sensor = new Sensor();
        sensor.setSensorId(request.getSensorId());
        sensor.setName(request.getName());
        sensor.setType(request.getType());
        sensor.setLocation(request.getLocation());
        sensor.setUnit(request.getUnit());

        Sensor saved = sensorRepository.save(sensor);
        return mapToDTO(saved);
    }

    @Transactional
    @CacheEvict(value = "sensors", key = "#sensorId")
    public SensorDTO updateSensor(String sensorId, SensorUpdateRequest request) {
        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor not found: " + sensorId));

        if (request.getName() != null) sensor.setName(request.getName());
        if (request.getType() != null) sensor.setType(request.getType());
        if (request.getLocation() != null) sensor.setLocation(request.getLocation());
        if (request.getUnit() != null) sensor.setUnit(request.getUnit());
        if (request.getActive() != null) sensor.setActive(request.getActive());

        Sensor saved = sensorRepository.save(sensor);
        return mapToDTO(saved);
    }

    @Transactional
    public void recordReading(String sensorId, Double value) {
        Sensor sensor = findSensorCached(sensorId);

        SensorReading reading = new SensorReading();
        reading.setSensorId(sensorId);
        reading.setValue(value);
        reading.setTimestamp(Instant.now());

        readingRepository.save(reading);

        // Broadcast to WebSocket clients
        SensorReadingDTO dto = new SensorReadingDTO(
                sensorId, value, reading.getTimestamp(), sensor.getUnit()
        );
        messagingTemplate.convertAndSend("/topic/sensors/" + sensorId, dto);

        log.info("Recorded reading for sensor {}: {} {}", sensorId, value, sensor.getUnit());
    }

    @Cacheable(value = "sensors", key = "#sensorId")
    public Sensor findSensorCached(String sensorId) {
        return sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor not found: " + sensorId));
    }

    public List<SensorDTO> getAllSensors() {
        return sensorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public SensorDTO getSensor(String sensorId) {
        Sensor sensor = findSensorCached(sensorId);
        return mapToDTO(sensor);
    }

    public List<SensorReadingDTO> getRecentReadings(String sensorId, int limit) {
        Sensor sensor = findSensorCached(sensorId);

        return readingRepository.findRecentBySensorId(sensorId, limit).stream()
                .map(r -> new SensorReadingDTO(r.getSensorId(), r.getValue(), r.getTimestamp(), sensor.getUnit()))
                .collect(Collectors.toList());
    }

    private SensorDTO mapToDTO(Sensor sensor) {
        SensorDTO dto = new SensorDTO();
        dto.setId(sensor.getId());
        dto.setSensorId(sensor.getSensorId());
        dto.setName(sensor.getName());
        dto.setType(sensor.getType());
        dto.setLocation(sensor.getLocation());
        dto.setUnit(sensor.getUnit());
        dto.setActive(sensor.isActive());

        readingRepository.findLatestBySensorId(sensor.getSensorId())
                .ifPresent(reading -> {
                    dto.setLastValue(reading.getValue());
                    dto.setLastUpdate(reading.getTimestamp().toString());
                });

        return dto;
    }
}