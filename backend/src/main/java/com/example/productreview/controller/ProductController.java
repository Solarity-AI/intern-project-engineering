package com.example.productreview.controller;

import com.example.productreview.dto.ProductDTO;
import com.example.productreview.dto.ReviewDTO;
import com.example.productreview.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ✨ NEW: Global stats endpoint for hero section (supports filtering)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats(
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
        
        // ✨ FIX: Case-insensitive sorting for name field
        // This ensures "iPhone" sorts correctly with other "I" names
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
        
        // ✨ FIX: Case-insensitive sorting for reviewerName field as well
        Sort.Order order = new Sort.Order(direction, sortField);
        if (sortField.equalsIgnoreCase("reviewerName")) {
            order = order.ignoreCase();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        
        if (rating != null) {
            return ResponseEntity.ok(productService.getReviewsByProductId(id, rating, pageable));
        }
        
        return ResponseEntity.ok(productService.getReviewsByProductId(id, null, pageable));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewDTO> addReview(@PathVariable Long id, @Valid @RequestBody ReviewDTO reviewDTO) {
        return ResponseEntity.ok(productService.addReview(id, reviewDTO));
    }

    @PutMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<ReviewDTO> markReviewAsHelpful(
            @PathVariable Long reviewId,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        return ResponseEntity.ok(productService.markReviewAsHelpful(reviewId, userId));
    }
    
    @GetMapping("/reviews/voted")
    public ResponseEntity<List<Long>> getUserVotedReviews(@RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(productService.getUserVotedReviewIds(userId));
    }

    @PostMapping("/{id}/chat")
    public ResponseEntity<Map<String, String>> chatAboutProduct(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }
        
        String answer = productService.chatAboutProduct(id, question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}