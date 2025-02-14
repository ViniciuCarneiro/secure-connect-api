package com.secure.connect.secure_connect.auth.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    @NotNull
    @JsonProperty("password")
    private String password;

    @NotNull
    @JsonProperty("confirm_password")
    private String confirmPassword;
}
