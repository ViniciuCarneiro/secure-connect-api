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

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    public User registerUser(User user) {

        return userRepository.save(encryptData(user));
    }

    public List<User> find() {
        return userRepository.findAll();
    }

    protected User encryptData(User user) {
        user.setPassword(encryptPassword(user.getPassword()));

        if (user.isMfaEnabled()) {
            user.setTotpSecret(totpService.generateSecretKey());
        }

        return user;
    }

    protected String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
