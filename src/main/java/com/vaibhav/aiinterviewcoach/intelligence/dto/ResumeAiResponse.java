package com.vaibhav.aiinterviewcoach.intelligence.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeAiResponse {
    private String summary;
    private String experienceLevel;
    private List<SkillItem> skills;
    
    @Data
    public static class SkillItem {
        private String skill;
        private Integer confidence;
    }
}

