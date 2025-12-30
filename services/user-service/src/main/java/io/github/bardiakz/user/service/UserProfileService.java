package io.github.bardiakz.user.service;

import io.github.bardiakz.user.dto.UserProfileCreateRequest;
import io.github.bardiakz.user.dto.UserProfileResponse;
import io.github.bardiakz.user.entity.Role;
import io.github.bardiakz.user.entity.UserProfile;
import io.github.bardiakz.user.event.EventPublisher;
import io.github.bardiakz.user.event.UserProfileCreatedEvent;
import io.github.bardiakz.user.event.UserRegisteredEvent;
import io.github.bardiakz.user.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository repository;
    private final EventPublisher eventPublisher;

    public UserProfileService(UserProfileRepository repository, EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void createProfileFromEvent(UserRegisteredEvent event) {
        if (repository.existsByEmail(event.getEmail())) {
            log.warn("Profile already exists for email: {}", event.getEmail());
            return;
        }

        UserProfile profile = new UserProfile();
        profile.setUsername(event.getUsername());
        profile.setEmail(event.getEmail());
        profile.setRole(parseRole(event.getDefaultRole()));
        profile.setFullName(event.getFullName());

        UserProfile saved = repository.save(profile);
        log.info("Profile created for userId: {}", saved.getId());

        // Publish confirmation event
        UserProfileCreatedEvent createdEvent = new UserProfileCreatedEvent(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole()
        );
        eventPublisher.publishProfileCreated(createdEvent);
    }

    @Transactional
    public UserProfileResponse createProfile(UserProfileCreateRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Profile already exists for this email");
        }

        UserProfile profile = new UserProfile();
        profile.setEmail(request.getEmail());
        profile.setUsername(request.getUsername());
        profile.setRole(request.getRole());
        profile.setFullName(request.getFullName());
        profile.setStudentNumber(request.getStudentNumber());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setTenantId(request.getTenantId() != null ? request.getTenantId() : 1L);

        UserProfile saved = repository.save(profile);
        return mapToResponse(saved);
    }

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role '{}', defaulting to STUDENT", roleStr);
            return Role.STUDENT;
        }
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setEmail(profile.getEmail());
        response.setUsername(profile.getUsername());
        response.setRole(profile.getRole());
        response.setFullName(profile.getFullName());
        response.setStudentNumber(profile.getStudentNumber());
        response.setPhoneNumber(profile.getPhoneNumber());
        return response;
    }
}
