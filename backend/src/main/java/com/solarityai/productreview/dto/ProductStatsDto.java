package com.solarityai.productreview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatsDto {
    private Long totalProducts;
    private Long totalReviews;
    private Double averageRating;
}
