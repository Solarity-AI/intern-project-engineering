package com.example.productreview.service;

import com.example.productreview.model.UserMapping;

import java.util.Optional;

public interface UserMappingService {

    UserMapping getOrCreateByClerkUserId(String clerkUserId);

    Optional<UserMapping> findByClerkUserId(String clerkUserId);
}
