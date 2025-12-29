package io.github.bardiakz.tracking_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time shuttle tracking
 * Uses STOMP protocol over WebSocket
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker
        // Clients subscribe to /topic/{destination}
        config.enableSimpleBroker("/topic");

        // Prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint: ws://localhost:8090/ws/tracking
        registry.addEndpoint("/ws/tracking")
                .setAllowedOriginPatterns("*") // Allow all origins (configure properly in production)
                .withSockJS(); // SockJS fallback for browsers without WebSocket support
    }
}