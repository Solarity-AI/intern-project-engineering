package com.example.productreview.service;

import com.example.productreview.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AISummaryServiceImplTest {

    private AISummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AISummaryServiceImpl("test-key", "gpt-4o-mini", 500);
    }

    // --- Test Mode Detection ---

    @Test
    void constructor_WithTestKey_ShouldEnableTestMode() {
        Review review = createReview(5, "Great quality product");
        String summary = service.generateReviewSummary(1L, "Test Product", Arrays.asList(review));
        assertNotNull(summary);
        assertTrue(summary.contains("customer review"));
    }

    @Test
    void constructor_WithEmptyKey_ShouldEnableTestMode() {
        AISummaryServiceImpl emptyKeyService = new AISummaryServiceImpl("", "gpt-4o-mini", 500);
        Review review = createReview(4, "Good product with nice design");
        String summary = emptyKeyService.generateReviewSummary(1L, "Product", Arrays.asList(review));
        assertNotNull(summary);
    }

    @Test
    void constructor_WithPlaceholderKey_ShouldEnableTestMode() {
        AISummaryServiceImpl placeholderService = new AISummaryServiceImpl("your-api-key-here", "gpt-4o-mini", 500);
        Review review = createReview(4, "Decent product");
        String summary = placeholderService.generateReviewSummary(1L, "Product", Arrays.asList(review));
        assertNotNull(summary);
    }

    // --- generateReviewSummary ---

    @Test
    void generateReviewSummary_WithNullReviews_ShouldReturnNull() {
        assertNull(service.generateReviewSummary(1L, "Product", null));
    }

    @Test
    void generateReviewSummary_WithEmptyReviews_ShouldReturnNull() {
        assertNull(service.generateReviewSummary(1L, "Product", Collections.emptyList()));
    }

    @Test
    void generateReviewSummary_WithPositiveReviews_ShouldContainPositiveSentiment() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Excellent quality and great performance"),
                createReview(4, "Good product, nice design"),
                createReview(5, "Amazing quality, fast speed")
        );

        String summary = service.generateReviewSummary(1L, "Test Product", reviews);

        assertNotNull(summary);
        assertTrue(summary.contains("positive"));
    }

    @Test
    void generateReviewSummary_WithNegativeReviews_ShouldContainNegativeSentiment() {
        List<Review> reviews = Arrays.asList(
                createReview(1, "Terrible product, expensive price"),
                createReview(2, "Bad battery life and many bugs"),
                createReview(1, "Would not recommend, too many issues")
        );

        String summary = service.generateReviewSummary(1L, "Test Product", reviews);

        assertNotNull(summary);
        assertTrue(summary.contains("negative"));
    }

    @Test
    void generateReviewSummary_WithMixedReviews_ShouldContainMixedSentiment() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Great product"),
                createReview(1, "Terrible product"),
                createReview(3, "Average product")
        );

        String summary = service.generateReviewSummary(1L, "Test Product", reviews);

        assertNotNull(summary);
        assertTrue(summary.contains("mixed"));
    }

    // --- chatWithReviews ---

    @Test
    void chatWithReviews_WithNullReviews_ShouldReturnNoReviewsMessage() {
        String result = service.chatWithReviews(1L, "How is quality?", null);
        assertTrue(result.contains("couldn't find any reviews"));
    }

    @Test
    void chatWithReviews_WithEmptyReviews_ShouldReturnNoReviewsMessage() {
        String result = service.chatWithReviews(1L, "How is quality?", Collections.emptyList());
        assertTrue(result.contains("couldn't find any reviews"));
    }

    @Test
    void chatWithReviews_HowManyQuestion_ShouldReturnCount() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Great"),
                createReview(4, "Good"),
                createReview(3, "OK")
        );

        String result = service.chatWithReviews(1L, "How many reviews are there?", reviews);
        assertTrue(result.contains("3"));
    }

    @Test
    void chatWithReviews_QualityQuestion_WithMostlyPositive_ShouldReturnHappy() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Excellent"),
                createReview(5, "Amazing"),
                createReview(4, "Great")
        );

        String result = service.chatWithReviews(1L, "Is the quality good?", reviews);
        assertTrue(result.contains("happy"));
    }

    @Test
    void chatWithReviews_QualityQuestion_WithMixedReviews_ShouldReturnMixed() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Great"),
                createReview(2, "Bad"),
                createReview(3, "OK"),
                createReview(1, "Terrible"),
                createReview(4, "Good")
        );

        String result = service.chatWithReviews(1L, "Is the quality good?", reviews);
        assertTrue(result.contains("mixed"));
    }

    @Test
    void chatWithReviews_ComplaintQuestion_WithNoNegativeReviews_ShouldReturnNoComplaints() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Perfect"),
                createReview(4, "Great")
        );

        String result = service.chatWithReviews(1L, "Are there any complaints?", reviews);
        assertTrue(result.contains("didn't find any major complaints"));
    }

    @Test
    void chatWithReviews_ComplaintQuestion_WithNegativeReviews_ShouldReturnComplaints() {
        List<Review> reviews = Arrays.asList(
                createReview(1, "Terrible"),
                createReview(2, "Bad"),
                createReview(5, "Great")
        );

        String result = service.chatWithReviews(1L, "What are the bad things?", reviews);
        assertTrue(result.contains("negative reviews"));
    }

    @Test
    void chatWithReviews_GenericQuestion_ShouldReturnGenericResponse() {
        List<Review> reviews = Arrays.asList(createReview(4, "Good product"));

        String result = service.chatWithReviews(1L, "Tell me about the warranty", reviews);
        assertTrue(result.contains("interesting question"));
    }

    // --- Additional Tests for Criteria Compliance ---

    @Test
    void constructor_WithNonSkPrefix_ShouldEnableTestMode() {
        AISummaryServiceImpl nonSkService = new AISummaryServiceImpl("not-starting-with-sk", "gpt-4o-mini", 500);
        Review review = createReview(4, "Good product");
        String summary = nonSkService.generateReviewSummary(1L, "Product", Arrays.asList(review));
        assertNotNull(summary);
    }

    @Test
    void constructor_WithNullKey_ShouldEnableTestMode() {
        AISummaryServiceImpl nullKeyService = new AISummaryServiceImpl(null, "gpt-4o-mini", 500);
        Review review = createReview(4, "Good product");
        String summary = nullKeyService.generateReviewSummary(1L, "Product", Arrays.asList(review));
        assertNotNull(summary);
    }

    @Test
    void generateReviewSummary_WithSingleReview_ShouldWork() {
        Review review = createReview(5, "Absolutely amazing quality");
        String summary = service.generateReviewSummary(1L, "Gadget", Collections.singletonList(review));
        assertNotNull(summary);
        assertTrue(summary.contains("1 customer review"));
    }

    @Test
    void generateReviewSummary_WithAllFiveStarReviews_ShouldBeOverwhelminglyPositive() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Perfect quality"),
                createReview(5, "Excellent performance"),
                createReview(5, "Outstanding design")
        );

        String summary = service.generateReviewSummary(1L, "Product", reviews);

        assertNotNull(summary);
        assertTrue(summary.contains("positive"));
    }

    @Test
    void generateReviewSummary_WithAllOneStarReviews_ShouldBeNegative() {
        List<Review> reviews = Arrays.asList(
                createReview(1, "Terrible product"),
                createReview(1, "Awful quality"),
                createReview(1, "Complete waste of money")
        );

        String summary = service.generateReviewSummary(1L, "Product", reviews);

        assertNotNull(summary);
        assertTrue(summary.contains("negative"));
    }

    @Test
    void generateReviewSummary_WithDesignPraise_ShouldMentionDesign() {
        List<Review> reviews = Arrays.asList(
                createReview(5, "Beautiful design and looks amazing"),
                createReview(5, "Love the look of this product")
        );

        String summary = service.generateReviewSummary(1L, "Product", reviews);

        assertNotNull(summary);
        assertTrue(summary.contains("design") || summary.contains("aesthetics"));
    }

    @Test
    void generateReviewSummary_WithBatteryComplaints_ShouldMentionBattery() {
        List<Review> reviews = Arrays.asList(
                createReview(1, "Terrible battery life"),
                createReview(2, "Battery drains too fast")
        );

        String summary = service.generateReviewSummary(1L, "Product", reviews);

        assertNotNull(summary);
        assertTrue(summary.toLowerCase().contains("battery"));
    }

    @Test
    void chatWithReviews_QualityQuestion_WithMostlyNegative_ShouldReturnConcerns() {
        List<Review> reviews = Arrays.asList(
                createReview(1, "Bad"),
                createReview(2, "Poor"),
                createReview(1, "Terrible"),
                createReview(1, "Awful"),
                createReview(3, "Meh")
        );

        String result = service.chatWithReviews(1L, "Is the quality good?", reviews);
        assertTrue(result.contains("concerns"));
    }

    @Test
    void chatWithReviews_HowManyWithSingleReview_ShouldReturn1() {
        List<Review> reviews = Collections.singletonList(createReview(4, "Good"));

        String result = service.chatWithReviews(1L, "How many reviews?", reviews);
        assertTrue(result.contains("1"));
    }

    private Review createReview(int rating, String comment) {
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewerName("TestUser");
        return review;
    }
}
