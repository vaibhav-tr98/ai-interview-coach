package com.vaibhav.aiinterviewcoach.interview.dto;

import java.util.List;

public record TranscriptResponse(
        String sessionId,
        String interviewId,
        String interviewType,
        List<TranscriptTurn> turns
) {
}
