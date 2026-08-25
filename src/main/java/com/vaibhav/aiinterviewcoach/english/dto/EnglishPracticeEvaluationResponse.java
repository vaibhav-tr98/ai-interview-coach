package com.vaibhav.aiinterviewcoach.english.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishPracticeEvaluationResponse {
    private Integer grammarScore;
    private Integer vocabularyScore;
    private Integer fluencyScore;
    private Integer clarityScore;
    private Integer relevanceScore;
    private Integer confidenceScore;
    private Integer overallScore;
    private List<String> strengths;
    private List<String> improvements;
    private String feedback;
}
