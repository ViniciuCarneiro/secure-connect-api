package com.secure.connect.secure_connect.auth.controller;

import com.secure.connect.secure_connect.auth.domain.enums.Authority;
import com.secure.connect.secure_connect.auth.domain.request.ForgotPasswordRequest;
import com.secure.connect.secure_connect.auth.domain.request.LoginRequest;
import com.secure.connect.secure_connect.auth.domain.request.OtpVerificationRequest;
import com.secure.connect.secure_connect.auth.domain.request.ResetPasswordRequest;
import com.secure.connect.secure_connect.auth.domain.response.LoginResponse;
import com.secure.connect.secure_connect.auth.domain.response.StandardResponse;
import com.secure.connect.secure_connect.auth.jwt.JwtService;
import com.secure.connect.secure_connect.auth.service.TotpService;
import com.secure.connect.secure_connect.email.service.EmailService;
import com.secure.connect.secure_connect.user.domain.User;
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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<StandardResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String token;

            if (!user.isMfaEnabled()) {
                token = jwtService.generateToken(user, user.getAuthorities(), 60);
            } else {
                var preAuthAuthority = new SimpleGrantedAuthority(Authority.PRE_AUTH_MFA.name());
                token = jwtService.generateToken(user, java.util.List.of(preAuthAuthority), 5);
            }

            StandardResponse<LoginResponse> response = StandardResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Autenticação realizada com sucesso.")
                    .data(new LoginResponse(token))
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            StandardResponse<LoginResponse> response = StandardResponse.<LoginResponse>builder()
                    .success(false)
                    .message("Falha na autenticação: Credenciais inválidas. Verifique seu e-mail e senha.")
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<StandardResponse<LoginResponse>> verifyOtp(@RequestBody @Valid OtpVerificationRequest otpRequest) {

        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) userService.findByEmail(userAuthenticated.getPrincipal().toString());

        boolean validOtp = totpService.verifyTotpCode(user.getTotpSecret(), otpRequest.getOtpCode());

        if (!validOtp) {
            StandardResponse<LoginResponse> response = StandardResponse.<LoginResponse>builder()
                    .success(false)
                    .message("Falha na verificação do código OTP: Código inválido. Verifique e tente novamente.")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = jwtService.generateToken(user, user.getAuthorities(), 60);
        StandardResponse<LoginResponse> response = StandardResponse.<LoginResponse>builder()
                .success(true)
                .message("Código OTP validado com sucesso.")
                .data(new LoginResponse(token))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponse<Object>> resetPassword(@RequestHeader String token, @RequestBody @Valid ResetPasswordRequest request) {

        String userId = tokenService.validateVerificationToken(token);

        if (userId == null) {
            StandardResponse<Object> response = StandardResponse.builder()
                    .success(false)
                    .message("Falha na redefinição de senha: Token inválido ou expirado. Solicite uma nova redefinição.")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User user = userService.findById(userId);

        if (user == null) {
            StandardResponse<Object> response = StandardResponse.builder()
                    .success(false)
                    .message("Token inválido. Solicite uma nova redefinição.")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            StandardResponse<Object> response = StandardResponse.builder()
                    .success(false)
                    .message("Falha na redefinição de senha: As senhas informadas não conferem.")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setPassword(request.getPassword());
        userService.updateUser(user);
        StandardResponse<Object> response = StandardResponse.builder()
                .success(true)
                .message("Senha redefinida com sucesso. Tente um novo acesso")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponse<Object>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {

        User user = (User) userService.findByEmail(request.getEmail());

        if (user != null) {
            String token = tokenService.generateVerificationToken(user);
            emailService.sendForgotPassword(user.getEmail(), token);
        }

        StandardResponse<Object> response = StandardResponse.builder()
                .success(true)
                .message("Solicitação de redefinição de senha efetuada com sucesso. Você receberá instruções em breve.")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
