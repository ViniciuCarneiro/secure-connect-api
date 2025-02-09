package com.secure.connect.secure_connect.user.domain;

import com.secure.connect.secure_connect.user.domain.enums.UserRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String name;
    private String password;
    private UserRole role;
}
