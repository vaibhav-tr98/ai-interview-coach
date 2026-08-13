package com.vaibhav.aiinterviewcoach.interview.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(
        @NotBlank(message = "Answer cannot be blank")
        String answer
) {
}
