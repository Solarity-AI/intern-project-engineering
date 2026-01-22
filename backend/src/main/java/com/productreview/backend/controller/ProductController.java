package com.productreview.backend.controller;

import com.productreview.backend.dto.*;
import com.productreview.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/stats")
    public ResponseEntity<GlobalStatsDTO> getGlobalStats(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getGlobalStats(category, search));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        log.info("getAllProducts called with category: {}, search: {}, sort: {}", category, search, sort);

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort.Order order = new Sort.Order(direction, sortField);
        if (sortField.equalsIgnoreCase("name")) {
            order = order.ignoreCase();
            log.info("Applying case-insensitive sorting for name field");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        return ResponseEntity.ok(productService.getAllProducts(category, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDTOById(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<ReviewDTO>> getReviews(
            @PathVariable Long id,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort.Order order = new Sort.Order(direction, sortField);
        if (sortField.equalsIgnoreCase("reviewerName")) {
            order = order.ignoreCase();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        return ResponseEntity.ok(productService.getReviewsByProductId(id, rating, pageable));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewDTO> addReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        return ResponseEntity.ok(productService.addReview(id, reviewDTO));
    }

    @PutMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<ReviewDTO> markReviewAsHelpful(
            @PathVariable Long reviewId,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        return ResponseEntity.ok(productService.markReviewAsHelpful(reviewId, userId));
    }

    @GetMapping("/reviews/voted")
    public ResponseEntity<List<Long>> getUserVotedReviews(
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(productService.getUserVotedReviewIds(userId));
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<ChatResponse> chatAboutProduct(
            @PathVariable Long id,
            @RequestBody ChatRequest request) {

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Question is required"));
        }

        String answer = productService.chatAboutProduct(id, request.getQuestion());
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
