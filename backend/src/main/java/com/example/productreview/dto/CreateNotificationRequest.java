package com.example.productreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a notification")
public class CreateNotificationRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Notification title", example = "Review Posted", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "Notification message", example = "Your review was posted successfully", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "Optional associated product ID", example = "1")
    private Long productId;
}
