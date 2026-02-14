package com.example.productreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Schema(description = "Review data for submission and display")
public class ReviewDTO {

    @Schema(description = "Unique review identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Reviewer name is required")
    @Size(min = 2, max = 50, message = "Reviewer name must be between 2 and 50 characters")
    @Schema(description = "Name of the reviewer", example = "Sarah", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
    private String reviewerName;

    @NotBlank(message = "Comment is required")
    @Size(min = 10, max = 500, message = "Comment must be between 10 and 500 characters")
    @Schema(description = "Review comment text", example = "Great product, highly recommended!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 10, maxLength = 500)
    private String comment;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Star rating", example = "5", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "5")
    private Integer rating;

    @Schema(description = "Number of helpful votes", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer helpfulCount;

    @Schema(description = "Review creation timestamp", example = "2026-02-13T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Associated product ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long productId;

    public ReviewDTO() {
    }

    public ReviewDTO(Long id, String reviewerName, String comment, Integer rating, Integer helpfulCount, LocalDateTime createdAt, Long productId) {
        this.id = id;
        this.reviewerName = reviewerName;
        this.comment = comment;
        this.rating = rating;
        this.helpfulCount = helpfulCount;
        this.createdAt = createdAt;
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
