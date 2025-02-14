package com.secure.connect.secure_connect.user.controller;


import com.secure.connect.secure_connect.email.service.EmailService;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.domain.dto.request.UserRequest;
import com.secure.connect.secure_connect.user.domain.dto.request.UserUpdateRequest;
import com.secure.connect.secure_connect.user.domain.dto.response.StandardResponse;
import com.secure.connect.secure_connect.user.domain.dto.response.UserListResponse;
import com.secure.connect.secure_connect.user.domain.dto.response.UserResponse;
import com.secure.connect.secure_connect.user.domain.mapper.UserMapper;
import com.secure.connect.secure_connect.user.service.UserService;
import com.secure.connect.secure_connect.user.service.VerificationTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Gerenciamento de usuários")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Consultar todos os usuários", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários obtida com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserListResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<StandardResponse<UserListResponse>> find() {
        try {
            List<User> listUsers = userService.findAll();
            StandardResponse<UserListResponse> response = StandardResponse.<UserListResponse>builder()
                    .success(true)
                    .message("Busca de usuários concluída com sucesso.")
                    .data(new UserListResponse(UserMapper.userListToUserResponse(listUsers)))
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            StandardResponse<UserListResponse> response = StandardResponse.<UserListResponse>builder()
                    .success(false)
                    .message("Falha na busca de usuários")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Registra um novo usuário", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "500", description = "Falha ao registrar o usuário",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<StandardResponse<UserResponse>> register(@RequestBody @Valid UserRequest request) {
        try {
            User user = userService.registerUser(UserMapper.userRequestToUser(request));
            String token = tokenService.generateVerificationToken(user);
            emailService.sendVerificationEmail(user.getEmail(), token);
            StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                    .success(true)
                    .message("Usuário registrado com sucesso. E-mail de verificação enviado.")
                    .data(UserMapper.userToUserResponse(user, "secure-connect"))
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                    .success(false)
                    .message("Falha ao registrar o usuário")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Atualiza um usuário existente", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "500", description = "Falha ao atualizar o usuário",
                    content = @Content)
    })
    @PutMapping("/update")
    public ResponseEntity<StandardResponse<UserResponse>> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        try {
            Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) userService.findByEmail(userAuthenticated.getPrincipal().toString());
            if (user != null) {
                user.setName(request.getName());
                user.setUsername(request.getUsername());
                user.setPassword(request.getPassword());
                userService.updateUser(user);
                StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                        .success(true)
                        .message("Usuário atualizado com sucesso.")
                        .data(UserMapper.userToUserResponse(user, "secure-connect"))
                        .build();
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                        .success(false)
                        .message("Falha ao atualizar o usuário")
                        .data(null)
                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                    .success(false)
                    .message("Falha ao atualizar o usuário")
                    .data(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
            summary = "Verifica conta por e-mail",
            description = "Confirma o e-mail do usuário através de um token enviado por e-mail.",
            parameters = {
                    @Parameter(name = "token", description = "Token de verificação enviado por e-mail", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "E-mail confirmado com sucesso",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = StandardResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Falha na verificação do e-mail: Token inválido ou expirado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = StandardResponse.class))
                    )
            }
    )
    @GetMapping("/verify-email")
    public ResponseEntity<StandardResponse<UserResponse>> verifyEmail(@RequestParam String token) {
        String userId = tokenService.validateVerificationToken(token);

        if (userId != null) {
            if (userService.verifiedEmail(userId)) {
                tokenService.deleteToken(token);

                StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                        .success(true)
                        .message("Email confirmado com sucesso.")
                        .data(null)
                        .build();

                return ResponseEntity.ok(response);
            } else {
                StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                        .success(false)
                        .message("Falha na verificação do e-mail: Usuário não verificado.")
                        .data(null)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        StandardResponse<UserResponse> response = StandardResponse.<UserResponse>builder()
                .success(false)
                .message("Falha na verificação do e-mail: Token inválido ou expirado.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}