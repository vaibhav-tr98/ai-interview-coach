package com.vaibhav.aiinterviewcoach.interview.prompt;

import lombok.Builder;

import com.vaibhav.aiinterviewcoach.interview.enums.Difficulty;

@Builder
public record InterviewState(
        Integer nextQuestionNumber,
        Integer totalQuestions,
        Difficulty currentDifficulty
) {}
