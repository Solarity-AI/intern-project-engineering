package com.example.productreview.repository;

import com.example.productreview.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserId(String userId);
    Optional<WishlistItem> findByUserIdAndProductId(String userId, Long productId);
    void deleteByUserIdAndProductId(String userId, Long productId);

    @Query("SELECT w.productId FROM WishlistItem w WHERE w.userId = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") String userId);
}
