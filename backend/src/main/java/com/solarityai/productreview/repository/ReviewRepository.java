package com.solarityai.productreview.repository;

import com.solarityai.backendfw.foundation.repository.BaseRepository;
import com.solarityai.productreview.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface ReviewRepository extends BaseRepository<ReviewEntity, UUID>,
        JpaSpecificationExecutor<ReviewEntity> {

    List<ReviewEntity> findByProductId(UUID productId);

    Page<ReviewEntity> findByProductId(UUID productId, Pageable pageable);

    @Query("SELECT r FROM ReviewEntity r WHERE r.product.id = :productId " +
           "AND (:rating IS NULL OR r.rating = :rating)")
    Page<ReviewEntity> findByProductIdAndRating(
            @Param("productId") UUID productId,
            @Param("rating") Integer rating,
            Pageable pageable);

    @Query("SELECT r.rating as rating, COUNT(r) as count FROM ReviewEntity r " +
           "WHERE r.product.id = :productId GROUP BY r.rating")
    List<Map<String, Object>> findRatingCountsByProductId(@Param("productId") UUID productId);
}
