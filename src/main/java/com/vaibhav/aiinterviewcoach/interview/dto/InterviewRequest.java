package com.vaibhav.aiinterviewcoach.interview.dto;

import jakarta.validation.constraints.NotBlank;

public record InterviewRequest(

        @NotBlank
        String interviewType,

        @NotBlank
        String experienceLevel,

        String resume,

        String jobDescription,

        String projectDescription

) {}