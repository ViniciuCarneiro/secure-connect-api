package com.secure.connect.secure_connect.auth.domain.request;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String password;
    private String confirmPassword;
}
