package com.example.productreview.repository;

import com.example.productreview.model.UserMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMappingRepository extends JpaRepository<UserMapping, Long> {

    Optional<UserMapping> findByClerkUserId(String clerkUserId);
}
