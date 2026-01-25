package com.solarityai.productreview.mapper;

import com.solarityai.productreview.dto.ReviewCreateDto;
import com.solarityai.productreview.dto.ReviewDto;
import com.solarityai.productreview.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    ReviewDto toDto(ReviewEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "helpfulCount", constant = "0")
    @Mapping(target = "reviewCreatedAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    ReviewEntity toEntity(ReviewCreateDto dto);
}
