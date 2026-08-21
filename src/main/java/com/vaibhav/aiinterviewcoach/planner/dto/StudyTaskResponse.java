package com.vaibhav.aiinterviewcoach.planner.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudyTaskResponse {
    private Long id;
    private Integer dayNumber;
    private String skillName;
    private String topic;
    private String description;
    private Boolean completed;
}
