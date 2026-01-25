package com.solarityai.productreview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequestDto {

    @NotBlank(message = "error.chat.question.required")
    private String question;
}
