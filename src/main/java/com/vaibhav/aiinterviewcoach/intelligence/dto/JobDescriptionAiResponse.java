package com.vaibhav.aiinterviewcoach.intelligence.dto;

import lombok.Data;
import java.util.List;

@Data
public class JobDescriptionAiResponse {
    private String company;
    private String role;
    private String seniority;
    private String summary;
    private List<SkillItem> requiredSkills;
    private List<SkillItem> preferredSkills;

    @Data
    public static class SkillItem {
        private String skill;
        private Integer importance;
    }
}

