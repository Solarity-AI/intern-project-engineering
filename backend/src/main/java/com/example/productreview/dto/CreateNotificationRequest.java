package com.example.productreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

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

    public CreateNotificationRequest() {}

    public CreateNotificationRequest(String title, String message, Long productId) {
        this.title = title;
        this.message = message;
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
