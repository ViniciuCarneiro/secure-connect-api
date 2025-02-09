package com.secure.connect.secure_connect.user.domain.dto.request;

import com.secure.connect.secure_connect.user.domain.enums.UserRole;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String name;
    private String password;
    private UserRole role;
}
