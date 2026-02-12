package com.example.productreview.model;

import jakarta.persistence.*;

@Entity
@Table(name = "review_votes", indexes = {
    @Index(name = "idx_review_vote_user", columnList = "userId"),
    @Index(name = "idx_review_vote_review", columnList = "reviewId")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_review_vote_user_review", columnNames = {"userId", "reviewId"})
})
public class ReviewVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private Long reviewId;

    public ReviewVote() {}

    public ReviewVote(String userId, Long reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
}
