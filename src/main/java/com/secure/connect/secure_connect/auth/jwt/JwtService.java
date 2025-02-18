package com.secure.connect.secure_connect.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.secure.connect.secure_connect.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class JwtService {
    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${spring.application.name}")
    private String appName;

    public String generateToken(User user, Collection<? extends GrantedAuthority> authorities, long expirationMinutes) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            return JWT.create()
                    .withIssuer(appName)
                    .withSubject(user.getEmail())
                    .withClaim("roles", roles)
                    .withExpiresAt(genExpirationDate(expirationMinutes))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token", exception);
        }
    }

    public boolean validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWT.require(algorithm)
                .withIssuer(appName)
                .build()
                .verify(token);
            return true;
        } catch (JWTVerificationException exception){
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(appName)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public List<String> getRolesFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String roles = JWT.require(algorithm)
                    .withIssuer(appName)
                    .build()
                    .verify(token)
                    .getClaim("roles")
                    .asString();
            return Arrays.asList(roles.split(","));
        } catch (JWTVerificationException exception) {
            return Collections.emptyList();
        }
    }

    private Instant genExpirationDate(long minutes){
        return LocalDateTime.now().plusMinutes(minutes).toInstant(ZoneOffset.of("-03:00"));
    }
}