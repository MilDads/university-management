package io.github.bardiakz.api_gateway.config;

import io.github.bardiakz.api_gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

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

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           JwtAuthenticationFilter jwtFilter) {
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

                // User Service - Protected
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("userServiceCircuitBreaker"))
                        )
                        .uri(userServiceUrl))

                // Resource Service - Protected
                .route("resource-service", r -> r
                        .path("/api/resources/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("resourceServiceCircuitBreaker"))
                        )
                        .uri(resourceServiceUrl))

                // Booking Service - Protected
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("bookingServiceCircuitBreaker"))
                        )
                        .uri(bookingServiceUrl))

                // Marketplace Service - Protected
                .route("marketplace-service", r -> r
                        .path("/api/marketplace/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("marketplaceServiceCircuitBreaker"))
                        )
                        .uri(marketplaceServiceUrl))

                // Payment Service - Protected
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("paymentServiceCircuitBreaker"))
                        )
                        .uri(paymentServiceUrl))

                // Exam Service - Protected
                .route("exam-service", r -> r
                        .path("/api/exams/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("examServiceCircuitBreaker"))
                        )
                        .uri(examServiceUrl))

                // Notification Service - Protected
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("notificationServiceCircuitBreaker"))
                        )
                        .uri(notificationServiceUrl))

                // IoT Service - Protected
                .route("iot-service", r -> r
                        .path("/api/iot/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("iotServiceCircuitBreaker"))
                        )
                        .uri(iotServiceUrl))

                // Tracking Service - Protected
                .route("tracking-service", r -> r
                        .path("/api/tracking/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(config -> config.setName("trackingServiceCircuitBreaker"))
                        )
                        .uri(trackingServiceUrl))

                .build();
    }
}