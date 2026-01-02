package io.github.bardiakz.iot_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Random;

/**
 * Simulates virtual sensor data for demonstration
 */
@Service
@EnableScheduling
public class VirtualSensorSimulator {

    private static final Logger log = LoggerFactory.getLogger(VirtualSensorSimulator.class);
    private final SensorService sensorService;
    private final Random random = new Random();

    public VirtualSensorSimulator(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void simulateTemperature() {
        try {
            // Simulate classroom temperature: 18-26°C
            double temperature = 20 + (random.nextDouble() * 6);
            sensorService.recordReading("TEMP-CLASS-101", temperature);
        } catch (Exception e) {
            log.debug("Sensor not registered yet: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 5000)
    public void simulateHumidity() {
        try {
            // Simulate humidity: 40-70%
            double humidity = 40 + (random.nextDouble() * 30);
            sensorService.recordReading("HUM-CLASS-101", humidity);
        } catch (Exception e) {
            log.debug("Sensor not registered yet: {}", e.getMessage());
        }
    }
}
