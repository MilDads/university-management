package io.github.bardiakz.user.service;

import io.github.bardiakz.user.dto.UserProfileCreateRequest;
import io.github.bardiakz.user.dto.UserProfileResponse;
import io.github.bardiakz.user.entity.UserProfile;
import io.github.bardiakz.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    @Transactional
    public UserProfileResponse createProfile(UserProfileCreateRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Profile already exists for this email");
        }

        UserProfile profile = new UserProfile();
        profile.setId(request.getId());
        profile.setEmail(request.getEmail());
        profile.setRole(request.getRole());
        profile.setFullName(request.getFullName());
        profile.setStudentNumber(request.getStudentNumber());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setTenantId(request.getTenantId());

        UserProfile saved = repository.save(profile);

        return mapToResponse(saved);
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setEmail(profile.getEmail());
        response.setRole(profile.getRole());
        response.setFullName(profile.getFullName());
        response.setStudentNumber(profile.getStudentNumber());
        response.setPhoneNumber(profile.getPhoneNumber());
        return response;
    }
}