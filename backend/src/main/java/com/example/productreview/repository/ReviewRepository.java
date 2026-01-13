package com.example.productreview.repository;

import com.example.productreview.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId")
    List<Review> findByProductId(@Param("productId") Long productId);
    
    // ✨ Added paged findByProductId
    Page<Review> findByProductId(@Param("productId") Long productId, Pageable pageable);
    
    // Updated query to handle optional rating filtering
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND (:rating IS NULL OR r.rating = :rating)")
    Page<Review> findByProductIdAndRating(@Param("productId") Long productId, @Param("rating") Integer rating, Pageable pageable);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> findRatingCountsByProductId(@Param("productId") Long productId);
}
