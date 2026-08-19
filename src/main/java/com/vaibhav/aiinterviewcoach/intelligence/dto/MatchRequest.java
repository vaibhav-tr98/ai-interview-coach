package com.vaibhav.aiinterviewcoach.intelligence.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchRequest {
    @NotNull
    private Long resumeId;

    @NotNull
    private Long jobDescriptionId;
}

