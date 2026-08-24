package com.vaibhav.aiinterviewcoach.deepinterview.dto;

import java.util.List;

public record DeepInterviewResultResponse(
        String sessionId,
        String interviewId,
        String type,
        Integer totalQuestions,
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
