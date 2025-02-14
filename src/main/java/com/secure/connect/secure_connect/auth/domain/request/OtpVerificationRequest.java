package com.secure.connect.secure_connect.auth.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OtpVerificationRequest {

    @NotNull
    @JsonProperty("code")
    private int otpCode;
}
