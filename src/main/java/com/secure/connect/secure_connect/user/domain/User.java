package com.secure.connect.secure_connect.user.domain;

import com.secure.connect.secure_connect.user.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;
    private String name;
    private String username;
    @Email
    private String email;
    private String password;
    private boolean emailVerified = false;
    private boolean mfaEnabled = false;
    private String totpSecret;
    private UserRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getRole()));
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}
