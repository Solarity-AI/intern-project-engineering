package com.example.productreview.exception;

import com.example.productreview.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void resourceNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validationError_shouldReturn400WithDetails() throws Exception {
        String invalidReview = "{\"reviewerName\":\"J\",\"comment\":\"Short\",\"rating\":6}";

        mockMvc.perform(post("/api/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidReview))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isMap());
    }

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
