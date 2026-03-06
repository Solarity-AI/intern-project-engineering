package com.example.productreview.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_mappings", indexes = {
    @Index(name = "idx_user_mappings_clerk_user", columnList = "clerk_user_id")
})
public class UserMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_user_id")
    private Long internalUserId;

    @Column(name = "clerk_user_id", nullable = false, unique = true, updatable = false)
    private String clerkUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserMapping() {
    }

    public UserMapping(String clerkUserId) {
        this.clerkUserId = clerkUserId;
    }

    public Long getInternalUserId() {
        return internalUserId;
    }

    public void setInternalUserId(Long internalUserId) {
        this.internalUserId = internalUserId;
    }

    public String getClerkUserId() {
        return clerkUserId;
    }

    public void setClerkUserId(String clerkUserId) {
        this.clerkUserId = clerkUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
