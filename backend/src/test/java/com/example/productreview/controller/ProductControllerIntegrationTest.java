package com.example.productreview.controller;

import com.example.productreview.BaseIntegrationTest;
import com.example.productreview.dto.ReviewDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllProducts_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/1").with(clerkAuth()))
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
                .with(clerkAuth("review-author"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewerName").value("Jane Doe"));
    }

    // --- Stats Endpoint Tests (#105) ---

    @Test
    void getGlobalStats_ShouldReturnOkWithValidStructure() throws Exception {
        mockMvc.perform(get("/api/v1/products/stats").with(clerkAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts", greaterThan(0)))
                .andExpect(jsonPath("$.totalReviews", greaterThan(0)))
                .andExpect(jsonPath("$.averageRating", greaterThan(0.0)));
    }

    @Test
    void getGlobalStats_WithCategoryFilter_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products/stats").with(clerkAuth()).param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts", greaterThan(0)))
                .andExpect(jsonPath("$.totalReviews", greaterThan(0)))
                .andExpect(jsonPath("$.averageRating", greaterThan(0.0)));
    }

    @Test
    void getGlobalStats_WithSearchFilter_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products/stats").with(clerkAuth()).param("search", "NonExistentProduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(0))
                .andExpect(jsonPath("$.totalReviews").value(0))
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }

    @Test
    void getGlobalStats_WithNonExistentCategoryAndSearch_ShouldReturnZeroState() throws Exception {
        mockMvc.perform(get("/api/v1/products/stats")
                .with(clerkAuth())
                .param("category", "NonExistentCategory")
                .param("search", "NonExistentProduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(0))
                .andExpect(jsonPath("$.totalReviews").value(0))
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }

    // --- Pagination Validation Tests ---

    @Test
    void getAllProducts_WithExcessiveSize_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()).param("size", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100"));
    }

    @Test
    void getAllProducts_WithMaxSize_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllProducts_WithValidSize_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()).param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllProducts_WithNegativePage_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()).param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be negative"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getAllProducts_WithoutSizeParam_ShouldDefaultTo10() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getReviews_WithExcessiveSize_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100"));
    }

    @Test
    void getReviews_WithNegativePage_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be negative"));
    }

    @Test
    void getReviews_WithMaxSize_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // --- Rating Filter Validation Tests ---

    @Test
    void getReviews_WithValidRating_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("rating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getReviews_WithRatingZero_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("rating", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating must be between 1 and 5"));
    }

    @Test
    void getReviews_WithRatingAboveFive_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("rating", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating must be between 1 and 5"));
    }

    @Test
    void getReviews_WithNegativeRating_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("rating", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating must be between 1 and 5"));
    }

    // --- Sort Field Validation Tests ---

    @Test
    void getReviews_WithValidSortField_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("sort", "rating,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getReviews_WithInvalidSortField_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Review Pagination Edge Cases ---

    @Test
    void getReviews_WithSizeZero_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page size must be at least 1"));
    }

    @Test
    void getReviews_WithSizeOne_ShouldReturnSingleReview() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void getReviews_WithOutOfBoundsPage_ShouldReturnEmptyContent() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()).param("page", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getReviews_WithDefaultParams_ShouldReturnPagedWithDefaults() throws Exception {
        mockMvc.perform(get("/api/v1/products/1/reviews").with(clerkAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    // --- Review Validation Tests ---

    @Test
    void addReview_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("J"); // Too short
        reviewDTO.setComment("Short"); // Too short
        reviewDTO.setRating(6); // Invalid rating

        mockMvc.perform(post("/api/v1/products/1/reviews")
                .with(clerkAuth("review-validator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isBadRequest());
    }

    // --- Voted Reviews Endpoint Tests ---

    @Test
    void getUserVotedReviews_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/products/reviews/voted")
                        .with(clerkAuth("user-with-no-votes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUserVotedReviews_WithoutAuthorization_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/products/reviews/voted"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    // --- Chat Endpoint Tests ---

    @Test
    void chatAboutProduct_WithValidQuestion_ShouldReturnAnswer() throws Exception {
        mockMvc.perform(post("/api/v1/products/1/chat")
                        .with(clerkAuth("chat-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"How many reviews does this product have?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists())
                .andExpect(jsonPath("$.answer").isString());
    }

    @Test
    void chatAboutProduct_WithEmptyQuestion_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/products/1/chat")
                        .with(clerkAuth("chat-validator"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.question").value("Question is required"));
    }

    @Test
    void chatAboutProduct_WithMissingQuestionKey_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/products/1/chat")
                        .with(clerkAuth("chat-validator"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"This has no question key\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.question").value("Question is required"));
    }

    // --- Product Sort Field Validation Tests ---

    @Test
    void getAllProducts_WithInvalidSortField_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()).param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Chat 404 Test ---

    @Test
    void chatAboutProduct_WithNonExistentProduct_ShouldReturn404() throws Exception {
        mockMvc.perform(post("/api/v1/products/99999/chat")
                        .with(clerkAuth("chat-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"How is the quality?\"}"))
                .andExpect(status().isNotFound());
    }

    // --- Sort Direction Tests ---

    @Test
    void getAllProducts_WithDescSort_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth()).param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // --- Helpful Vote Endpoint Test ---

    @Test
    void markReviewAsHelpful_ShouldToggleHelpfulCount() throws Exception {
        String firstResponse = mockMvc.perform(put("/api/v1/products/reviews/1/helpful")
                        .with(clerkAuth("toggle-test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andReturn().getResponse().getContentAsString();

        int firstCount = objectMapper.readTree(firstResponse).get("helpfulCount").asInt();

        String secondResponse = mockMvc.perform(put("/api/v1/products/reviews/1/helpful")
                        .with(clerkAuth("toggle-test-user")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int secondCount = objectMapper.readTree(secondResponse).get("helpfulCount").asInt();

        assertEquals(firstCount - 1, secondCount);
    }
}
