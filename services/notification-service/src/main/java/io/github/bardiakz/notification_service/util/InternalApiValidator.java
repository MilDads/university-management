package io.github.bardiakz.notification_service.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates internal API requests using a shared secret
 */
@Component
public class InternalApiValidator extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalApiValidator.class);
    private static final String INTERNAL_API_HEADER = "X-Internal-Secret";

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Allow actuator endpoints without authentication
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedSecret = request.getHeader(INTERNAL_API_HEADER);

        if (providedSecret == null || !providedSecret.equals(internalApiSecret)) {
            logger.warn("Unauthorized internal API access attempt from {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized - Invalid internal API secret");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
