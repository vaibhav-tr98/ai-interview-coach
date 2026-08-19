package com.vaibhav.aiinterviewcoach.intelligence.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class JdInterviewConfigResponse {
    private String role;
    private String interviewType;
    private String difficulty;
    private String experienceLevel;
    private String interviewerPersona;
    private List<String> focusSkills;
}

