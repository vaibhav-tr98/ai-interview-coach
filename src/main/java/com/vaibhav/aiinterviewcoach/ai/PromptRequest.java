package com.vaibhav.aiinterviewcoach.ai;

import jakarta.validation.constraints.NotBlank;

public record PromptRequest(

        @NotBlank(message = "Prompt cannot be empty")
        String prompt

) {}