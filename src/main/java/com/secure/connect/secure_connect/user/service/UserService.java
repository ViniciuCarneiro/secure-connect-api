package com.secure.connect.secure_connect.user.service;

import com.secure.connect.secure_connect.auth.service.TotpService;
import com.secure.connect.secure_connect.exception.InvalidDataException;
import com.secure.connect.secure_connect.exception.UserNotFoundException;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    public List<User> findAll() {

        try {
            log.info("Consultando usuários no banco de dados...");
            return userRepository.findAll();
        } catch (Exception ex) {
            log.error("Erro ao consultar usuários: {}", ex.getMessage(), ex);
            throw new UserNotFoundException("Erro ao consultar usuários", ex);
        }
    }

    public UserDetails findByEmail(String email) {

        try {
            log.info("Consultando usuário por email: {}", email);
            return userRepository.findByEmail(email);
        } catch (Exception ex) {
            log.error("Erro ao consultar usuário por email {}: {}", email, ex.getMessage(), ex);
            throw new UserNotFoundException("Erro ao consultar usuário por email", ex);
        }
    }

    public User findById(String userId) {

        try {
            log.info("Consultando usuário pelo ID: {}", userId);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o ID: " + userId));
        } catch (Exception ex) {
            log.error("Erro ao consultar usuário com ID {}: {}", userId, ex.getMessage(), ex);
            throw new UserNotFoundException("Erro ao consultar usuário com ID: " + userId, ex);
        }
    }

    public User registerUser(User user) {

        try {
            log.info("Registrando usuário com email: {}", user.getEmail());

            User userEncrypted = encryptData(user);
            User savedUser = userRepository.save(userEncrypted);

            log.info("Usuário registrado com sucesso: {}", user.getEmail());
            return savedUser;
        } catch (Exception ex) {
            log.error("Erro ao registrar usuário {}: {}", user.getEmail(), ex.getMessage(), ex);
            throw new InvalidDataException("Erro ao registrar usuário", ex);
        }
    }

    public void updateUser(User user) {

        try {
            log.info("Atualizando dados do usuário no banco de dados: {}", user);

            userRepository.save(user);

            log.info("Usuário atualizado com sucesso: {}", user.getEmail());
        } catch (Exception ex) {
            log.error("Erro ao atualizar usuário {}: {}", user.getEmail(), ex.getMessage(), ex);
            throw new InvalidDataException("Erro ao atualizar usuário", ex);
        }
    }

    protected User encryptData(User user) {

        try {
            log.info("Criptografando dados sensíveis do usuário: {}", user.getEmail());
            user.setPassword(encryptPassword(user.getPassword()));

            if (user.isMfaEnabled()) {
                String secretKey = totpService.generateSecretKey();
                if (secretKey != null) {
                    user.setTotpSecret(secretKey);
                    log.info("Chave TOTP gerada para o usuário: {}", user.getEmail());
                } else {
                    log.warn("Falha ao gerar chave TOTP para o usuário: {}. Desabilitando MFA.", user.getEmail());
                    user.setMfaEnabled(false);
                }
            }
            return user;
        } catch (Exception ex) {
            log.error("Erro ao criptografar dados do usuário {}: {}", user.getEmail(), ex.getMessage(), ex);
            throw new RuntimeException("Erro ao criptografar dados do usuário", ex);
        }
    }

    public boolean verifiedEmail(String userId) {

        try {
            log.info("Verificando usuário pelo ID: {}", userId);

            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setEmailVerified(true);

                log.info("Atualizando emailVerified para true no usuário: {}", user.getEmail());
                userRepository.save(user);
                return true;
            } else {
                log.warn("Usuário não encontrado para o ID: {}", userId);
                return false;
            }
        } catch (Exception ex) {
            log.error("Erro ao verificar email para o usuário com ID {}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Erro ao verificar email", ex);
        }
    }

    protected String encryptPassword(String password) {

        try {
            return passwordEncoder.encode(password);
        } catch (Exception ex) {
            log.error("Erro ao criptografar a senha: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao criptografar a senha", ex);
        }
    }
}