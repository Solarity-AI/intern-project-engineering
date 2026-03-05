package com.example.productreview.service;

import com.example.productreview.model.Review;

import java.util.List;

public interface AISummaryService {

    String generateReviewSummary(Long productId, String productName, List<Review> reviews);

    String chatWithReviews(Long productId, String question, List<Review> reviews);
}
