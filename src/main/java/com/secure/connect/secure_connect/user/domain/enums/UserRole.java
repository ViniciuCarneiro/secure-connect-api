package com.secure.connect.secure_connect.user.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    USER_ADMIN("USER_ADMIN"),
    USER_STANDARD("USER_STANDARD");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }
}