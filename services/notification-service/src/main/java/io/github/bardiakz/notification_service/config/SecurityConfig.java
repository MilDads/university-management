package io.github.bardiakz.notification_service.config;

import io.github.bardiakz.notification_service.util.InternalApiValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for internal API access only
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final InternalApiValidator internalApiValidator;

    public SecurityConfig(InternalApiValidator internalApiValidator) {
        this.internalApiValidator = internalApiValidator;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/notifications/**").permitAll()  // ADD THIS LINE
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalApiValidator, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}