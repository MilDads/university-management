package io.github.bardiakz.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecretKey signingKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String jwtSecret) {
        super(Config.class);

        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits");
        }

        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        logger.info("JwtAuthenticationFilter initialized with secret (first 10 chars): {}...",
                jwtSecret.substring(0, Math.min(10, jwtSecret.length())));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            logger.debug("Processing request to: {}", path);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                String token = authHeader.substring(7);

                // Parse JWT using JJWT 0.13.0 API
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)  // Use verifyWith() in 0.13.0
                        .build()
                        .parseSignedClaims(token)  // Use parseSignedClaims() in 0.13.0
                        .getPayload();  // Use getPayload() instead of getBody()

                // Extract user information from JWT
                String username = claims.getSubject();
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);

                logger.info("JWT validated for user: {} ({})", username, email);
                logger.debug("Adding headers - X-User-Id: {}, X-User-Email: {}, X-User-Role: {}",
                        username, email, role);

                // Build modified request with user headers
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", username != null ? username : "")
                        .header("X-User-Email", email != null ? email : "")
                        .header("X-User-Role", role != null ? role : "")
                        .build();

                // Create modified exchange
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                // Log all headers being forwarded (for debugging)
                if (logger.isDebugEnabled()) {
                    modifiedRequest.getHeaders().forEach((key, value) ->
                            logger.debug("Forwarding header: {} = {}", key,
                                    key.equalsIgnoreCase("Authorization") ? "[REDACTED]" : value)
                    );
                }

                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                logger.error("JWT validation failed for path {}: {}", path, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}