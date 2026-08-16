package com.vaibhav.aiinterviewcoach.interview.prompt;

import lombok.Builder;

@Builder
public record InterviewState(
        Integer nextQuestionNumber,
        Integer totalQuestions
) {}
