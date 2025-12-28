package io.github.bardiakz.user.dto;

import lombok.Data;

@Data
public class UserProfileCreateRequest {
    private String email;
    private String username;
    private String role;
    private String fullName;
    private String studentNumber;
    private String phoneNumber;
    private Long tenantId;
}