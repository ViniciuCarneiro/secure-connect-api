package com.secure.connect.secure_connect.exception;

public class TotpVerificationException extends RuntimeException {
    public TotpVerificationException(String message) {
        super(message);
    }

    public TotpVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}