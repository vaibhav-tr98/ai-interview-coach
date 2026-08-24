package com.vaibhav.aiinterviewcoach.deepinterview.dto;

import jakarta.validation.constraints.NotBlank;

public record DeepInterviewAnswerRequest(
        @NotBlank(message = "Answer cannot be empty")
        String answer
) {
}
