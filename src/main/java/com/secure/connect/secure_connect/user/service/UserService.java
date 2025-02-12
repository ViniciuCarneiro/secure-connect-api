package com.secure.connect.secure_connect.user.service;

import com.secure.connect.secure_connect.auth.service.TotpService;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public UserDetails findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(String userId) {
        return userRepository.findById(userId).get();
    }

    public User registerUser(User user) {

        return userRepository.save(encryptData(user));
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    protected User encryptData(User user) {
        user.setPassword(encryptPassword(user.getPassword()));

        if (user.isMfaEnabled()) {
            user.setTotpSecret(totpService.generateSecretKey());
        }

        return user;
    }

    public boolean verifiedEmail(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmailVerified(true);
            userRepository.save(user);

            return true;
        }

        return false;
    }

    protected String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
