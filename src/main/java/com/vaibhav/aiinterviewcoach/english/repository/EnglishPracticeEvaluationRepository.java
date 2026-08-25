package com.vaibhav.aiinterviewcoach.english.repository;

import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeEvaluation;
import com.vaibhav.aiinterviewcoach.english.entity.EnglishPracticeSession;
import com.vaibhav.aiinterviewcoach.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EnglishPracticeEvaluationRepository extends JpaRepository<EnglishPracticeEvaluation, Long> {
    Optional<EnglishPracticeEvaluation> findBySession(EnglishPracticeSession session);

    @Query("SELECT e FROM EnglishPracticeEvaluation e WHERE e.session.user = :user")
    List<EnglishPracticeEvaluation> findAllByUser(@Param("user") User user);
}
