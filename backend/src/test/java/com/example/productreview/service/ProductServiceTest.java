package com.example.productreview.service;

import com.example.productreview.dto.ProductDTO;
import com.example.productreview.dto.ReviewDTO;
import com.example.productreview.model.Product;
import com.example.productreview.model.Review;
import com.example.productreview.repository.ProductRepository;
import com.example.productreview.repository.ReviewRepository;
import com.example.productreview.repository.ReviewVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.example.productreview.model.ReviewVote;
import com.example.productreview.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private ReviewVoteRepository reviewVoteRepository;
    
    @Mock
    private AISummaryService aiSummaryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        Set<String> categories = new HashSet<>(Arrays.asList("Category"));
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Description");
        product.setCategories(categories); // ✨ Updated
        product.setPrice(100.0);
        product.setImageUrl("http://example.com/image.jpg");
        product.setAverageRating(0.0);
        product.setReviewCount(0);

        productDTO = new ProductDTO(1L, "Test Product", "Description", categories, 100.0, "http://example.com/image.jpg", 0.0, 0, null, null);
    }

    @Test
    void getAllProducts_ShouldReturnPageOfDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getAllProducts(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(product.getName(), result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void getProductDTOById_ShouldReturnDTO() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findRatingCountsByProductId(1L)).thenReturn(new ArrayList<>());
        when(reviewRepository.findByProductId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        ProductDTO result = productService.getProductDTOById(1L);

        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
    }

    @Test
    void addReview_ShouldUpdateStatsAndReturnDTO() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("User");
        reviewDTO.setComment("Good product indeed");
        reviewDTO.setRating(5);

        Review review = new Review();
        review.setId(1L);
        review.setReviewerName("User");
        review.setRating(5);
        review.setProduct(product);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDTO result = productService.addReview(1L, reviewDTO);

        assertNotNull(result);
        verify(productRepository, times(1)).updateProductStatsAtomic(1L);
    }

    // --- Error Case Tests (U24) ---

    @Test
    void getProductById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void getProductDTOById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductDTOById(999L));
    }

    @Test
    void addReview_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("User");
        reviewDTO.setComment("Good product indeed");
        reviewDTO.setRating(5);
        assertThrows(ResourceNotFoundException.class, () -> productService.addReview(999L, reviewDTO));
    }

    // --- markReviewAsHelpful Tests ---

    @Test
    void markReviewAsHelpful_ShouldIncrementOnFirstVote() {
        Review review = new Review();
        review.setId(1L);
        review.setHelpfulCount(0);
        review.setProduct(product);

        when(reviewRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(review));
        when(reviewVoteRepository.findByUserIdAndReview_Id("user1", 1L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        productService.markReviewAsHelpful(1L, "user1");

        assertEquals(1, review.getHelpfulCount());
        verify(reviewVoteRepository).save(any(ReviewVote.class));
    }

    @Test
    void markReviewAsHelpful_ShouldDecrementOnSecondVote() {
        Review review = new Review();
        review.setId(1L);
        review.setHelpfulCount(1);
        review.setProduct(product);
        ReviewVote existingVote = new ReviewVote("user1", review);

        when(reviewRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(review));
        when(reviewVoteRepository.findByUserIdAndReview_Id("user1", 1L)).thenReturn(Optional.of(existingVote));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        productService.markReviewAsHelpful(1L, "user1");

        assertEquals(0, review.getHelpfulCount());
        verify(reviewVoteRepository).delete(existingVote);
    }

    @Test
    void markReviewAsHelpful_WhenReviewNotFound_ShouldThrowException() {
        when(reviewRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.markReviewAsHelpful(999L, "user1"));
    }

    @Test
    void markReviewAsHelpful_WithNullUserId_ShouldIncrementWithoutVoteRecord() {
        Review review = new Review();
        review.setId(1L);
        review.setHelpfulCount(0);
        review.setProduct(product);

        when(reviewRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        productService.markReviewAsHelpful(1L, null);

        assertEquals(1, review.getHelpfulCount());
        verifyNoInteractions(reviewVoteRepository);
    }

    // --- getUserVotedReviewIds Tests ---

    @Test
    void getUserVotedReviewIds_ShouldReturnVotedIds() {
        when(reviewVoteRepository.findReviewIdsByUserId("user1")).thenReturn(Arrays.asList(10L, 20L));

        List<Long> result = productService.getUserVotedReviewIds("user1");

        assertEquals(2, result.size());
        assertTrue(result.contains(10L));
        assertTrue(result.contains(20L));
    }

    @Test
    void getUserVotedReviewIds_WhenNoVotes_ShouldReturnEmpty() {
        when(reviewVoteRepository.findReviewIdsByUserId("user1")).thenReturn(new ArrayList<>());

        List<Long> result = productService.getUserVotedReviewIds("user1");

        assertTrue(result.isEmpty());
    }

    // --- getGlobalStats Tests ---

    @Test
    void getGlobalStats_NoFilter_ShouldReturnAllStats() {
        // SUM(reviewCount)=8, AVG(averageRating)=3.5, COUNT(p)=2
        when(productRepository.getGlobalStats()).thenReturn(Collections.singletonList(new Object[]{8L, 3.5, 2L}));

        Map<String, Object> stats = productService.getGlobalStats(null, null);

        assertEquals(2L, stats.get("totalProducts"));
        assertEquals(8L, stats.get("totalReviews"));
        assertEquals(3.5, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WithCategory_ShouldFilterByCategory() {
        when(productRepository.getCategoryStats("Electronics")).thenReturn(Collections.singletonList(new Object[]{5L, 4.0, 1L}));

        Map<String, Object> stats = productService.getGlobalStats("Electronics", null);

        assertEquals(1L, stats.get("totalProducts"));
        assertEquals(5L, stats.get("totalReviews"));
        assertEquals(4.0, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WithSearch_ShouldFilterByName() {
        when(productRepository.getSearchStats("Samsung")).thenReturn(Collections.singletonList(new Object[]{5L, 4.0, 1L}));

        Map<String, Object> stats = productService.getGlobalStats(null, "Samsung");

        assertEquals(1L, stats.get("totalProducts"));
        assertEquals(5L, stats.get("totalReviews"));
        assertEquals(4.0, stats.get("averageRating"));
    }

    // --- getAllProducts Filter Tests ---

    @Test
    void getAllProducts_WithCategory_ShouldFilterByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findByCategory("Electronics", pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getAllProducts("Electronics", null, pageable);

        assertNotNull(result);
        verify(productRepository).findByCategory("Electronics", pageable);
    }

    @Test
    void getAllProducts_WithSearch_ShouldFilterByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findByNameContainingIgnoreCase("Test", pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getAllProducts(null, "Test", pageable);

        assertNotNull(result);
        verify(productRepository).findByNameContainingIgnoreCase("Test", pageable);
    }

    @Test
    void getAllProducts_WithCategoryAndSearch_ShouldFilterByBoth() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findByCategoryAndNameContainingIgnoreCase("Electronics", "Test", pageable))
                .thenReturn(productPage);

        Page<ProductDTO> result = productService.getAllProducts("Electronics", "Test", pageable);

        assertNotNull(result);
        verify(productRepository).findByCategoryAndNameContainingIgnoreCase("Electronics", "Test", pageable);
    }

    // --- Additional Tests for Criteria Compliance ---

    @Test
    void addReview_WithMinRating_ShouldUpdateStats() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("User");
        reviewDTO.setComment("Not great product at all");
        reviewDTO.setRating(1);

        Review review = new Review();
        review.setId(2L);
        review.setReviewerName("User");
        review.setRating(1);
        review.setProduct(product);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDTO result = productService.addReview(1L, reviewDTO);

        assertNotNull(result);
        verify(productRepository, times(1)).updateProductStatsAtomic(1L);
    }

    @Test
    void addReview_WithMaxRating_ShouldUpdateStats() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("FanUser");
        reviewDTO.setComment("Absolutely perfect product");
        reviewDTO.setRating(5);

        Review review = new Review();
        review.setId(3L);
        review.setReviewerName("FanUser");
        review.setRating(5);
        review.setProduct(product);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDTO result = productService.addReview(1L, reviewDTO);

        assertNotNull(result);
        verify(productRepository, times(1)).updateProductStatsAtomic(1L);
    }

    @Test
    void getReviewsByProductId_Paged_ShouldReturnPagedReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Review review = new Review();
        review.setId(1L);
        review.setReviewerName("User");
        review.setComment("Nice");
        review.setRating(4);
        review.setHelpfulCount(0);
        review.setProduct(product);

        Page<Review> reviewPage = new PageImpl<>(Arrays.asList(review));
        when(reviewRepository.findByProductId(1L, pageable)).thenReturn(reviewPage);

        Page<ReviewDTO> result = productService.getReviewsByProductId(1L, null, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getReviewsByProductId_WithRatingFilter_ShouldFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Review review = new Review();
        review.setId(1L);
        review.setReviewerName("User");
        review.setComment("Nice");
        review.setRating(5);
        review.setHelpfulCount(0);
        review.setProduct(product);

        Page<Review> reviewPage = new PageImpl<>(Arrays.asList(review));
        when(reviewRepository.findByProductIdAndRating(1L, 5, pageable)).thenReturn(reviewPage);

        Page<ReviewDTO> result = productService.getReviewsByProductId(1L, 5, pageable);

        assertEquals(1, result.getContent().size());
        verify(reviewRepository).findByProductIdAndRating(1L, 5, pageable);
    }

    @Test
    void chatAboutProduct_ShouldDelegateToAIService() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));
        when(aiSummaryService.chatWithReviews(eq(1L), eq("How is quality?"), any()))
                .thenReturn("AI response");

        String result = productService.chatAboutProduct(1L, "How is quality?");

        assertEquals("AI response", result);
        verify(aiSummaryService).chatWithReviews(eq(1L), eq("How is quality?"), any());
    }

    @Test
    void chatAboutProduct_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.chatAboutProduct(999L, "How is quality?"));
    }

    @Test
    void getProductDTOById_ShouldIncludeRatingBreakdown() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        List<Object[]> ratingCounts = Arrays.asList(
                new Object[]{5, 3L},
                new Object[]{4, 2L}
        );
        when(reviewRepository.findRatingCountsByProductId(1L)).thenReturn(ratingCounts);
        when(reviewRepository.findByProductId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        ProductDTO result = productService.getProductDTOById(1L);

        assertNotNull(result.getRatingBreakdown());
        assertEquals(3L, result.getRatingBreakdown().get(5));
        assertEquals(2L, result.getRatingBreakdown().get(4));
        assertEquals(0L, result.getRatingBreakdown().get(1));
    }

    @Test
    void getGlobalStats_WithCategoryAndSearch_ShouldFilterByBoth() {
        when(productRepository.getCategoryAndSearchStats("Electronics", "Samsung"))
                .thenReturn(Collections.singletonList(new Object[]{5L, 4.0, 1L}));

        Map<String, Object> stats = productService.getGlobalStats("Electronics", "Samsung");

        assertEquals(1L, stats.get("totalProducts"));
        assertEquals(5L, stats.get("totalReviews"));
        assertEquals(4.0, stats.get("averageRating"));
    }

    @Test
    void getAllProducts_WithCategoryAll_ShouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getAllProducts("All", null, pageable);

        verify(productRepository).findAll(pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllProducts_WithEmptySearch_ShouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getAllProducts(null, "  ", pageable);

        verify(productRepository).findAll(pageable);
    }

    @Test
    void markReviewAsHelpful_ShouldNotGoBelowZero() {
        Review review = new Review();
        review.setId(1L);
        review.setHelpfulCount(0);
        review.setProduct(product);
        ReviewVote existingVote = new ReviewVote("user1", review);

        when(reviewRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(review));
        when(reviewVoteRepository.findByUserIdAndReview_Id("user1", 1L)).thenReturn(Optional.of(existingVote));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        productService.markReviewAsHelpful(1L, "user1");

        assertEquals(0, review.getHelpfulCount());
    }

    // --- Empty Database / Zero-State Tests (#105) ---

    @Test
    void getGlobalStats_WhenDatabaseEmpty_ShouldReturnZeroState() {
        when(productRepository.getGlobalStats()).thenReturn(Collections.emptyList());

        Map<String, Object> stats = productService.getGlobalStats(null, null);

        assertEquals(0L, stats.get("totalProducts"));
        assertEquals(0L, stats.get("totalReviews"));
        assertEquals(0.0, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WhenResultContainsNullAggregates_ShouldReturnZeroState() {
        when(productRepository.getGlobalStats())
                .thenReturn(Collections.singletonList(new Object[]{null, null, 0L}));

        Map<String, Object> stats = productService.getGlobalStats(null, null);

        assertEquals(0L, stats.get("totalProducts"));
        assertEquals(0L, stats.get("totalReviews"));
        assertEquals(0.0, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WithCategory_WhenEmpty_ShouldReturnZeroState() {
        when(productRepository.getCategoryStats("NonExistent")).thenReturn(Collections.emptyList());

        Map<String, Object> stats = productService.getGlobalStats("NonExistent", null);

        assertEquals(0L, stats.get("totalProducts"));
        assertEquals(0L, stats.get("totalReviews"));
        assertEquals(0.0, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WithSearch_WhenEmpty_ShouldReturnZeroState() {
        when(productRepository.getSearchStats("NoMatch")).thenReturn(Collections.emptyList());

        Map<String, Object> stats = productService.getGlobalStats(null, "NoMatch");

        assertEquals(0L, stats.get("totalProducts"));
        assertEquals(0L, stats.get("totalReviews"));
        assertEquals(0.0, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WithCategoryAndSearch_WhenEmpty_ShouldReturnZeroState() {
        when(productRepository.getCategoryAndSearchStats("NonExistent", "NoMatch"))
                .thenReturn(Collections.emptyList());

        Map<String, Object> stats = productService.getGlobalStats("NonExistent", "NoMatch");

        assertEquals(0L, stats.get("totalProducts"));
        assertEquals(0L, stats.get("totalReviews"));
        assertEquals(0.0, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WhenResultRowIsNull_ShouldReturnZeroState() {
        List<Object[]> nullRowList = new ArrayList<>();
        nullRowList.add(null);
        when(productRepository.getGlobalStats()).thenReturn(nullRowList);

        Map<String, Object> stats = productService.getGlobalStats(null, null);

        assertEquals(0L, stats.get("totalProducts"));
        assertEquals(0L, stats.get("totalReviews"));
        assertEquals(0.0, stats.get("averageRating"));
    }

    @Test
    void getGlobalStats_WhenRepositoryReturnsNull_ShouldReturnZeroState() {
        when(productRepository.getGlobalStats()).thenReturn(null);

        Map<String, Object> stats = productService.getGlobalStats(null, null);

        assertEquals(0L, stats.get("totalProducts"));
        assertEquals(0L, stats.get("totalReviews"));
        assertEquals(0.0, stats.get("averageRating"));
    }

    // --- updateProductStats edge cases (#105) ---

    @Test
    void addReview_WhenCalled_ShouldDelegateStatsToAtomicUpdate() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewerName("TestUser");
        reviewDTO.setComment("Good product indeed");
        reviewDTO.setRating(5);

        Review review = new Review();
        review.setId(1L);
        review.setReviewerName("TestUser");
        review.setRating(5);
        review.setProduct(product);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDTO result = productService.addReview(1L, reviewDTO);

        assertNotNull(result);
        verify(productRepository, times(1)).updateProductStatsAtomic(1L);
        verify(reviewRepository, never()).getReviewStats(anyLong());
    }

    @Test
    void markReviewAsHelpful_WithNullHelpfulCount_ShouldInitializeToZero() {
        Review review = new Review();
        review.setId(1L);
        review.setHelpfulCount(null);
        review.setProduct(product);

        when(reviewRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(review));
        when(reviewVoteRepository.findByUserIdAndReview_Id("user1", 1L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        productService.markReviewAsHelpful(1L, "user1");

        assertEquals(1, review.getHelpfulCount());
    }
}
