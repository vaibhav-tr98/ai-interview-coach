package com.vaibhav.aiinterviewcoach.interview.prompt;

import lombok.Builder;

@Builder
public record InterviewTurnContext(
        Integer questionNumber,
        String question,
        String answer,
        Integer evaluationScore,
        String evaluationFeedback,
        String evaluationStrengths,
        String evaluationWeaknesses
) {}
