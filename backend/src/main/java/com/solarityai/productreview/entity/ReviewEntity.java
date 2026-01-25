package com.solarityai.productreview.entity;

import com.solarityai.backendfw.foundation.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class ReviewEntity extends BaseEntity {

    @Column(name = "reviewer_name", nullable = false)
    private String reviewerName;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "review_created_at")
    private Instant reviewCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @PrePersist
    protected void onCreate() {
        if (reviewCreatedAt == null) {
            reviewCreatedAt = Instant.now();
        }
    }
}
