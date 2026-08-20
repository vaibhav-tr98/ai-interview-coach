package com.vaibhav.aiinterviewcoach.analytics.dto;

public record AnswerQualityResponse(
    double averageScore,
    double averageAnswerLength,
    long totalAnswers,
    double strongAnswerPercentage,
    double weakAnswerPercentage,
    Integer strongestAnswerScore,
    Integer weakestAnswerScore
) {}
