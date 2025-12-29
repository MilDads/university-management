package io.github.bardiakz.authservice;

public enum Role {
    ADMIN(0),
    STUDENT(1),
    INSTRUCTOR(2),
    FACULTY(3);

    private final int value;

    Role(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}