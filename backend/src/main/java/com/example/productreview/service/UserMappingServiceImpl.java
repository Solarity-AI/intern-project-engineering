package com.example.productreview.service;

import com.example.productreview.model.UserMapping;
import com.example.productreview.repository.UserMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserMappingServiceImpl implements UserMappingService {

    private final UserMappingRepository userMappingRepository;

    @Override
    public UserMapping getOrCreateByClerkUserId(String clerkUserId) {
        String normalizedClerkUserId = normalizeClerkUserId(clerkUserId);

        return userMappingRepository.findByClerkUserId(normalizedClerkUserId)
                .orElseGet(() -> createMapping(normalizedClerkUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserMapping> findByClerkUserId(String clerkUserId) {
        if (clerkUserId == null || clerkUserId.isBlank()) {
            return Optional.empty();
        }

        return userMappingRepository.findByClerkUserId(clerkUserId.trim());
    }

    private UserMapping createMapping(String clerkUserId) {
        try {
            return userMappingRepository.saveAndFlush(new UserMapping(clerkUserId));
        } catch (DataIntegrityViolationException ex) {
            return userMappingRepository.findByClerkUserId(clerkUserId)
                    .orElseThrow(() -> ex);
        }
    }

    private String normalizeClerkUserId(String clerkUserId) {
        if (clerkUserId == null || clerkUserId.isBlank()) {
            throw new IllegalArgumentException("Clerk user ID must not be blank");
        }

        return clerkUserId.trim();
    }
}
