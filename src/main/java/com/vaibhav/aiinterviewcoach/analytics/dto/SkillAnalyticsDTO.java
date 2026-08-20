package com.vaibhav.aiinterviewcoach.analytics.dto;

import java.time.LocalDateTime;

public record SkillAnalyticsDTO(
    String skillName,
    String category,
    Double averageScore,
    Integer bestScore,
    Integer weakestScore,
    Integer attemptCount,
    String trend,
    LocalDateTime lastPracticedAt,
    String strengthLevel
) {}
