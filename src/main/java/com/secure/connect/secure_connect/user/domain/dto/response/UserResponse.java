package com.secure.connect.secure_connect.user.domain.dto.response;

import com.secure.connect.secure_connect.user.domain.enums.UserRole;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String name;
    private UserRole role;
}
