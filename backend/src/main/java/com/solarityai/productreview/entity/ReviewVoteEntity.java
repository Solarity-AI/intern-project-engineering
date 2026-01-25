package com.solarityai.productreview.entity;

import com.solarityai.backendfw.foundation.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "review_votes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"}))
@Getter
@Setter
public class ReviewVoteEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;
}
