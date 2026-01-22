package com.productreview.backend.repository;

import com.productreview.backend.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserId(String userId);

    Optional<WishlistItem> findByUserIdAndProductId(String userId, Long productId);

    void deleteByUserIdAndProductId(String userId, Long productId);
}