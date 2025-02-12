package com.secure.connect.secure_connect.auth.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OtpVerificationRequest {

    private int otpCode;
}
