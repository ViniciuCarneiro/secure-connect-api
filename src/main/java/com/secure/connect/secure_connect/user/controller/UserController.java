package com.secure.connect.secure_connect.user.controller;

import com.secure.connect.secure_connect.email.service.EmailService;
import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.domain.dto.request.UserRequest;
import com.secure.connect.secure_connect.user.domain.dto.response.ResponseStandard;
import com.secure.connect.secure_connect.user.domain.mapper.UserMapper;
import com.secure.connect.secure_connect.user.service.UserService;
import com.secure.connect.secure_connect.user.service.VerificationTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    VerificationTokenService tokenService;

    @Autowired
    EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<ResponseStandard> register(@RequestBody @Valid UserRequest request) {
        try {
            User user = userService.registerUser(UserMapper.userRequestToUser(request));

            String token = tokenService.generateVerificationToken(user);
            emailService.sendVerificationEmail(user.getEmail(), token);

            ResponseStandard response = new ResponseStandard("User successfully registered",
                    Optional.of(UserMapper.userToUserResponse(user)));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseStandard errorResponse = new ResponseStandard("Failed to register user: " + e.getMessage(), Optional.empty());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseStandard> find() {
         try {

             List<User> listUsers = userService.find();

            ResponseStandard response = new ResponseStandard("Successful user research",
                    Optional.of(UserMapper.userListToUserResponse(listUsers)));

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ResponseStandard errorResponse = new ResponseStandard("Failed user research: " + e.getMessage(), Optional.empty());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean isValid = tokenService.validateVerificationToken(token);
        if (isValid) {
            return ResponseEntity.ok("E-mail confirmado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido ou expirado.");
    }
}
