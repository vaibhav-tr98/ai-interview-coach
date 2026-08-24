package com.vaibhav.aiinterviewcoach.deepinterview.dto;

public record DeepInterviewAnswerResponse(
        String sessionId,
        Integer questionNumber,
        String question,
        String answer,
        DeepEvaluationResult evaluation,
        String nextQuestion,
        String closingMessage,
        boolean isComplete
) {
}
