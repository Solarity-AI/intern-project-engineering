package com.example.productreview.exception;

import com.example.productreview.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest extends BaseIntegrationTest {

    @Test
    void resourceNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validationError_shouldReturn400WithDetails() throws Exception {
        String invalidReview = "{\"reviewerName\":\"J\",\"comment\":\"Short\",\"rating\":6}";

        mockMvc.perform(post("/api/v1/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidReview))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void missingHeader_shouldReturn400WithHeaderName() throws Exception {
        mockMvc.perform(get("/api/v1/products/reviews/voted"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Required header 'X-User-ID' is missing"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validationException_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be negative"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
