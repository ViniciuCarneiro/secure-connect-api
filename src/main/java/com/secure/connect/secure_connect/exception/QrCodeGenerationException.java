package com.secure.connect.secure_connect.exception;

public class QrCodeGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public QrCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}