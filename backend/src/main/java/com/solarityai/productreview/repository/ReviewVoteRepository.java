package com.solarityai.productreview.repository;

import com.solarityai.backendfw.foundation.repository.BaseRepository;
import com.solarityai.productreview.entity.ReviewVoteEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewVoteRepository extends BaseRepository<ReviewVoteEntity, UUID> {

    Optional<ReviewVoteEntity> findByUserIdAndReviewId(String userId, UUID reviewId);

    List<ReviewVoteEntity> findByUserId(String userId);
}
