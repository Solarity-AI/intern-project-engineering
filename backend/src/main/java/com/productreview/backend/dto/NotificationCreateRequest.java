package com.productreview.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {
    private String title;
    private String message;
    private Long productId;
}