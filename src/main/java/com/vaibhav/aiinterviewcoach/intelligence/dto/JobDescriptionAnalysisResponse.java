package com.vaibhav.aiinterviewcoach.intelligence.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class JobDescriptionAnalysisResponse {
    private Long id;
    private String title;
    private String company;
    private String role;
    private String seniority;
    private String summary;
    private List<JdSkillDto> skills;

    @Data
    @Builder
    public static class JdSkillDto {
        private String skillName;
        private Integer importance;
        private Boolean isRequired;
    }
}

