package com.university.marketplace.security;

import org.springframework.stereotype.Component;

@Component
public class RequestAuth {

    public String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("Missing X-User-Id header");
        }
        return userId;
    }

    public void requireRole(String role, String... allowed) {
        if (role == null || role.isBlank()) {
            throw new RuntimeException("Missing X-User-Role header");
        }
        for (String a : allowed) {
            if (a.equalsIgnoreCase(role)) return;
        }
        throw new RuntimeException("Forbidden for role: " + role);
    }
}
