package com.vaibhav.aiinterviewcoach.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterviewRequest(

        @NotBlank
        String interviewType,

        @NotBlank
        String experienceLevel,

        @Size(max = 5000)
        String resume,

        @Size(max = 5000)
        String jobDescription,

        @Size(max = 2000)
        String projectDescription,

        String role,

        String dsaDifficulty,

        String dsaTopic,

        String projectUrl,

        Integer durationMinutes,

        String interviewerPersona

) {}