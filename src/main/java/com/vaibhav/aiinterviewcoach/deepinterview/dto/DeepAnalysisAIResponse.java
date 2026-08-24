package com.vaibhav.aiinterviewcoach.deepinterview.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepAnalysisAIResponse(
        Integer overallScore,
        Integer technicalCorrectnessScore,
        Integer depthScore,
        Integer projectOwnershipScore,
        Boolean consistencySignal,
        Integer communicationScore,
        Integer confidenceScore,
        List<String> strengths,
        List<String> weaknesses,
        String unsupportedClaims,
        List<String> recommendations,
        String interviewSummary
) {
}
