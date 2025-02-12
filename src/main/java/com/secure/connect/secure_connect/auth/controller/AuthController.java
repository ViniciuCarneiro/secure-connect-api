package com.secure.connect.secure_connect.auth.controller;

import com.secure.connect.secure_connect.auth.domain.enums.Authority;
import com.secure.connect.secure_connect.auth.domain.request.ForgotPasswordRequest;
import com.secure.connect.secure_connect.auth.domain.request.LoginRequest;
import com.secure.connect.secure_connect.auth.domain.request.OtpVerificationRequest;
import com.secure.connect.secure_connect.auth.domain.request.ResetPasswordRequest;
import com.secure.connect.secure_connect.auth.domain.response.LoginResponse;
import com.secure.connect.secure_connect.auth.jwt.JwtService;
import com.secure.connect.secure_connect.auth.service.TotpService;
import com.secure.connect.secure_connect.email.service.EmailService;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.repository.UserRepository;
import com.secure.connect.secure_connect.user.service.UserService;
import com.secure.connect.secure_connect.user.service.VerificationTokenService;
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
    private UserService userService;

    @Autowired
    private TotpService totpService;

    @Autowired
    private VerificationTokenService tokenService;

    @Autowired
    private EmailService emailService;

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

        User user = (User) userService.findByEmail(userAuthenticated.getPrincipal().toString());

        boolean validOtp = totpService.verifyTotpCode(user.getTotpSecret(), otpRequest.getOtpCode());
        if (!validOtp) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código OTP inválido");
        }

        String token = jwtService.generateToken(user, user.getAuthorities(), 60);
        return new ResponseEntity<>(new LoginResponse(token), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader String token, @RequestBody @Valid ResetPasswordRequest request) {
        String userId = tokenService.validateVerificationToken(token);
        if (userId != null) {
            User user = userService.findById(userId);

            if (user != null) {
                if (request.getPassword().equals(request.getConfirmPassword())) {
                    user.setPassword(request.getPassword());
                    userService.updateUser(user);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido ou expirado.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        if (request != null) {
            User user = (User) userService.findByEmail(request.getEmail());

            if (user != null) {
                String token = tokenService.generateVerificationToken(user);
                emailService.sendForgotPassword(user.getEmail(), token);
            }
        }

        return ResponseEntity.ok("Recuperação de senha solicitada !");
    }
}