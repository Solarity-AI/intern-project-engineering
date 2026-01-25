package com.solarityai.productreview.dto;

import com.solarityai.backendfw.foundation.model.BaseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppNotificationDto extends BaseDto {

    private String userId;

    @NotBlank(message = "error.notification.title.required")
    private String title;

    @NotBlank(message = "error.notification.message.required")
    private String message;

    private Boolean isRead;
    private Instant notificationCreatedAt;
    private UUID productId;
}
