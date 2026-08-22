package com.vaibhav.aiinterviewcoach.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationAssessmentResponse {
    private Long id;
    private Long interviewId;
    private Integer overallScore;
    private Integer clarityScore;
    private Integer relevanceScore;
    private Integer completenessScore;
    private Integer concisenessScore;
    private Integer vocabularyScore;
    private Integer confidenceScore;
    private Integer fillerWordCount;
    private Integer repetitionCount;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> recommendations;
    private LocalDateTime assessedAt;
}
