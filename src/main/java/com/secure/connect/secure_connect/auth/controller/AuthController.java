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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "Autenticação e Autorização")
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

    @Operation(summary = "Realiza o login do usuário e gera um token JWT", description = "Este endpoint realiza a autenticação do usuário e retorna um token JWT para autenticações futuras.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso.", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Falha na autenticação, credenciais inválidas", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
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

    @Operation(summary = "Verifica o código OTP para autenticação de múltiplos fatores", description = "Este endpoint verifica o código OTP enviado ao usuário para permitir a autenticação de múltiplos fatores.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código OTP validado com sucesso", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Falha na verificação do código OTP", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
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

    @Operation(summary = "Redefine a senha do usuário", description = "Este endpoint permite redefinir a senha do usuário utilizando um token de verificação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Falha na redefinição de senha, token inválido ou expirado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)))
    })
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

    @Operation(summary = "Solicita a redefinição de senha", description = "Este endpoint permite ao usuário solicitar uma redefinição de senha através do envio de um email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação de redefinição de senha realizada com sucesso", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class)) })
    })
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
