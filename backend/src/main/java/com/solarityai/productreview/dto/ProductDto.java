package com.solarityai.productreview.dto;

import com.solarityai.backendfw.foundation.model.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDto extends BaseDto {
    private String name;
    private String description;
    private Set<String> categories;
    private BigDecimal price;
    private String imageUrl;
    private Double averageRating;
    private Integer reviewCount;
    private Map<Integer, Long> ratingBreakdown;
    private String aiSummary;
}
