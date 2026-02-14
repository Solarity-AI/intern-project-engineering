package com.example.productreview.controller;

import com.example.productreview.dto.ReviewDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
