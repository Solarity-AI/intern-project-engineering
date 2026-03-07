package com.example.productreview.config;

public class AuthenticatedUserContextMissingException extends RuntimeException {

    public AuthenticatedUserContextMissingException(String message) {
        super(message);
    }
}
