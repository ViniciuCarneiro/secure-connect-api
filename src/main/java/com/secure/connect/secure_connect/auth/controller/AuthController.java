package com.secure.connect.secure_connect.auth.controller;

import com.secure.connect.secure_connect.auth.domain.enums.Authority;
import com.secure.connect.secure_connect.auth.domain.request.LoginRequest;
import com.secure.connect.secure_connect.auth.domain.request.OtpVerificationRequest;
import com.secure.connect.secure_connect.auth.domain.response.LoginResponse;
import com.secure.connect.secure_connect.auth.jwt.JwtService;
import com.secure.connect.secure_connect.auth.service.TotpService;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TotpService totpService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String token = null;

        if (!user.isMfaEnabled()) {
            token = jwtService.generateToken(user, user.getAuthorities(), 60);
        } else {
            var preAuthAuthority = new SimpleGrantedAuthority(Authority.PRE_AUTH_MFA.name());
            token = jwtService.generateToken(user, java.util.List.of(preAuthAuthority), 5); // 5 minutos
        }

        return new ResponseEntity<>(new LoginResponse(token), HttpStatus.OK);
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody @Valid OtpVerificationRequest otpRequest) throws NoSuchAlgorithmException {

        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication();

        User user = (User) userRepository.findByEmail(userAuthenticated.getPrincipal().toString());

        boolean validOtp = totpService.verifyTotpCode(user.getTotpSecret(), otpRequest.getOtpCode());
        if (!validOtp) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código OTP inválido");
        }

        String token = jwtService.generateToken(user, user.getAuthorities(), 60);
        return new ResponseEntity<>(new LoginResponse(token), HttpStatus.OK);
    }
}