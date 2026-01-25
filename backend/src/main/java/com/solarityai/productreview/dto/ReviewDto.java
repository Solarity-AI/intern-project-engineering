package com.solarityai.productreview.dto;

import com.solarityai.backendfw.foundation.model.BaseDto;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewDto extends BaseDto {

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

    private Integer helpfulCount;
    private Instant reviewCreatedAt;
    private UUID productId;
}
