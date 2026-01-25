package com.solarityai.productreview.service.impl;

import com.solarityai.backendfw.exception.NotFoundException;
import com.solarityai.backendfw.query.model.PageRequestDto;
import com.solarityai.backendfw.query.response.PageMetadata;
import com.solarityai.backendfw.query.response.PageResponse;
import com.solarityai.productreview.dto.*;
import com.solarityai.productreview.entity.ProductEntity;
import com.solarityai.productreview.entity.ReviewEntity;
import com.solarityai.productreview.entity.ReviewVoteEntity;
import com.solarityai.productreview.mapper.ProductMapper;
import com.solarityai.productreview.mapper.ReviewMapper;
import com.solarityai.productreview.repository.ProductRepository;
import com.solarityai.productreview.repository.ReviewRepository;
import com.solarityai.productreview.repository.ReviewVoteRepository;
import com.solarityai.productreview.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ProductMapper productMapper;
    private final ReviewMapper reviewMapper;
    private final AIService aiService;

    @Override
    public ProductDto getProductById(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        ProductDto dto = productMapper.toDto(product);

        // Add rating breakdown
        dto.setRatingBreakdown(getRatingBreakdown(id));

        // Add AI summary
        dto.setAiSummary(aiService.generateReviewSummary(product));

        return dto;
    }

    @Override
    public Page<ProductDto> getProducts(String category, String search, PageRequestDto pageRequest) {
        Pageable pageable = createPageable(pageRequest);

        Page<ProductEntity> productPage;

        if (category != null && !category.isEmpty() && search != null && !search.isEmpty()) {
            productPage = productRepository.findByCategoryAndNameContainingIgnoreCase(category, search, pageable);
        } else if (category != null && !category.isEmpty()) {
            productPage = productRepository.findByCategory(category, pageable);
        } else if (search != null && !search.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(productMapper::toDto);
    }

    @Override
    public ProductStatsDto getProductStats(String category, String search) {
        if (category != null && !category.isEmpty() && search != null && !search.isEmpty()) {
            return productRepository.getCategoryAndSearchStats(category, search);
        } else if (category != null && !category.isEmpty()) {
            return productRepository.getCategoryStats(category);
        } else if (search != null && !search.isEmpty()) {
            return productRepository.getSearchStats(search);
        } else {
            return productRepository.getGlobalStats();
        }
    }

    @Override
    public PageResponse<ReviewDto> getProductReviews(UUID productId, Integer rating, PageRequestDto pageRequest) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        Pageable pageable = createPageable(pageRequest);
        Page<ReviewEntity> reviewPage = reviewRepository.findByProductIdAndRating(productId, rating, pageable);

        List<ReviewDto> reviews = reviewPage.getContent().stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());

        return PageResponse.<ReviewDto>builder()
                .data(reviews)
                .page(PageMetadata.builder()
                        .number(reviewPage.getNumber())
                        .size(reviewPage.getSize())
                        .totalElements(reviewPage.getTotalElements())
                        .totalPages(reviewPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public ReviewDto addReview(UUID productId, ReviewCreateDto reviewDto) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        ReviewEntity review = reviewMapper.toEntity(reviewDto);
        review.setProduct(product);
        review = reviewRepository.save(review);

        // Update product statistics
        updateProductStatistics(product);

        return reviewMapper.toDto(review);
    }

    @Override
    public ReviewDto toggleHelpfulVote(UUID reviewId, String userId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));

        Optional<ReviewVoteEntity> existingVote = reviewVoteRepository.findByUserIdAndReviewId(userId, reviewId);

        if (existingVote.isPresent()) {
            // Remove vote
            reviewVoteRepository.delete(existingVote.get());
            review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
        } else {
            // Add vote
            ReviewVoteEntity vote = new ReviewVoteEntity();
            vote.setUserId(userId);
            vote.setReviewId(reviewId);
            reviewVoteRepository.save(vote);
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        }

        review = reviewRepository.save(review);
        return reviewMapper.toDto(review);
    }

    @Override
    public List<UUID> getVotedReviewIds(String userId) {
        return reviewVoteRepository.findByUserId(userId).stream()
                .map(ReviewVoteEntity::getReviewId)
                .collect(Collectors.toList());
    }

    @Override
    public ChatResponseDto chatAboutProduct(UUID productId, ChatRequestDto request) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        List<ReviewEntity> reviews = reviewRepository.findByProductId(productId);
        String answer = aiService.chatAboutProduct(product, reviews, request.getQuestion());

        return new ChatResponseDto(answer);
    }

    private Map<Integer, Long> getRatingBreakdown(UUID productId) {
        List<Map<String, Object>> results = reviewRepository.findRatingCountsByProductId(productId);
        Map<Integer, Long> breakdown = new HashMap<>();

        // Initialize all ratings to 0
        for (int i = 1; i <= 5; i++) {
            breakdown.put(i, 0L);
        }

        // Fill in actual counts
        for (Map<String, Object> result : results) {
            Integer rating = (Integer) result.get("rating");
            Long count = (Long) result.get("count");
            breakdown.put(rating, count);
        }

        return breakdown;
    }

    private void updateProductStatistics(ProductEntity product) {
        List<ReviewEntity> reviews = reviewRepository.findByProductId(product.getId());

        int reviewCount = reviews.size();
        double averageRating = reviews.stream()
                .mapToInt(ReviewEntity::getRating)
                .average()
                .orElse(0.0);

        product.setReviewCount(reviewCount);
        product.setAverageRating(averageRating);
        productRepository.save(product);
    }

    private Pageable createPageable(PageRequestDto pageRequest) {
        if (pageRequest == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        }

        int page = pageRequest.getPage();
        int size = pageRequest.getSize() > 0 ? pageRequest.getSize() : 10;

        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
    }
}
