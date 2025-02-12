package com.secure.connect.secure_connect.auth.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Getter
public class LoginRequest {
    @Email
    @NotNull
    private String email;
    @NotNull
    private String password;
}