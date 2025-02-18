package com.secure.connect.secure_connect.auth.listener;

import com.secure.connect.secure_connect.email.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SuspiciousActivityListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private EmailService emailService;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private int failedAttempts = 0;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {

        failedAttempts++;

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            log.warn("Varias tentativas de login mal sucedida para a conta: {}", event.getAuthentication().getName());
            emailService.sendSuspiciousActivity(event.getAuthentication().getName());
            failedAttempts = 0;
        }
    }
}