package com.vaibhav.aiinterviewcoach.interview.session;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository
        extends JpaRepository<InterviewSession, Long> {
}
