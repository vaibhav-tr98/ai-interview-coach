package com.vaibhav.aiinterviewcoach.analytics.dto;

import com.vaibhav.aiinterviewcoach.intelligence.dto.ResumeJobMatchResponse;

public record JobReadinessResponse(
    ResumeJobMatchResponse latestMatch,
    boolean hasData
) {}
