package com.vaibhav.aiinterviewcoach.planner.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiStudyPlanResponse {
    private String targetRole;
    private List<AiStudyTask> days;

    @Data
    public static class AiStudyTask {
        private Integer dayNumber;
        private String skill;
        private String topic;
        private String description;
    }
}
