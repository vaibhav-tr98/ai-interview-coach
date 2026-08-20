package com.vaibhav.aiinterviewcoach.interview.repository;

import com.vaibhav.aiinterviewcoach.interview.entity.AnswerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnswerEvaluationRepository extends JpaRepository<AnswerEvaluation, Long> {
    List<AnswerEvaluation> findByQuestionAnswerSessionSessionId(String sessionId);
    Optional<AnswerEvaluation> findByQuestionAnswerId(Long questionAnswerId);
    List<AnswerEvaluation> findByQuestionAnswer_Session_Interview_UserId(Long userId);
}
