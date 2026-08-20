package com.vaibhav.aiinterviewcoach.analytics.dto;

import java.time.LocalDateTime;

public record InterviewHistoryDTO(
    Long interviewId,
    String interviewType,
    String targetRole,
    String status,
    int totalQuestions,
    Integer overallScore,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    Long resumeId,
    Long jobDescriptionId
) {}
