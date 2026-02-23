package com.example.productreview.exception;

import com.example.productreview.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_shouldReturn400WithMessage() {
        ValidationException ex = new ValidationException("Invalid input data");
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertEquals("Invalid input data", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleUnauthorized_shouldReturn403() {
        UnauthorizedException ex = new UnauthorizedException("Access denied");
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getCode());
        assertEquals("Access denied", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleRuntimeException_shouldReturn500WithGenericMessage() {
        RuntimeException ex = new RuntimeException("NullPointerException");
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}
