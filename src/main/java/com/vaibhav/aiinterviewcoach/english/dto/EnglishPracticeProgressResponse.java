package com.vaibhav.aiinterviewcoach.english.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishPracticeProgressResponse {
    private long totalSessions;
    private long completedSessions;
    private double averageOverallScore;
    private double averageGrammarScore;
    private double averageVocabularyScore;
    private double averageFluencyScore;
    private double averageClarityScore;
    private double averageRelevanceScore;
    private double averageConfidenceScore;
    private String strongestArea;
    private String weakestArea;
}
