package com.example.productreview.model;

import jakarta.persistence.*;

@Entity
@Table(name = "review_votes", indexes = {
    @Index(name = "idx_review_vote_user", columnList = "user_id"),
    @Index(name = "idx_review_vote_review", columnList = "review_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_review_vote_user_review", columnNames = {"user_id", "review_id"})
})
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId; // Internal user ID resolved from Clerk mapping

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    public ReviewVote() {}

    public ReviewVote(String userId, Review review) {
        this.userId = userId;
        this.review = review;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
    public Long getReviewId() { return review != null ? review.getId() : null; }
}
