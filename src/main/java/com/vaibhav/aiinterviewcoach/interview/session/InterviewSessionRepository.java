package com.vaibhav.aiinterviewcoach.interview.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewSessionRepository
        extends JpaRepository<InterviewSession, Long> {

    Optional<InterviewSession> findBySessionId(String sessionId);
}
