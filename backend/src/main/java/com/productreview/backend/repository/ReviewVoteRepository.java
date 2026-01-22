package com.productreview.backend.repository;

import com.productreview.backend.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

    Optional<ReviewVote> findByUserIdAndReviewId(String userId, Long reviewId);

    List<ReviewVote> findByUserId(String userId);
}