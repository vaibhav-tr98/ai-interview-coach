package com.vaibhav.aiinterviewcoach.interview.dto;

public record SessionResponse(
        String sessionId,
        String interviewId,
        String interviewType,
        Integer questionNumber,
        String currentQuestion,
        String sessionStatus
) {
}
