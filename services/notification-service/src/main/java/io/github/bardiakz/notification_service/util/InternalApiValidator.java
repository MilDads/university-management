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
import java.util.Enumeration;

/**
 * Validates internal API calls using a shared secret
 */
@Component
public class InternalApiValidator extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalApiValidator.class);
    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // DEBUG: Log all headers
        logger.debug("=== Request to {} from {} ===", requestPath, request.getRemoteAddr());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.debug("Header: {} = {}", headerName, request.getHeader(headerName));
        }

        // Allow actuator/health endpoints without authentication
        if (requestPath.startsWith("/actuator")) {
            logger.debug("Allowing actuator endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Validate internal secret for all other requests
        String providedSecret = request.getHeader(INTERNAL_SECRET_HEADER);

        logger.debug("Expected secret starts with: {}...",
                internalApiSecret != null && internalApiSecret.length() > 10
                        ? internalApiSecret.substring(0, 10)
                        : "NULL");
        logger.debug("Provided secret starts with: {}",
                providedSecret != null && providedSecret.length() > 10
                        ? providedSecret.substring(0, 10)
                        : "NULL");

        if (providedSecret == null || providedSecret.isEmpty()) {
            logger.warn("Missing internal API secret from {} for path {}",
                    request.getRemoteAddr(), requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing internal API secret\"}");
            return;
        }

        if (!providedSecret.equals(internalApiSecret)) {
            logger.warn("Invalid internal API secret from {} for path {}. Expected length: {}, Got length: {}",
                    request.getRemoteAddr(), requestPath,
                    internalApiSecret != null ? internalApiSecret.length() : 0,
                    providedSecret.length());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid internal API secret\"}");
            return;
        }

        logger.debug("Internal API secret validated successfully for {}", requestPath);
        // Secret is valid, continue with the request
        filterChain.doFilter(request, response);
    }
}