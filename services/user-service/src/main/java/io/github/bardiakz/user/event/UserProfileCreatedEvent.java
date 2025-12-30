package io.github.bardiakz.user.event;

import io.github.bardiakz.user.entity.Role;
import java.io.Serializable;
import java.time.Instant;

public class UserProfileCreatedEvent implements Serializable {
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private Instant timestamp;

    public UserProfileCreatedEvent() {
        this.timestamp = Instant.now();
    }

    public UserProfileCreatedEvent(Long userId, String username, String email, Role role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.timestamp = Instant.now();
    }

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}