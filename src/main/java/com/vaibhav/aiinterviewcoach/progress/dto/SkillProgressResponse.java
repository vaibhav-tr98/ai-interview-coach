package com.vaibhav.aiinterviewcoach.progress.dto;

import java.time.LocalDateTime;

public record SkillProgressResponse(
        String skill,
        Double averageScore,
        Integer bestScore,
        Integer weakestScore,
        Integer attemptCount,
        LocalDateTime lastPracticedAt,
        String trend
) {
}
