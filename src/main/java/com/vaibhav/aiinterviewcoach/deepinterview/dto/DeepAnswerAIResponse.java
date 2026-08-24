package com.vaibhav.aiinterviewcoach.deepinterview.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepAnswerAIResponse(
        DeepEvaluationResult evaluation,
        String nextQuestion,
        String closingMessage
) {
}
