package com.example.productreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification data for display")
public class NotificationDTO {

    @Schema(description = "Unique notification identifier", example = "1")
    private Long id;

    @Schema(description = "Notification title", example = "New review on your product")
    private String title;

    @Schema(description = "Notification message", example = "Someone left a 5-star review")
    private String message;

    @Schema(description = "Whether the notification has been read", example = "false")
    private boolean read;

    @Schema(description = "When the notification was created")
    private LocalDateTime createdAt;

    @Schema(description = "Linked product ID (optional)", example = "1")
    private Long productId;
}
