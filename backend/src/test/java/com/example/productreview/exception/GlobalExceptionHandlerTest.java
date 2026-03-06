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
        mockMvc.perform(get("/api/v1/products/999999").with(clerkAuth()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validationError_shouldReturn400WithDetails() throws Exception {
        String invalidReview = "{\"reviewerName\":\"J\",\"comment\":\"Short\",\"rating\":6}";

        mockMvc.perform(post("/api/v1/products/1/reviews")
                        .with(clerkAuth("validation-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidReview))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void missingAuthorization_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products/reviews/voted"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void resourceNotFound_shouldIncludeTimestampAndCode() throws Exception {
        mockMvc.perform(get("/api/v1/products/888888").with(clerkAuth()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 888888"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").doesNotExist());
    }
}
