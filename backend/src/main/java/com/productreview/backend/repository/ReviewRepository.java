package com.productreview.backend.repository;

import com.productreview.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findByProductIdAndRating(Long productId, Integer rating, Pageable pageable);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> findRatingCountsByProductId(@Param("productId") Long productId);
}