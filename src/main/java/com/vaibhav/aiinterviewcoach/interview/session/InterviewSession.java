package com.vaibhav.aiinterviewcoach.interview.session;

import java.util.UUID;

public class InterviewSession {

    private final String sessionId =
            UUID.randomUUID().toString();

    public String getSessionId() {
        return sessionId;
    }
}