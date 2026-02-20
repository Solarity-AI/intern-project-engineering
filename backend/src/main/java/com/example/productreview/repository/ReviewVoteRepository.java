package com.example.productreview.repository;

import com.example.productreview.model.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    Optional<ReviewVote> findByUserIdAndReview_Id(String userId, Long reviewId);
    List<ReviewVote> findByUserId(String userId);

    @Query("SELECT v.review.id FROM ReviewVote v WHERE v.userId = :userId")
    List<Long> findReviewIdsByUserId(@Param("userId") String userId);
}
