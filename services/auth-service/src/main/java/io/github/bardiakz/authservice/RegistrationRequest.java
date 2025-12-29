package io.github.bardiakz.authservice;

public record RegistrationRequest(String username, String password, Role role) {
}
