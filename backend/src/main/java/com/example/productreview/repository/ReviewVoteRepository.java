package com.example.productreview.repository;

import com.example.productreview.model.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    Optional<ReviewVote> findByUserIdAndReviewId(String userId, Long reviewId);
    List<ReviewVote> findByUserId(String userId);
}
