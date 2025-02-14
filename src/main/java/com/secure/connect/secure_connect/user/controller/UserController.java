package com.secure.connect.secure_connect.user.controller;

import com.secure.connect.secure_connect.email.service.EmailService;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.domain.dto.request.UserRequest;
import com.secure.connect.secure_connect.user.domain.dto.response.StandardResponse;
import com.secure.connect.secure_connect.user.domain.dto.response.UserListResponse;
import com.secure.connect.secure_connect.user.domain.dto.response.UserResponse;
import com.secure.connect.secure_connect.user.domain.mapper.UserMapper;
import com.secure.connect.secure_connect.user.service.UserService;
import com.secure.connect.secure_connect.user.service.VerificationTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenService tokenService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/search")
    public ResponseEntity<StandardResponse<UserListResponse>> find() {

        try {
            List<User> listUsers = userService.findAll();

            StandardResponse<UserListResponse> response = StandardResponse.<UserListResponse>builder()
                    .success(true)
                    .message("User search completed successfully.")
                    .data(new UserListResponse(UserMapper.userListToUserResponse(listUsers)))
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            StandardResponse< UserListResponse > response = StandardResponse.<UserListResponse>builder()
                    .success(false)
                    .message("User search failed")
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<UserResponse>> register(@RequestBody @Valid UserRequest request) {

        try {
            User user = userService.registerUser(UserMapper.userRequestToUser(request));

            String token = tokenService.generateVerificationToken(user);
            emailService.sendVerificationEmail(user.getEmail(), token);

            StandardResponse< UserResponse > response = StandardResponse.<UserResponse>builder()
                    .success(true)
                    .message( "User registered successfully. Verification email sent.")
                    .data(UserMapper.userToUserResponse(user, "secure-connect"))
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            StandardResponse< UserResponse > response = StandardResponse.<UserResponse>builder()
                    .success(false)
                    .message("User registration failed")
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<StandardResponse<UserResponse>> verifyEmail(@RequestParam String token) {

        String userId = tokenService.validateVerificationToken(token);

        if (userId != null) {
            if (userService.verifiedEmail(userId)) {
                tokenService.deleteToken(token);

                StandardResponse< UserResponse > response = StandardResponse.<UserResponse>builder()
                        .success(true)
                        .message("Email confirmed successfully.")
                        .data(null)
                        .build();

                return ResponseEntity.ok(response);
            } else {
                StandardResponse< UserResponse > response = StandardResponse.<UserResponse>builder()
                        .success(false)
                        .message("Email verification failed: User not verified.")
                        .data(null)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        StandardResponse< UserResponse > response = StandardResponse.<UserResponse>builder()
                .success(false)
                .message("Email verification failed: Invalid or expired token.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
