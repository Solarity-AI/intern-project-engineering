package com.solarityai.productreview.service;

import com.solarityai.backendfw.query.model.PageRequestDto;
import com.solarityai.backendfw.query.response.PageResponse;
import com.solarityai.productreview.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductDto getProductById(UUID id);

    Page<ProductDto> getProducts(String category, String search, PageRequestDto pageRequest);

    ProductStatsDto getProductStats(String category, String search);

    PageResponse<ReviewDto> getProductReviews(UUID productId, Integer rating, PageRequestDto pageRequest);

    ReviewDto addReview(UUID productId, ReviewCreateDto reviewDto);

    ReviewDto toggleHelpfulVote(UUID reviewId, String userId);

    List<UUID> getVotedReviewIds(String userId);

    ChatResponseDto chatAboutProduct(UUID productId, ChatRequestDto request);
}
