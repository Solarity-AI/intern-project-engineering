package com.solarityai.productreview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class NotificationCreateDto {

    @NotBlank(message = "error.notification.title.required")
    private String title;

    @NotBlank(message = "error.notification.message.required")
    private String message;

    private UUID productId;
}
