package com.solarityai.productreview.mapper;

import com.solarityai.productreview.dto.ProductCreateDto;
import com.solarityai.productreview.dto.ProductDto;
import com.solarityai.productreview.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "ratingBreakdown", ignore = true)
    @Mapping(target = "aiSummary", ignore = true)
    ProductDto toDto(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewCount", constant = "0")
    @Mapping(target = "reviews", ignore = true)
    ProductEntity toEntity(ProductCreateDto dto);
}
