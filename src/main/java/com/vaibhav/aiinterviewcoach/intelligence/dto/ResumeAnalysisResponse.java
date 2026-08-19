package com.vaibhav.aiinterviewcoach.intelligence.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ResumeAnalysisResponse {
    private Long id;
    private String title;
    private String summary;
    private List<ResumeSkillDto> skills;
    private List<ClaimDto> claims;

    @Data
    @Builder
    public static class ResumeSkillDto {
        private String skillName;
        private Integer confidence;
    }

    @Data
    @Builder
    public static class ClaimDto {
        private String claimText;
        private String status;
        private String verificationQuestions;
    }
}

