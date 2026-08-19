package com.vaibhav.aiinterviewcoach.intelligence.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ResumeJobMatchResponse {
    private Long resumeId;
    private Long jobDescriptionId;
    private String role;
    
    private Integer requiredMatchPercentage;
    private Integer preferredMatchPercentage;
    private Integer overallMatchPercentage;
    
    private List<SkillMatchDetail> matchedSkills;
    private List<SkillMatchDetail> weakSkills;
    private List<String> missingSkills;
    private List<String> recommendations;
    
    @Data
    @Builder
    public static class SkillMatchDetail {
        private String skillName;
        private String resumeMatch;
        private String demonstratedSkill;
        private String currentPerformance;
        private String matchStatus; // STRONG_MATCH, WEAK_MATCH, etc.
    }
}

