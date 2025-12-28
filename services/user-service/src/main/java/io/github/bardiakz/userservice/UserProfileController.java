package io.github.bardiakz.user.controller;

import io.github.bardiakz.user.dto.UserProfileCreateRequest;
import io.github.bardiakz.user.dto.UserProfileResponse;
import io.github.bardiakz.user.entity.UserProfile;
import io.github.bardiakz.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles")    
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileRepository repository;

    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody UserProfileCreateRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Profile already exists for this email"));
        }

        UserProfile profile = new UserProfile();
        profile.setEmail(request.getEmail());
        profile.setUsername(request.getUsername());
        profile.setRole(request.getRole());
        profile.setFullName(request.getFullName());
        profile.setStudentNumber(request.getStudentNumber());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setTenantId(request.getTenantId() != null ? request.getTenantId() : 1L);

        profile = repository.save(profile);

        UserProfileResponse response = mapToResponse(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        UserProfileResponse r = new UserProfileResponse();
        r.setId(profile.getId());
        r.setEmail(profile.getEmail());
        r.setUsername(profile.getUsername());
        r.setRole(profile.getRole());
        r.setFullName(profile.getFullName());
        r.setStudentNumber(profile.getStudentNumber());
        r.setPhoneNumber(profile.getPhoneNumber());
        return r;
    }
}