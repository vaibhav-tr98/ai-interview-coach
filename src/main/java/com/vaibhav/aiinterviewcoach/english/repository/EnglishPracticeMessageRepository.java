package com.vaibhav.aiinterviewcoach.english.repository;

import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeMessage;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnglishPracticeMessageRepository extends JpaRepository<EnglishPracticeMessage, Long> {
    List<EnglishPracticeMessage> findBySessionOrderBySequenceNumberAsc(EnglishPracticeSession session);
    long countBySession(EnglishPracticeSession session);
}
