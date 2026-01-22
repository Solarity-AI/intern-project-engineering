package com.productreview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalStatsDTO {
    private Integer totalProducts;
    private Integer totalReviews;
    private Double averageRating;
}