package com.vaibhav.aiinterviewcoach.english.repository;

import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeSession;
import com.vaibhav.aiinterviewcoach.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnglishPracticeSessionRepository extends JpaRepository<EnglishPracticeSession, Long> {
    List<EnglishPracticeSession> findByUserOrderByIdDesc(User user);
    long countByUser(User user);
    long countByUserAndStatus(User user, com.vaibhav.aiinterviewcoach.english.enums.EnglishPracticeStatus status);
}
