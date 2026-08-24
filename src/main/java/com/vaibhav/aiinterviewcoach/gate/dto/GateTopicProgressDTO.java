package com.vaibhav.aiinterviewcoach.gate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateTopicProgressDTO {
    private String topic;
    private int totalAttempts;
    private int correctAttempts;
    private double accuracy;
}
