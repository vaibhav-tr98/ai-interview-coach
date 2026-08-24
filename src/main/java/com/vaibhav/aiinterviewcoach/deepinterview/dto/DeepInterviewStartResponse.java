package com.vaibhav.aiinterviewcoach.deepinterview.dto;

public record DeepInterviewStartResponse(
        String interviewId,
        String sessionId,
        String interviewerMessage,
        String firstQuestion,
        String type
) {
}
