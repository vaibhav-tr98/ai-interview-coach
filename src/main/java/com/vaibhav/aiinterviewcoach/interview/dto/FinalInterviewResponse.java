package com.vaibhav.aiinterviewcoach.interview.dto;

import java.util.List;

public record FinalInterviewResponse(
        String sessionId,
        String interviewId,
        String interviewType,
        Integer totalQuestions,
        Integer overallScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations
) {
}
