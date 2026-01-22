package com.productreview.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "review_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    public ReviewVote(String userId, Long reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
    }
}