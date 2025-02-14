package com.secure.connect.secure_connect.user.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.secure.connect.secure_connect.user.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("username")
    private String username;

    @NotNull
    @JsonProperty("email")
    private String email;

    @NotNull
    @JsonProperty("password")
    private String password;

    @NotNull
    @JsonProperty("role")
    private UserRole role;

    @NotNull
    @JsonProperty("mfa_enabled")
    private boolean mfaEnabled;
}
