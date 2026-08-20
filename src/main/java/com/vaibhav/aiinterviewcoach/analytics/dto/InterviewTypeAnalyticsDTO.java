package com.vaibhav.aiinterviewcoach.analytics.dto;

public record InterviewTypeAnalyticsDTO(
    String interviewType,
    long attempts,
    long completedAttempts,
    double averageScore,
    Integer bestScore,
    Integer latestScore
) {}
