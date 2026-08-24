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
public class GateProgressDTO {
    private int totalAttempts;
    private int correctAttempts;
    private int incorrectAttempts;
    private double accuracy;
    private double averageScore;
    
    private String strongestSubject;
    private String weakestSubject;
    
    private List<GateSubjectProgressDTO> subjectWiseProgress;
}
