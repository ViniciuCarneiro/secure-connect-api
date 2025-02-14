package com.secure.connect.secure_connect.auth.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
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
            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMAC_ALGORITHM);
            keyGenerator.init(KEY_SIZE_BITS);
            SecretKey secretKey = keyGenerator.generateKey();
            return base32.encodeAsString(secretKey.getEncoded()).replace("=", "");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Erro ao gerar a chave secreta TOTP", e);
        }
    }

    public boolean verifyTotpCode(String base32Secret, int code) {
        if (base32Secret == null || base32Secret.isEmpty()) {
            return false;
        }

        try {
            byte[] decodedKey = base32.decode(base32Secret);
            SecretKey secretKey = new SecretKeySpec(decodedKey, HMAC_ALGORITHM);
            Instant now = Instant.now();

            int currentCode = totpGenerator.generateOneTimePassword(secretKey, now);

            if (currentCode == code) {
                return true;
            }

        } catch (InvalidKeyException e) {
            return false;
        }

        return false;
    }

    public static String buildOtpAuthUri(String issuer, String account, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer, account, secret, issuer
        );
    }
}
