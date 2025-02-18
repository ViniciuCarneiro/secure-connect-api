package com.secure.connect.secure_connect.email.service;

import com.secure.connect.secure_connect.exception.EmailSendingException;
import com.secure.connect.secure_connect.user.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.url.verify-email}")
    private String verifyEmailUrl;

    @Value("${app.url.reset-password}")
    private String resetPasswordUrl;

    @Async
    public void sendVerificationEmail(String email, String token) {

        log.info("Enviando email de verificação de conta para o email: {}", email);

        String subject = "Verificação de Conta - Secure Connect";

        Map<String, Object> variables = new HashMap<>();
        variables.put("email", email);
        variables.put("verificationLink", verifyEmailUrl + "?token=" + token);

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("emails/verificacao_conta", context);

        sendEmail(email, subject, htmlContent);
    }

    @Async
    public void sendForgotPassword(String email, String token) {

        log.info("Enviando email de recuperação de senha para o email: {}", email);

        String subject = "Recuperação de Senha - Secure Connect";

        Map<String, Object> variables = new HashMap<>();
        variables.put("email", email);
        variables.put("resetLink", resetPasswordUrl + "?token=" + token);

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("emails/recuperar_senha", context);

        sendEmail(email, subject, htmlContent);
    }

    @Async
    public void sendSuspiciousActivity(String email) {

        log.info("Enviando email de atividade suspeita para o email: {}", email);

        String subject = "Alerta de Atividade Suspeita - Secure Connect";

        Map<String, Object> variables = new HashMap<>();
        variables.put("email", email);

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("emails/alerta-atividade-suspeita", context);

        sendEmail(email, subject, htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {

        log.info("from {}", fromEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("E-mail '{}' enviado para {}", subject, to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail para {}: {}", to, e.getMessage());
            throw new EmailSendingException("Falha ao enviar e-mail para " + to, e);
        }
    }
}
