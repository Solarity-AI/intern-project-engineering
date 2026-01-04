package com.example.productreview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private String imageUrl; // New field
    private Double averageRating;
    private Integer reviewCount;
    private Map<Integer, Long> ratingBreakdown;
}
