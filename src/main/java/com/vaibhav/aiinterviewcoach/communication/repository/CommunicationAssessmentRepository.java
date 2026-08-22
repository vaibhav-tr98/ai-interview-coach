package com.vaibhav.aiinterviewcoach.communication.repository;

import com.vaibhav.aiinterviewcoach.communication.entity.CommunicationAssessment;
import com.vaibhav.aiinterviewcoach.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunicationAssessmentRepository extends JpaRepository<CommunicationAssessment, Long> {
    Optional<CommunicationAssessment> findByInterviewIdAndUser(Long interviewId, User user);
    List<CommunicationAssessment> findAllByUserOrderByAssessedAtDesc(User user);
    boolean existsByInterviewId(Long interviewId);
}
