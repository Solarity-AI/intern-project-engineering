package com.example.productreview.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ModelUnitTest {

    // --- Review ---

    @Test
    void review_OnCreate_ShouldSetCreatedAt() {
        Review review = new Review();
        assertNull(review.getCreatedAt());
        review.onCreate();
        assertNotNull(review.getCreatedAt());
    }

    @Test
    void review_OnCreate_WithNullHelpfulCount_ShouldDefaultToZero() {
        Review review = new Review();
        review.setHelpfulCount(null);
        review.onCreate();
        assertEquals(0, review.getHelpfulCount());
    }

    @Test
    void review_AllArgsConstructor_ShouldSetAllFields() {
        Product product = new Product();
        LocalDateTime now = LocalDateTime.now();
        Review review = new Review(1L, "John", "Great product", 5, 10, now, product);

        assertEquals(1L, review.getId());
        assertEquals("John", review.getReviewerName());
        assertEquals("Great product", review.getComment());
        assertEquals(5, review.getRating());
        assertEquals(10, review.getHelpfulCount());
        assertEquals(now, review.getCreatedAt());
        assertEquals(product, review.getProduct());
    }

    // --- ReviewVote ---

    @Test
    void reviewVote_Constructor_ShouldSetFields() {
        Review review = new Review();
        review.setId(42L);
        ReviewVote vote = new ReviewVote("user-123", review);

        assertEquals("user-123", vote.getUserId());
        assertEquals(review, vote.getReview());
        assertEquals(42L, vote.getReviewId());
    }

    @Test
    void reviewVote_GetReviewId_WithNullReview_ShouldReturnNull() {
        ReviewVote vote = new ReviewVote();
        assertNull(vote.getReviewId());
    }

    // --- WishlistItem ---

    @Test
    void wishlistItem_Constructor_ShouldSetFields() {
        WishlistItem item = new WishlistItem("user-456", 7L);
        assertEquals("user-456", item.getUserId());
        assertEquals(7L, item.getProductId());
    }

    @Test
    void wishlistItem_SettersAndGetters_ShouldWork() {
        WishlistItem item = new WishlistItem();
        item.setId(1L);
        item.setUserId("user-789");
        item.setProductId(42L);

        assertEquals(1L, item.getId());
        assertEquals("user-789", item.getUserId());
        assertEquals(42L, item.getProductId());
    }

    // --- AppNotification ---

    @Test
    void appNotification_Constructor_ShouldSetFields() {
        AppNotification notification = new AppNotification("user-1", "Title", "Message", 5L);
        assertEquals("user-1", notification.getUserId());
        assertEquals("Title", notification.getTitle());
        assertEquals("Message", notification.getMessage());
        assertEquals(5L, notification.getProductId());
    }

    @Test
    void appNotification_DefaultValues_ShouldBeSet() {
        AppNotification notification = new AppNotification();
        assertFalse(notification.isRead());
        assertNotNull(notification.getCreatedAt());
    }
}
