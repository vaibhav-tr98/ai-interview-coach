package com.vaibhav.aiinterviewcoach.analytics.dto;

import java.time.LocalDateTime;

public record RecentInterviewDTO(
    Long interviewId,
    String interviewType,
    Integer score,
    LocalDateTime completedAt,
    String strongestArea,
    String weakestArea
) {}
