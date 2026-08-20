package com.vaibhav.aiinterviewcoach.analytics.dto;

import java.util.List;
import java.util.Map;

public record AnalyticsOverviewResponse(
    long totalInterviews,
    long completedInterviews,
    double averageInterviewScore,
    Integer bestInterviewScore,
    Integer latestInterviewScore,
    long totalQuestionsAnswered,
    String strongestSkill,
    String weakestSkill,
    List<String> improvingSkills,
    List<String> decliningSkills,
    List<String> stableSkills,
    String recentScoreTrend,
    Map<String, Long> interviewTypeBreakdown
) {}
