package io.github.bardiakz.user.controller;

import io.github.bardiakz.user.dto.UserProfileCreateRequest;
import io.github.bardiakz.user.dto.UserProfileResponse;
import io.github.bardiakz.user.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> createProfile(@RequestBody UserProfileCreateRequest request) {
        UserProfileResponse response = service.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyProfile(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok("Profile for: " + username);
    }
}