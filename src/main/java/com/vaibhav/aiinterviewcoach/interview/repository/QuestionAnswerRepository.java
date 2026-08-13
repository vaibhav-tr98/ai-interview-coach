package com.vaibhav.aiinterviewcoach.interview.repository;

import com.vaibhav.aiinterviewcoach.interview.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
}
