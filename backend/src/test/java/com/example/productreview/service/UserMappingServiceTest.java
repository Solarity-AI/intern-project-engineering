package com.example.productreview.service;

import com.example.productreview.model.UserMapping;
import com.example.productreview.repository.UserMappingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMappingServiceTest {

    @Mock
    private UserMappingRepository userMappingRepository;

    @InjectMocks
    private UserMappingServiceImpl userMappingService;

    @Test
    void getOrCreateByClerkUserId_WhenMappingExists_ShouldReturnExistingMapping() {
        UserMapping existingMapping = new UserMapping("clerk-user-1");
        existingMapping.setInternalUserId(42L);
        when(userMappingRepository.findByClerkUserId("clerk-user-1")).thenReturn(Optional.of(existingMapping));

        UserMapping resolvedMapping = userMappingService.getOrCreateByClerkUserId("clerk-user-1");

        assertSame(existingMapping, resolvedMapping);
        verify(userMappingRepository, never()).saveAndFlush(any(UserMapping.class));
    }

    @Test
    void getOrCreateByClerkUserId_WhenMappingMissing_ShouldCreateMapping() {
        UserMapping createdMapping = new UserMapping("clerk-user-2");
        createdMapping.setInternalUserId(99L);
        when(userMappingRepository.findByClerkUserId("clerk-user-2")).thenReturn(Optional.empty());
        when(userMappingRepository.saveAndFlush(any(UserMapping.class))).thenReturn(createdMapping);

        UserMapping resolvedMapping = userMappingService.getOrCreateByClerkUserId("clerk-user-2");

        assertEquals(99L, resolvedMapping.getInternalUserId());
        assertEquals("clerk-user-2", resolvedMapping.getClerkUserId());
    }

    @Test
    void getOrCreateByClerkUserId_WhenConcurrentInsertOccurs_ShouldReturnExistingMapping() {
        UserMapping existingMapping = new UserMapping("clerk-user-3");
        existingMapping.setInternalUserId(7L);
        when(userMappingRepository.findByClerkUserId("clerk-user-3"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingMapping));
        when(userMappingRepository.saveAndFlush(any(UserMapping.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        UserMapping resolvedMapping = userMappingService.getOrCreateByClerkUserId("clerk-user-3");

        assertSame(existingMapping, resolvedMapping);
    }

    @Test
    void getOrCreateByClerkUserId_WhenBlank_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> userMappingService.getOrCreateByClerkUserId("  "));
    }
}
