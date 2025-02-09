package com.secure.connect.secure_connect.user.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum UserRole {

    ROLE_USER_ADMIN("USER_ADMIN"),
    ROLE_USER_STANDARD("USER_STANDARD");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    @JsonValue
    public String getRole() {
        return role;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        return Stream.of(UserRole.values())
                .filter(role -> role.role.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + value));
    }
}
