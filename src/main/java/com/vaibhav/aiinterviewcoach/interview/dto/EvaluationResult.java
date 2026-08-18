package com.vaibhav.aiinterviewcoach.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EvaluationResult(
        @NotNull(message = "Score must not be null")
        @Min(value = 0, message = "Score must not be less than 0")
        @Max(value = 100, message = "Score must not be greater than 100")
        Integer score,

        @NotBlank(message = "Feedback must not be blank")
        String feedback,

        @NotBlank(message = "Strengths must not be blank")
        String strengths,

        @NotBlank(message = "Weaknesses must not be blank")
        String weaknesses,
        
        java.util.List<SkillEvaluationDTO> skills
) {
}
