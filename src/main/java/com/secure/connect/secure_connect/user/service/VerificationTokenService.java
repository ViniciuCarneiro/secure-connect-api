package com.secure.connect.secure_connect.user.service;

import com.secure.connect.secure_connect.exception.TokenExpiredException;
import com.secure.connect.secure_connect.exception.TokenNotFoundException;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.domain.VerificationToken;
import com.secure.connect.secure_connect.user.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class VerificationTokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public String generateVerificationToken(User user) {

        if (user == null) {
            log.error("Tentativa de gerar token com usuário nulo.");
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }

        try {
            log.info("Gerando token de verificação para o usuário: {}", user.getEmail());
            String token = UUID.randomUUID().toString();

            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setUserId(user.getId());
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

            log.info("Token gerado com sucesso! Salvando no banco de dados...");
            tokenRepository.save(verificationToken);

            return token;
        } catch (Exception ex) {
            log.error("Erro ao gerar token de verificação para o usuário {}: {}", user.getEmail(), ex.getMessage(), ex);
            throw new RuntimeException("Erro ao gerar token de verificação.", ex);
        }
    }

    public String validateVerificationToken(String token) {

        try {
            log.info("Validando token de verificação informado: {}", token);

            Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);

            if (optionalToken.isPresent()) {
                VerificationToken verificationToken = optionalToken.get();
                log.info("Token encontrado. Validando data de expiração...");

                if (verificationToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                    log.info("Token válido. Extraindo ID do usuário.");
                    return verificationToken.getUserId();
                } else {
                    log.warn("Token expirado: {}", token);
                    throw new TokenExpiredException("Token expirado.");
                }
            } else {
                log.warn("Token não encontrado: {}", token);
                throw new TokenNotFoundException("Token não encontrado.");
            }
        } catch (Exception ex) {
            log.error("Erro ao validar token {}: {}", token, ex.getMessage(), ex);
            throw ex;
        }
    }

    public void deleteToken(String token) {

        try {
            log.info("Verificando existência do token antes de deletar: {}", token);

            Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);

            if (verificationToken.isPresent()) {
                tokenRepository.delete(verificationToken.get());
                log.info("Token deletado com sucesso: {}", token);
            } else {
                log.warn("Tentativa de deletar token inexistente: {}", token);
            }
        } catch (Exception ex) {
            log.error("Erro ao deletar o token {}: {}", token, ex.getMessage(), ex);
            throw new RuntimeException("Erro ao deletar o token.", ex);
        }
    }
}