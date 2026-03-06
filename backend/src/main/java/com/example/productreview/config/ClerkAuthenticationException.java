package com.example.productreview.config;

public class ClerkAuthenticationException extends RuntimeException {

    public ClerkAuthenticationException(String message) {
        super(message);
    }

    public ClerkAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
