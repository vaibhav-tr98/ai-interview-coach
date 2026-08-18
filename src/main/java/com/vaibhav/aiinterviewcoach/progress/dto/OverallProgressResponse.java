package com.vaibhav.aiinterviewcoach.progress.dto;

import java.util.List;

public record OverallProgressResponse(
        Integer overallScore,
        Integer totalInterviews,
        Integer completedInterviews,
        Integer averageInterviewScore,
        Integer bestInterviewScore,
        Integer latestInterviewScore,
        List<SkillScoreDTO> strongestSkills,
        List<SkillScoreDTO> weakestSkills,
        List<String> improvingSkills,
        List<String> decliningSkills,
        List<RecommendationDTO> recommendedSkills
) {
}
