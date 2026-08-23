package com.vaibhav.aiinterviewcoach.coding.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class CodingProgressDTO {
    private int attemptedProblems;
    private int solvedProblems;
    private double successRate;
    private double averageScore;
    
    // SkillName -> Stats
    private Map<String, TopicStats> topicPerformance;
    private Map<String, Integer> difficultyDistribution;

    @Data
    @Builder
    public static class TopicStats {
        private int attempts;
        private double averageScore;
    }
}
