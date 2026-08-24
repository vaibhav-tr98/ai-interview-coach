package com.vaibhav.aiinterviewcoach.gate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateSubjectProgressDTO {
    private String subject;
    private int totalAttempts;
    private int correctAttempts;
    private double accuracy;
    private List<GateTopicProgressDTO> topicWiseProgress;
}
