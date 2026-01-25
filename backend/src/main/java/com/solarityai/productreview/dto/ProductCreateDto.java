package com.solarityai.productreview.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductCreateDto {

    @NotBlank(message = "error.product.name.required")
    private String name;

    private String description;

    private Set<String> categories;

    @NotNull(message = "error.product.price.required")
    @DecimalMin(value = "0.0", message = "error.product.price.positive")
    private BigDecimal price;

    private String imageUrl;
}
