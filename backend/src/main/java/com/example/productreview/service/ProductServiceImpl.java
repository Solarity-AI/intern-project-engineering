package com.example.productreview.service;

import com.example.productreview.dto.ProductDTO;
import com.example.productreview.dto.ReviewDTO;
import com.example.productreview.exception.ResourceNotFoundException;
import com.example.productreview.model.Product;
import com.example.productreview.model.Review;
import com.example.productreview.model.ReviewVote;
import com.example.productreview.repository.ProductRepository;
import com.example.productreview.repository.ReviewRepository;
import com.example.productreview.repository.ReviewVoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final AISummaryService aiSummaryService;

    public ProductServiceImpl(ProductRepository productRepository, 
                              ReviewRepository reviewRepository, 
                              ReviewVoteRepository reviewVoteRepository,
                              AISummaryService aiSummaryService) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.reviewVoteRepository = reviewVoteRepository;
        this.aiSummaryService = aiSummaryService;
    }

    @Override
    public Page<ProductDTO> getAllProducts(String category, String search, Pageable pageable) {
        boolean hasCategory = category != null && !category.isEmpty() && !category.equalsIgnoreCase("All");
        boolean hasSearch = search != null && !search.trim().isEmpty();
        
        log.info("Service getAllProducts: hasCategory={}, hasSearch={}, search='{}'", hasCategory, hasSearch, search);

        Page<Product> products;

        if (hasCategory && hasSearch) {
            log.info("Searching by Category AND Name");
            products = productRepository.findByCategoryAndNameContainingIgnoreCase(category, search, pageable);
        } else if (hasCategory) {
            log.info("Searching by Category");
            products = productRepository.findByCategory(category, pageable);
        } else if (hasSearch) {
            log.info("Searching by Name");
            products = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            log.info("Returning ALL products");
            products = productRepository.findAll(pageable);
        }
        
        // ✨ Log categories for debugging
        products.getContent().forEach(p -> 
            log.info("Product: {}, Categories: {}", p.getName(), p.getCategories())
        );
        
        return products.map(this::convertToProductDTO);
    }

    @Override
    public ProductDTO getProductDTOById(Long id) {
        Product product = getProductById(id);
        ProductDTO productDTO = convertToProductDTO(product);
        
        Map<Integer, Long> ratingBreakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingBreakdown.put(i, 0L);
        }
        
        List<Object[]> counts = reviewRepository.findRatingCountsByProductId(id);
        for (Object[] result : counts) {
            Integer rating = (Integer) result[0];
            Long count = (Long) result[1];
            ratingBreakdown.put(rating, count);
        }
        
        productDTO.setRatingBreakdown(ratingBreakdown);
        
        try {
            List<Review> reviews = reviewRepository.findByProductId(id);
            if (!reviews.isEmpty()) {
                String aiSummary = aiSummaryService.generateReviewSummary(
                        id, 
                        product.getName(), 
                        reviews
                );
                productDTO.setAiSummary(aiSummary);
            }
        } catch (Exception e) {
            log.error("Error generating AI summary for product {}: {}", id, e.getMessage());
        }
        
        return productDTO;
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    public List<ReviewDTO> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewDTO> getReviewsByProductId(Long productId, Integer rating, Pageable pageable) {
        if (rating != null) {
            return reviewRepository.findByProductIdAndRating(productId, rating, pageable)
                    .map(this::convertToReviewDTO);
        }
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::convertToReviewDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "aiSummaries", key = "#productId")
    public ReviewDTO addReview(Long productId, ReviewDTO reviewDTO) {
        Product product = getProductById(productId);

        Review review = new Review();
        review.setReviewerName(reviewDTO.getReviewerName());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        review.setHelpfulCount(0);
        review.setProduct(product);

        Review savedReview = reviewRepository.save(review);
        updateProductStats(product);

        return convertToReviewDTO(savedReview);
    }

    @Override
    @Transactional
    public ReviewDTO markReviewAsHelpful(Long reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        if (review.getHelpfulCount() == null) {
            review.setHelpfulCount(0);
        }

        if (userId != null) {
            Optional<ReviewVote> existingVote = reviewVoteRepository.findByUserIdAndReviewId(userId, reviewId);
            
            if (existingVote.isPresent()) {
                reviewVoteRepository.delete(existingVote.get());
                if (review.getHelpfulCount() > 0) {
                    review.setHelpfulCount(review.getHelpfulCount() - 1);
                }
            } else {
                reviewVoteRepository.save(new ReviewVote(userId, reviewId));
                review.setHelpfulCount(review.getHelpfulCount() + 1);
            }
        } else {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        }
        
        Review savedReview = reviewRepository.save(review);
        return convertToReviewDTO(savedReview);
    }
    
    @Override
    public List<Long> getUserVotedReviewIds(String userId) {
        return reviewVoteRepository.findByUserId(userId).stream()
                .map(ReviewVote::getReviewId)
                .collect(Collectors.toList());
    }
    
    @Override
    public String chatAboutProduct(Long productId, String question) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return aiSummaryService.chatWithReviews(productId, question, reviews);
    }
    
    // ✨ NEW: Get global statistics from database (supports filtering)
    @Override
    public Map<String, Object> getGlobalStats(String category, String search) {
        boolean hasCategory = category != null && !category.isEmpty() && !category.equalsIgnoreCase("All");
        boolean hasSearch = search != null && !search.trim().isEmpty();
        
        List<Product> products;
        
        // Get filtered products
        if (hasCategory && hasSearch) {
            products = productRepository.findAll().stream()
                    .filter(p -> p.getCategories() != null && p.getCategories().contains(category))
                    .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (hasCategory) {
            products = productRepository.findAll().stream()
                    .filter(p -> p.getCategories() != null && p.getCategories().contains(category))
                    .collect(Collectors.toList());
        } else if (hasSearch) {
            products = productRepository.findAll().stream()
                    .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            products = productRepository.findAll();
        }
        
        long totalProducts = products.size();
        
        // Sum of all review counts from filtered products
        long totalReviews = products.stream()
                .mapToLong(p -> p.getReviewCount() != null ? p.getReviewCount() : 0)
                .sum();
        
        // Calculate average rating across filtered products
        Double avgRating = products.stream()
                .filter(p -> p.getAverageRating() != null && p.getAverageRating() > 0)
                .mapToDouble(Product::getAverageRating)
                .average()
                .orElse(0.0);
        
        // Round to 1 decimal place
        avgRating = Math.round(avgRating * 10.0) / 10.0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("totalReviews", totalReviews);
        stats.put("averageRating", avgRating);
        
        log.info("Filtered stats (category={}, search={}): products={}, reviews={}, avgRating={}", 
                category, search, totalProducts, totalReviews, avgRating);
        
        return stats;
    }

    private void updateProductStats(Product product) {
        List<Review> reviews = reviewRepository.findByProductId(product.getId());
        int count = reviews.size();
        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setReviewCount(count);
        product.setAverageRating(Math.round(average * 10.0) / 10.0);
        productRepository.save(product);
    }

    private ReviewDTO convertToReviewDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getReviewerName(),
                review.getComment(),
                review.getRating(),
                review.getHelpfulCount() != null ? review.getHelpfulCount() : 0,
                review.getCreatedAt(),
                review.getProduct().getId()
        );
    }

    private ProductDTO convertToProductDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategories(),
                product.getPrice(),
                product.getImageUrl(),
                product.getAverageRating(),
                product.getReviewCount(),
                null,
                null
        );
    }
}