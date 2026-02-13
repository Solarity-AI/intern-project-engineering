package com.example.productreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Set;

@Schema(description = "Product details including ratings and AI summary")
public class ProductDTO {

    @Schema(description = "Unique product identifier", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "Product description", example = "The latest iPhone with A17 Pro chip and Titanium design.")
    private String description;

    @Schema(description = "Product categories", example = "[\"Electronics\", \"Smartphones\"]")
    private Set<String> categories;

    @Schema(description = "Product price in USD", example = "999.99")
    private Double price;

    @Schema(description = "Product image URL", example = "https://images.unsplash.com/photo-1695048133142-1a20484d2569")
    private String imageUrl;

    @Schema(description = "Average star rating (1.0-5.0)", example = "4.3")
    private Double averageRating;

    @Schema(description = "Total number of reviews", example = "42")
    private Integer reviewCount;

    @Schema(description = "Rating distribution (star count to number of reviews)", example = "{\"1\": 2, \"2\": 3, \"3\": 5, \"4\": 12, \"5\": 20}")
    private Map<Integer, Long> ratingBreakdown;

    @Schema(description = "AI-generated review summary (only on single product detail)", example = "Users praise the premium design and camera quality...")
    private String aiSummary;

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, String description, Set<String> categories, Double price, String imageUrl, Double averageRating, Integer reviewCount, Map<Integer, Long> ratingBreakdown, String aiSummary) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.price = price;
        this.imageUrl = imageUrl;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.ratingBreakdown = ratingBreakdown;
        this.aiSummary = aiSummary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Map<Integer, Long> getRatingBreakdown() {
        return ratingBreakdown;
    }

    public void setRatingBreakdown(Map<Integer, Long> ratingBreakdown) {
        this.ratingBreakdown = ratingBreakdown;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }
}
