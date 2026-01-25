package com.solarityai.productreview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewCreateDto {

    @NotBlank(message = "error.review.reviewerName.required")
    @Size(min = 2, max = 50, message = "error.review.reviewerName.size")
    private String reviewerName;

    @NotBlank(message = "error.review.comment.required")
    @Size(min = 10, max = 500, message = "error.review.comment.size")
    private String comment;

    @NotNull(message = "error.review.rating.required")
    @Min(value = 1, message = "error.review.rating.min")
    @Max(value = 5, message = "error.review.rating.max")
    private Integer rating;
}
