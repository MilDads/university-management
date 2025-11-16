package io.github.bardiakz.authservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Constructor injection instead of field injection
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            // Reuse the authenticated user instead of loading again
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            log.info("User {} logged in successfully", request.username());
            return ResponseEntity.ok(new LoginResponse(jwt, request.username()));

        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for username: {}", request.username());
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {

        // Check if username already exists (efficient query)
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "Username is already taken"));
        }

        // Validate password strength
        if (!isPasswordStrong(request.password())) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "Password must be at least 8 characters with letters and numbers"));
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        // SECURITY FIX: Don't allow users to set their own role
        // Always default to USER role for registration
        user.setRole(Role.student);

        // If you want to allow role selection, it should be:
        // 1. In a separate admin endpoint
        // 2. Protected by admin authentication
        // 3. Audited/logged

        try {
            userRepository.save(user);
            log.info("New user registered: {}", user.getUsername());
            return ResponseEntity.status(201)
                    .body(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            log.error("Error registering user", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    private boolean isPasswordStrong(String password) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Za-z].*")
                && password.matches(".*\\d.*");
    }

    @PostMapping("/test")
    @Profile("dev")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("test", "test successful"));
    }
}