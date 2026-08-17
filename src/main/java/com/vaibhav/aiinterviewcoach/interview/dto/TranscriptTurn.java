package com.vaibhav.aiinterviewcoach.interview.dto;

public record TranscriptTurn(
        Integer questionNumber,
        String question,
        String answer,
        EvaluationResponse evaluation
) {
}
