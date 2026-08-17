package com.vaibhav.aiinterviewcoach.interview.dto;

public record InterviewResponse(

        String interviewId,

        String sessionId,

        String interviewerMessage,

        String question,

        String interviewType

) {}
