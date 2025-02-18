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
import com.secure.connect.secure_connect.exception.AuthenticationFailedException;
import com.secure.connect.secure_connect.exception.TokenNotFoundException;
import com.secure.connect.secure_connect.exception.TotpVerificationException;
import com.secure.connect.secure_connect.exception.UserNotFoundException;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.service.UserService;
import com.secure.connect.secure_connect.user.service.VerificationTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Autenticação e Autorização")
@Slf4j
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

    @Operation(summary = "Realiza o login do usuário e gera um token JWT",
            description = "Este endpoint realiza a autenticação do usuário e retorna um token JWT para autenticações futuras.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Falha na autenticação, credenciais inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<StandardResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {

        log.info("Iniciando autenticação para o email: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String token;

            if (!user.isMfaEnabled()) {
                log.info("Gerando token para usuário {} sem MFA.", user.getEmail());
                token = jwtService.generateToken(user, user.getAuthorities(), 60);
            } else {
                log.info("Gerando token para usuário {} com MFA pré-autorizado.", user.getEmail());
                var preAuthAuthority = new SimpleGrantedAuthority(Authority.PRE_AUTH_MFA.name());
                token = jwtService.generateToken(user, List.of(preAuthAuthority), 5);
            }

            log.info("Autenticação realizada com sucesso para o email: {}", loginRequest.getEmail());

            StandardResponse<LoginResponse> response = StandardResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Autenticação realizada com sucesso.")
                    .data(new LoginResponse(token))
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Falha na autenticação para o email {}: {}", loginRequest.getEmail(), ex.getMessage(), ex);
            throw new AuthenticationFailedException("Falha na autenticação: Credenciais inválidas. Verifique seu e-mail e senha.", ex);
        }
    }

    @Operation(summary = "Verifica o código OTP para autenticação de múltiplos fatores",
            description = "Este endpoint verifica o código OTP enviado ao usuário para permitir a autenticação de múltiplos fatores.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código OTP validado com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Falha na verificação do código OTP",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping("/login/verify-otp")
    public ResponseEntity<StandardResponse<LoginResponse>> verifyOtp(@RequestBody @Valid OtpVerificationRequest otpRequest) {

        log.info("Iniciando verificação de OTP...");

        try {
            Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) userService.findByEmail(userAuthenticated.getPrincipal().toString());

            boolean validOtp = totpService.verifyTotpCode(user.getTotpSecret(), otpRequest.getOtpCode());

            if (!validOtp) {
                log.warn("Código OTP inválido para o usuário: {}", user.getEmail());
                throw new TotpVerificationException("Falha na verificação do código OTP: Código inválido.");
            }

            String token = jwtService.generateToken(user, user.getAuthorities(), 60);
            log.info("Código OTP validado com sucesso para o usuário: {}", user.getEmail());

            StandardResponse<LoginResponse> response = StandardResponse.<LoginResponse>builder()
                    .success(true)
                    .message("Código OTP validado com sucesso.")
                    .data(new LoginResponse(token))
                    .build();

            return ResponseEntity.ok(response);
        } catch (TotpVerificationException ex) {
            log.error("Erro na verificação do OTP: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado na verificação do OTP: {}", ex.getMessage(), ex);
            throw new TotpVerificationException("Erro inesperado na verificação do código OTP.", ex);
        }
    }

    @Operation(summary = "Redefine a senha do usuário",
            description = "Este endpoint permite redefinir a senha do usuário utilizando um token de verificação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Falha na redefinição de senha, token inválido ou expirado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponse<Object>> resetPassword(@RequestParam String token, @RequestBody @Valid ResetPasswordRequest request) {

        log.info("Iniciando fluxo de reset de senha...");

        try {
            String userId = tokenService.validateVerificationToken(token);

            if (userId == null) {
                log.warn("Token inválido ou expirado: {}", token);
                throw new TokenNotFoundException("Falha na redefinição de senha: Token inválido ou expirado. Solicite uma nova redefinição.");
            }

            User user = userService.findById(userId);

            if (user == null) {
                log.warn("Usuário não encontrado para o token: {}", token);
                throw new UserNotFoundException("Falha na redefinição de senha: Usuário não encontrado.");
            }

            if (!request.getPassword().equals(request.getConfirmPassword())) {
                log.warn("As senhas informadas não conferem para o usuário: {}", user.getEmail());
                throw new IllegalArgumentException("Falha na redefinição de senha: As senhas informadas não conferem.");
            }

            user.setPassword(request.getPassword());

            userService.updateUser(user);

            log.info("Senha redefinida com sucesso para o usuário: {}", user.getEmail());

            StandardResponse<Object> response = StandardResponse.builder()
                    .success(true)
                    .message("Senha redefinida com sucesso. Tente um novo acesso")
                    .data(null)
                    .build();

            return ResponseEntity.ok(response);
        } catch (TokenNotFoundException | UserNotFoundException | IllegalArgumentException ex) {
            log.error("Erro na redefinição de senha: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado na redefinição de senha: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro inesperado na redefinição de senha.", ex);
        }
    }

    @Operation(summary = "Solicita a redefinição de senha",
            description = "Este endpoint permite ao usuário solicitar uma redefinição de senha através do envio de um email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação de redefinição de senha realizada com sucesso",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)) })
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponse<Object>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {

        log.info("Iniciando solicitação de recuperação de senha para o email: {}", request.getEmail());

        try {
            User user = (User) userService.findByEmail(request.getEmail());

            if (user != null) {
                log.info("Usuário encontrado. Enviando email de recuperação de senha...");
                String token = tokenService.generateVerificationToken(user);
                emailService.sendForgotPassword(user.getEmail(), token);
            } else {
                log.warn("Usuário não encontrado para o email: {}", request.getEmail());
            }

            StandardResponse<Object> response = StandardResponse.builder()
                    .success(true)
                    .message("Solicitação de redefinição de senha efetuada com sucesso. Você receberá instruções em breve.")
                    .data(null)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Erro ao solicitar redefinição de senha para o email {}: {}", request.getEmail(), ex.getMessage(), ex);
            throw new RuntimeException("Erro ao solicitar redefinição de senha.", ex);
        }
    }
}