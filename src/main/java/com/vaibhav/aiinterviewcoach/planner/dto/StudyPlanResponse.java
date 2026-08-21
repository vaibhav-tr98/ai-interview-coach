package com.vaibhav.aiinterviewcoach.planner.dto;

import com.vaibhav.aiinterviewcoach.planner.enums.PlanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudyPlanResponse {
    private Long id;
    private String targetRole;
    private PlanStatus status;
    private LocalDateTime createdAt;
    private List<StudyTaskResponse> tasks;
}
