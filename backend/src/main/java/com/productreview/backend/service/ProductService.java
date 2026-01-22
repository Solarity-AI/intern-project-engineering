package com.productreview.backend.service;

import com.productreview.backend.dto.GlobalStatsDTO;
import com.productreview.backend.dto.ProductDTO;
import com.productreview.backend.dto.ReviewDTO;
import com.productreview.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<ProductDTO> getAllProducts(String category, String search, Pageable pageable);

    ProductDTO getProductDTOById(Long id);

    Product getProductById(Long id);

    List<ReviewDTO> getReviewsByProductId(Long productId);

    Page<ReviewDTO> getReviewsByProductId(Long productId, Integer rating, Pageable pageable);

    ReviewDTO addReview(Long productId, ReviewDTO reviewDTO);

    ReviewDTO markReviewAsHelpful(Long reviewId, String userId);

    List<Long> getUserVotedReviewIds(String userId);

    String chatAboutProduct(Long productId, String question);

    GlobalStatsDTO getGlobalStats(String category, String search);
}
