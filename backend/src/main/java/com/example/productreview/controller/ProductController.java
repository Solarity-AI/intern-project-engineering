package com.example.productreview.controller;

import com.example.productreview.dto.ProductDTO;
import com.example.productreview.dto.ReviewDTO;
import com.example.productreview.exception.ValidationException;
import com.example.productreview.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.Set;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_REVIEW_SORT_FIELDS = Set.of(
            "createdAt", "rating", "reviewerName", "helpfulCount");
    private static final Set<String> ALLOWED_PRODUCT_SORT_FIELDS = Set.of(
            "name", "price", "averageRating", "reviewCount");

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ValidationException("Page index must not be negative");
        }
        if (size < 1) {
            throw new ValidationException("Page size must be at least 1");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new ValidationException("Page size must not exceed " + MAX_PAGE_SIZE);
        }
    }

    private void validateRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
    }

    private void validateSortField(String sortField, Set<String> allowedFields) {
        if (!allowedFields.contains(sortField)) {
            throw new ValidationException("Invalid sort field: " + sortField + ". Allowed: " + allowedFields);
        }
    }

    @Operation(
            tags = "Products",
            summary = "Get global statistics",
            description = "Returns aggregate statistics (total products, total reviews, average rating) with optional category and search filtering.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned successfully")
    })
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats(
            @Parameter(description = "Filter by category name", example = "Electronics")
            @RequestParam(required = false) String category,
            @Parameter(description = "Search products by name (case-insensitive)", example = "iPhone")
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getGlobalStats(category, search));
    }

    @Operation(
            tags = "Products",
            summary = "List all products",
            description = "Returns a paginated list of products with optional category filtering, search, and sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of products returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @Parameter(description = "Filter by category name", example = "Electronics")
            @RequestParam(required = false) String category,
            @Parameter(description = "Search products by name (case-insensitive)", example = "iPhone")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page index (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g. name,asc or price,desc)", example = "name,asc")
            @RequestParam(defaultValue = "name,asc") String sort) {

        validatePagination(page, size);

        log.info("getAllProducts called with category: {}, search: {}, sort: {}", category, search, sort);

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        validateSortField(sortField, ALLOWED_PRODUCT_SORT_FIELDS);
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

    @Operation(
            tags = "Products",
            summary = "Get product by ID",
            description = "Returns a single product with its details and AI-generated review summary.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product returned successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDTOById(id));
    }

    @Operation(
            tags = "Reviews",
            summary = "List reviews for a product",
            description = "Returns a paginated list of reviews for a given product, with optional rating filter and sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of reviews returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<ReviewDTO>> getReviews(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Filter by star rating (1-5)", example = "5")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "Page index (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        validatePagination(page, size);
        validateRating(rating);

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        validateSortField(sortField, ALLOWED_REVIEW_SORT_FIELDS);

        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort.Order order = new Sort.Order(direction, sortField);
        if (sortField.equalsIgnoreCase("reviewerName")) {
            order = order.ignoreCase();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        return ResponseEntity.ok(productService.getReviewsByProductId(id, rating, pageable));
    }

    @Operation(
            tags = "Reviews",
            summary = "Submit a review",
            description = "Creates a new review for the specified product and updates the product's rating statistics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid review data"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewDTO> addReview(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        return ResponseEntity.ok(productService.addReview(id, reviewDTO));
    }

    @Operation(
            tags = "Reviews",
            summary = "Toggle helpful vote on a review",
            description = "Toggles the current user's helpful vote on a review. Voting again removes the vote.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vote toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PutMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<ReviewDTO> markReviewAsHelpful(
            @Parameter(description = "Review ID", example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "User ID for vote tracking", required = true)
            @RequestHeader(value = "X-User-ID", required = true) String userId) {
        return ResponseEntity.ok(productService.markReviewAsHelpful(reviewId, userId));
    }

    @Operation(
            tags = "Reviews",
            summary = "Get user's voted review IDs",
            description = "Returns a list of review IDs that the current user has marked as helpful.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of voted review IDs returned")
    })
    @GetMapping("/reviews/voted")
    public ResponseEntity<List<Long>> getUserVotedReviews(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(productService.getUserVotedReviewIds(userId));
    }

    @Operation(
            tags = "AI",
            summary = "Chat about a product",
            description = "Sends a question to the AI assistant about a product's reviews and returns an analysis response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI response returned successfully"),
            @ApiResponse(responseCode = "400", description = "Question is required"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/{id}/chat")
    public ResponseEntity<Map<String, String>> chatAboutProduct(
            @Parameter(description = "Product ID", example = "1")
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
