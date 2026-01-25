package com.solarityai.productreview.repository;

import com.solarityai.backendfw.foundation.repository.BaseRepository;
import com.solarityai.productreview.entity.WishlistItemEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistItemRepository extends BaseRepository<WishlistItemEntity, UUID> {

    List<WishlistItemEntity> findByUserId(String userId);

    Optional<WishlistItemEntity> findByUserIdAndProductId(String userId, UUID productId);

    void deleteByUserIdAndProductId(String userId, UUID productId);
}
