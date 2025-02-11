package com.secure.connect.secure_connect.user.repository;

import com.secure.connect.secure_connect.user.domain.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {
    Optional<VerificationToken> findByToken(String token);
}