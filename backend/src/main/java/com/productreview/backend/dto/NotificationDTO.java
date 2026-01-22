package com.productreview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String userId;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Long productId;
}