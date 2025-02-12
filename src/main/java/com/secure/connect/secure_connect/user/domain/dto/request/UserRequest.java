package com.secure.connect.secure_connect.user.domain.dto.request;

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
    private String name;
    @NotNull
    private String username;
    @NotNull
    private String email;
    @NotNull
    private String password;
    @NotNull
    private UserRole role;
    @NotNull
    private boolean mfaEnabled;
}
