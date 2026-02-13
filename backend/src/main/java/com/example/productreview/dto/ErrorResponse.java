package com.example.productreview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Structured error response returned for all API errors")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred (ISO 8601)", example = "2026-02-13T14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int code;

    @Schema(description = "Human-readable error message", example = "Reviewer name is required")
    private String message;

    @Schema(description = "Field-level validation errors (field name to error message)", example = "{\"reviewerName\": \"must not be blank\", \"rating\": \"must be at least 1\"}")
    private Map<String, String> details;

    public ErrorResponse() {
    }

    public ErrorResponse(int code, String message) {
        this.timestamp = LocalDateTime.now();
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(int code, String message, Map<String, String> details) {
        this.timestamp = LocalDateTime.now();
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
