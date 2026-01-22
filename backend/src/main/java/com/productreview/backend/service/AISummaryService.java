package com.productreview.backend.service;

import com.productreview.backend.entity.Review;

import java.util.List;

public interface AISummaryService {

    String generateReviewSummary(Long productId, String productName, List<Review> reviews);

    String chatWithReviews(Long productId, String question, List<Review> reviews);
}
