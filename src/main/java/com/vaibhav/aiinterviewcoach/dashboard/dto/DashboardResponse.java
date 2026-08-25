package com.vaibhav.aiinterviewcoach.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private double overallReadinessScore;
    private int totalInterviews;
    private int completedInterviews;
    private int codingAttempts;
    private double codingSuccessRate;
    private int gateAttempts;
    private double gateAccuracy;
    private double communicationScore;
    private double englishPracticeScore;
    private double studyPlanProgress;
    private int deepInterviewCount;
    private List<String> strongestAreas;
    private List<String> weakestAreas;
    private List<DashboardActivityDto> recentActivity;
    private String recommendedNextAction;
}
