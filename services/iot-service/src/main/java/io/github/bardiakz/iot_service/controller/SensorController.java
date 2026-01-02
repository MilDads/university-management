package io.github.bardiakz.iot_service.controller;

import io.github.bardiakz.iot_service.dto.SensorDTO;
import io.github.bardiakz.iot_service.dto.SensorReadingDTO;
import io.github.bardiakz.iot_service.dto.SensorRegisterRequest;
import io.github.bardiakz.iot_service.dto.SensorUpdateRequest;
import io.github.bardiakz.iot_service.service.SensorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iot/sensors")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<SensorDTO> registerSensor(@RequestBody SensorRegisterRequest request) {
        return ResponseEntity.ok(sensorService.registerSensor(request));
    }

    @PutMapping("/{sensorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
    public ResponseEntity<SensorDTO> updateSensor(
            @PathVariable String sensorId,
            @RequestBody SensorUpdateRequest request) {
        return ResponseEntity.ok(sensorService.updateSensor(sensorId, request));
    }

    @GetMapping
    public ResponseEntity<List<SensorDTO>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    @GetMapping("/{sensorId}")
    public ResponseEntity<SensorDTO> getSensor(@PathVariable String sensorId) {
        return ResponseEntity.ok(sensorService.getSensor(sensorId));
    }

    @GetMapping("/{sensorId}/readings")
    public ResponseEntity<List<SensorReadingDTO>> getReadings(
            @PathVariable String sensorId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(sensorService.getRecentReadings(sensorId, limit));
    }

    @PostMapping("/{sensorId}/reading")
    public ResponseEntity<Void> recordReading(
            @PathVariable String sensorId,
            @RequestBody Map<String, Double> payload) {
        sensorService.recordReading(sensorId, payload.get("value"));
        return ResponseEntity.ok().build();
    }
}
