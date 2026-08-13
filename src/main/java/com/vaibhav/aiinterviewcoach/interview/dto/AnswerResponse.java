package com.vaibhav.aiinterviewcoach.interview.dto;

public record AnswerResponse(
        String sessionId,
        Integer questionNumber,
        String previousQuestion,
        String answer,
        String nextQuestion
) {
}
