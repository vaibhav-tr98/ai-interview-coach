package com.vaibhav.aiinterviewcoach.analytics.controller;

import com.vaibhav.aiinterviewcoach.analytics.dto.*;
import com.vaibhav.aiinterviewcoach.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/interviews")
    public ResponseEntity<List<InterviewHistoryDTO>> getInterviewHistory() {
        return ResponseEntity.ok(analyticsService.getInterviewHistory());
    }

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewResponse> getOverview() {
        return ResponseEntity.ok(analyticsService.getOverview());
    }

    @GetMapping("/score-trend")
    public ResponseEntity<List<ScoreTrendDTO>> getScoreTrend() {
        return ResponseEntity.ok(analyticsService.getScoreTrend());
    }

    @GetMapping("/by-type")
    public ResponseEntity<Map<String, InterviewTypeAnalyticsDTO>> getByType() {
        return ResponseEntity.ok(analyticsService.getByType());
    }

    @GetMapping("/skills")
    public ResponseEntity<List<SkillAnalyticsDTO>> getSkills() {
        return ResponseEntity.ok(analyticsService.getSkills());
    }

    @GetMapping("/answer-quality")
    public ResponseEntity<AnswerQualityResponse> getAnswerQuality() {
        return ResponseEntity.ok(analyticsService.getAnswerQuality());
    }

    @GetMapping("/job-readiness")
    public ResponseEntity<JobReadinessResponse> getJobReadiness() {
        return ResponseEntity.ok(analyticsService.getJobReadiness());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentInterviewDTO>> getRecent() {
        return ResponseEntity.ok(analyticsService.getRecent());
    }
}
