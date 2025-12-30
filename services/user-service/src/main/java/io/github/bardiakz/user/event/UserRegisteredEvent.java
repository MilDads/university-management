package io.github.bardiakz.user.event;

import java.io.Serializable;
import java.time.Instant;

public class UserRegisteredEvent implements Serializable {
    private String username;
    private String email;
    private String defaultRole;
    private String fullName;
    private Instant timestamp;

    public UserRegisteredEvent() {}

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDefaultRole() { return defaultRole; }
    public void setDefaultRole(String defaultRole) { this.defaultRole = defaultRole; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
