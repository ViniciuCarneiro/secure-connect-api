package com.secure.connect.secure_connect.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String email, String token) {
        String url = "http://localhost:8080/api/auth/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirmação de E-mail");
        message.setText("Para confirmar seu cadastro, clique no link: " + url);
        mailSender.send(message);
    }

    public void sendForgotPassword(String email, String token) {
        String url = "http://localhost:8080/api/auth/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirmação de E-mail");
        message.setText("Para confirmar seu cadastro, clique no link: " + url);
        mailSender.send(message);
    }
}