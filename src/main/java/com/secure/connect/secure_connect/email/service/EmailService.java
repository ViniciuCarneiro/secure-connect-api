package com.secure.connect.secure_connect.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String email, String token) {
        String url = "http://localhost:8080/api/auth/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirmação de E-mail");
        message.setText("Para confirmar seu cadastro, clique no link: " + url);
        mailSender.send(message);
    }

    @Async
    public void sendForgotPassword(String email, String token) {
        String url = "http://localhost:8080/api/auth/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Recuperação de senha");
        message.setText("Para recuperar sua senha, clique no link: " + url);
        mailSender.send(message);
    }

    @Async
    public void sendSuspiciousActivity(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Alerta de tentativas de login");
        message.setText("Alerta: múltiplas tentativas de login falhas para: " + email);
        mailSender.send(message);
    }
}