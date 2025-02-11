package com.secure.connect.secure_connect.user.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "verificationTokens")
public class VerificationToken {

    @Id
    private String id;

    private String token;

    private String userId;

    private LocalDateTime expiryDate;
}