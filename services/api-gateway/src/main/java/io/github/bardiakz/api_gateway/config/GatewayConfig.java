package io.github.bardiakz.api_gateway.config;

import io.github.bardiakz.api_gateway.filter.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);

    @Value("${AUTH_SERVICE_URL:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${USER_SERVICE_URL:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${RESOURCE_SERVICE_URL:http://localhost:8083}")
    private String resourceServiceUrl;

    @Value("${BOOKING_SERVICE_URL:http://localhost:8084}")
    private String bookingServiceUrl;

    @Value("${MARKETPLACE_SERVICE_URL:http://localhost:8085}")
    private String marketplaceServiceUrl;

    @Value("${PAYMENT_SERVICE_URL:http://localhost:8086}")
    private String paymentServiceUrl;

    @Value("${EXAM_SERVICE_URL:http://localhost:8087}")
    private String examServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8088}")
    private String notificationServiceUrl;

    @Value("${IOT_SERVICE_URL:http://localhost:8089}")
    private String iotServiceUrl;

    @Value("${TRACKING_SERVICE_URL:http://localhost:8090}")
    private String trackingServiceUrl;

    // IMPORTANT: Internal API Secret for secure service-to-service communication
    @Value("${INTERNAL_API_SECRET}")
    private String internalApiSecret;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           JwtAuthenticationFilter jwtFilter) {

        // Log the internal secret on startup (first 10 chars only for security)
        logger.info("Internal API Secret configured: {}...",
                internalApiSecret != null && internalApiSecret.length() > 10
                        ? internalApiSecret.substring(0, 10)
                        : "NOT SET");

        return builder.routes()
                // Auth Service - Public (no JWT filter)
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri(authServiceUrl))

                // User Profile Service - Protected
                .route("user-profiles", r -> r
                        .path("/api/profiles/**")
                        .filters(f -> f
                                // Add internal secret FIRST, before JWT filter
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("userServiceCircuitBreaker"))
                        )
                        .uri(userServiceUrl))

                // User Service - Protected
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("userServiceCircuitBreaker"))
                        )
                        .uri(userServiceUrl))

                // Resource Service - Protected
                .route("resource-service", r -> r
                        .path("/api/resources/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("resourceServiceCircuitBreaker"))
                        )
                        .uri(resourceServiceUrl))

                // Booking Service - Protected
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("bookingServiceCircuitBreaker"))
                        )
                        .uri(bookingServiceUrl))

                // Marketplace Service - Protected
                .route("marketplace-service", r -> r
                        .path("/api/marketplace/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("marketplaceServiceCircuitBreaker"))
                        )
                        .uri(marketplaceServiceUrl))

                // Payment Service - Protected
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("paymentServiceCircuitBreaker"))
                        )
                        .uri(paymentServiceUrl))

                // Exam Service - Protected
                .route("exam-service", r -> r
                        .path("/api/exams/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("examServiceCircuitBreaker"))
                        )
                        .uri(examServiceUrl))

                // Notification Service - Protected with Internal API Secret
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                // CRITICAL: Add Internal API Secret BEFORE JWT filter
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/notification"))
                        )
                        .uri(notificationServiceUrl))

                // IoT Service - Protected
                .route("iot-service", r -> r
                        .path("/api/iot/**", "/ws/iot/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("iotServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/iot"))
                        )
                        .uri(iotServiceUrl))

                // Tracking Service - Protected
                .route("tracking-service", r -> r
                        .path("/api/tracking/**")
                        .filters(f -> f
                                .addRequestHeader("X-Internal-Secret", internalApiSecret)
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("trackingServiceCircuitBreaker"))
                        )
                        .uri(trackingServiceUrl))

                .build();
    }
}