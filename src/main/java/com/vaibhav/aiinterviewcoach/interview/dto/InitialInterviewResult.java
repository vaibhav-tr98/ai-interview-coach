package com.vaibhav.aiinterviewcoach.interview.dto;

import jakarta.validation.constraints.NotBlank;

public record InitialInterviewResult(
        @NotBlank(message = "Interviewer message must not be blank")
        String interviewerMessage,

        @NotBlank(message = "Question must not be blank")
        String question
) {
}
