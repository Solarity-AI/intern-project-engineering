package com.example.productreview.controller;

import com.example.productreview.dto.ReviewDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "rate-limit.requests-per-minute=10000")
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllProducts_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addReview_WithValidData_ShouldReturnCreated() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("Jane Doe");
        reviewDTO.setComment("This is a very good product and I like it.");
        reviewDTO.setRating(5);

        mockMvc.perform(post("/api/v1/products/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewerName").value("Jane Doe"));
    }

    // --- Pagination Validation Tests ---

    @Test
    void getAllProducts_WithExcessiveSize_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100"));
    }

    @Test
    void getAllProducts_WithMaxSize_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllProducts_WithValidSize_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllProducts_WithNegativePage_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be negative"));
    }

    @Test
    void getAllProducts_WithoutSizeParam_ShouldDefaultTo10() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getReviews_WithExcessiveSize_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100"));
    }

    @Test
    void getReviews_WithNegativePage_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be negative"));
    }

    // --- Review Validation Tests ---

    @Test
    void addReview_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("J"); // Too short
        reviewDTO.setComment("Short"); // Too short
        reviewDTO.setRating(6); // Invalid rating

        mockMvc.perform(post("/api/v1/products/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isBadRequest());
    }
}
