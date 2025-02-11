package com.secure.connect.secure_connect.user.service;

import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.domain.VerificationToken;
import com.secure.connect.secure_connect.user.repository.UserRepository;
import com.secure.connect.secure_connect.user.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationTokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    public String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserId(user.getId());
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepository.save(verificationToken);
        return token;
    }

    public boolean validateVerificationToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isPresent()) {
            VerificationToken verificationToken = optionalToken.get();
            if (verificationToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                Optional<User> optionalUser = userRepository.findById(verificationToken.getUserId());
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    user.setEmailVerified(true);
                    userRepository.save(user);
                    tokenRepository.delete(verificationToken);
                    return true;
                }
            }
        }
        return false;
    }
}