package com.example.productreview.exception;

import com.example.productreview.dto.ErrorResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionModelUnitTest {

    @Test
    void resourceNotFoundException_shouldHaveCorrectMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product", 42L);
        assertEquals("Product not found with id: 42", ex.getMessage());
    }

    @Test
    void resourceNotFoundException_withStringMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void validationException_shouldHaveCorrectMessage() {
        ValidationException ex = new ValidationException("Invalid input");
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    void unauthorizedException_shouldHaveCorrectMessage() {
        UnauthorizedException ex = new UnauthorizedException("Access denied");
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void errorResponse_shouldPopulateAllFields() {
        ErrorResponse response = new ErrorResponse(404, "Not found");
        assertEquals(404, response.getCode());
        assertEquals("Not found", response.getMessage());
        assertNotNull(response.getTimestamp());
        assertNull(response.getDetails());
    }
}
