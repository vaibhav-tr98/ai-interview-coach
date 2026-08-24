package com.vaibhav.aiinterviewcoach.gate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GateAttemptResponse {
    private Long attemptId;
    private Long questionId;
    private String selectedAnswer;
    private String correctOption;
    private Boolean isCorrect;
    private Integer score;
}
