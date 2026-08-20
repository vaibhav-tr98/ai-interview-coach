package com.vaibhav.aiinterviewcoach.analytics.dto;

import java.time.LocalDateTime;

public record ScoreTrendDTO(
    Long interviewId,
    String interviewType,
    Integer score,
    LocalDateTime completedAt
) {}
