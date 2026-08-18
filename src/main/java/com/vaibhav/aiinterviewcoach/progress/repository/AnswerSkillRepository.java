package com.vaibhav.aiinterviewcoach.progress.repository;

import com.vaibhav.aiinterviewcoach.progress.entity.AnswerSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerSkillRepository extends JpaRepository<AnswerSkill, Long> {
    List<AnswerSkill> findByQuestionAnswerId(Long questionAnswerId);
    List<AnswerSkill> findBySkillId(Long skillId);
    List<AnswerSkill> findByQuestionAnswerSessionSessionId(String sessionId);

    @Query("SELECT ans FROM AnswerSkill ans " +
           "WHERE ans.skill.id = :skillId AND ans.questionAnswer.session.interview.user.id = :userId " +
           "ORDER BY ans.createdAt ASC")
    List<AnswerSkill> findHistoryBySkillAndUserOrdered(@Param("skillId") Long skillId, @Param("userId") Long userId);
}
