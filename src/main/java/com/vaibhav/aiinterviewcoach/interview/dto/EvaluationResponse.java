package com.vaibhav.aiinterviewcoach.interview.dto;

public record EvaluationResponse(
        Integer score,
        String feedback,
        String strengths,
        String weaknesses
) {
}
