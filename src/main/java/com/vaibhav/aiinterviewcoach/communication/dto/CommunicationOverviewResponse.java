package com.vaibhav.aiinterviewcoach.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationOverviewResponse {
    private Double averageOverallScore;
    private Double averageClarityScore;
    private Double averageConfidenceScore;
    private Integer totalAssessments;
    private List<CommunicationAssessmentResponse> history;
}
