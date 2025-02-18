package com.secure.connect.secure_connect.exception;

public class TotpGenerationException extends RuntimeException {
    public TotpGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}