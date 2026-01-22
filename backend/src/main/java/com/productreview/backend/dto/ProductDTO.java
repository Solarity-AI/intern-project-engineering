package com.productreview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Set<String> categories;
    private Double price;
    private String imageUrl;
    private Double averageRating;
    private Integer reviewCount;
    private Map<Integer, Integer> ratingBreakdown;
    private String aiSummary;
}