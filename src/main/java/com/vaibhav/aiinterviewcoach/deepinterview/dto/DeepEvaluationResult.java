package com.vaibhav.aiinterviewcoach.deepinterview.dto;

public record DeepEvaluationResult(
        Integer score,
        String feedback,
        String strengths,
        String weaknesses,
        Integer technicalCorrectnessScore,
        Integer depthScore,
        Integer projectOwnershipScore,
        Boolean consistencySignal,
        Integer communicationScore,
        Integer confidenceScore,
        String unsupportedClaims
) {
}
