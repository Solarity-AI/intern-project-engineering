package com.solarityai.productreview.controller;

import com.solarityai.backendfw.foundation.controller.BaseController;
import com.solarityai.backendfw.query.model.PageRequestDto;
import com.solarityai.backendfw.query.response.PageResponse;
import com.solarityai.productreview.dto.*;
import com.solarityai.productreview.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController extends BaseController {

    private final ProductService productService;

    @GetMapping("/stats")
    public ResponseEntity<ProductStatsDto> getStats(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        ProductStatsDto stats = productService.getProductStats(category, search);
        return ResponseEntity.ok(stats);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(page)
                .size(size)
                .build();

        Page<ProductDto> products = productService.getProducts(category, search, pageRequest);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable UUID id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<PageResponse<ReviewDto>> getProductReviews(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "reviewCreatedAt,desc") String sort) {

        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(page)
                .size(size)
                .build();

        PageResponse<ReviewDto> reviews = productService.getProductReviews(id, rating, pageRequest);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewDto> addReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewCreateDto reviewDto) {
        ReviewDto review = productService.addReview(id, reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<ReviewDto> toggleHelpfulVote(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "X-User-ID", required = false, defaultValue = "anonymous") String userId) {
        ReviewDto review = productService.toggleHelpfulVote(reviewId, userId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/reviews/voted")
    public ResponseEntity<List<UUID>> getVotedReviews(
            @RequestHeader(value = "X-User-ID", required = true) String userId) {
        List<UUID> reviewIds = productService.getVotedReviewIds(userId);
        return ResponseEntity.ok(reviewIds);
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<ChatResponseDto> chatAboutProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ChatRequestDto request) {
        ChatResponseDto response = productService.chatAboutProduct(id, request);
        return ResponseEntity.ok(response);
    }
}
