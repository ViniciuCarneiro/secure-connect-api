package com.secure.connect.secure_connect.auth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.secure.connect.secure_connect.exception.TotpGenerationException;
import com.secure.connect.secure_connect.exception.TotpVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
public class TotpService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int KEY_SIZE_BITS = 256;

    private final TimeBasedOneTimePasswordGenerator totpGenerator;
    private final Base32 base32 = new Base32();

    public TotpService() {
        totpGenerator = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30), 6, HMAC_ALGORITHM);
    }

    public String generateSecretKey() {

        try {
            log.info("Gerando secret key...");

            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMAC_ALGORITHM);
            keyGenerator.init(KEY_SIZE_BITS);
            SecretKey secretKey = keyGenerator.generateKey();

            log.info("Secret Key gerada com sucesso!");

            return base32.encodeAsString(secretKey.getEncoded()).replace("=", "");
        } catch (NoSuchAlgorithmException e) {
            log.error("Erro ao gerar a chave secreta TOTP", e);
            throw new TotpGenerationException("Erro ao gerar a chave secreta TOTP", e);
        }
    }

    public boolean verifyTotpCode(String base32Secret, int code) {

        log.info("Iniciando verificação do código TOTP...");

        if (base32Secret == null || base32Secret.isEmpty()) {
            log.error("Chave secreta não informada para verificação TOTP.");
            throw new TotpVerificationException("Chave secreta não informada para verificação TOTP.");
        }

        try {
            log.info("Decodificando chave secreta e validando código...");

            byte[] decodedKey = base32.decode(base32Secret);
            SecretKey secretKey = new SecretKeySpec(decodedKey, HMAC_ALGORITHM);

            Instant now = Instant.now();
            int currentCode = totpGenerator.generateOneTimePassword(secretKey, now);

            log.info("Código gerado: {}. Código informado: {}", currentCode, code);

            if (currentCode == code) {
                log.info("Código TOTP validado com sucesso.");
                return true;
            } else {
                log.info("Código TOTP inválido.");
                return false;
            }
        } catch (InvalidKeyException e) {
            log.error("Chave secreta inválida para verificação TOTP.", e);
            throw new TotpVerificationException("Chave secreta inválida para verificação TOTP.", e);
        }
    }

    public static String buildOtpAuthUri(String issuer, String account, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer, account, secret, issuer
        );
    }
}
