package com.secure.connect.secure_connect.user.service;

import com.secure.connect.secure_connect.exception.EmailNotFoundException;
import com.secure.connect.secure_connect.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        try {
            UserDetails userDetails = userRepository.findByEmail(email);
            if (userDetails == null) {
                log.warn("Usuário não encontrado para o email: {}", email);
                throw new EmailNotFoundException("Usuário não encontrado com o email: " + email);
            }
            return userDetails;
        } catch (Exception e) {
            log.error("Erro ao carregar o usuário com o email {}: {}", email, e.getMessage(), e);
            throw new EmailNotFoundException("Erro ao carregar o usuário com o email: " + email, e);
        }
    }
}