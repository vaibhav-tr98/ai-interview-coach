package com.vaibhav.aiinterviewcoach.gate.repository;

import com.vaibhav.aiinterviewcoach.gate.entity.GateQuestion;
import com.vaibhav.aiinterviewcoach.gate.enums.GateDifficulty;
import com.vaibhav.aiinterviewcoach.gate.enums.GateQuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GateQuestionRepository extends JpaRepository<GateQuestion, Long> {

    @Query("SELECT q FROM GateQuestion q WHERE " +
           "(:skillName IS NULL OR q.skill.name = :skillName) AND " +
           "(:topic IS NULL OR q.topic = :topic) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:type IS NULL OR q.questionType = :type)")
    List<GateQuestion> findQuestions(@Param("skillName") String skillName,
                                     @Param("topic") String topic,
                                     @Param("difficulty") GateDifficulty difficulty,
                                     @Param("type") GateQuestionType type);
                                     
    @Query(value = "SELECT * FROM gate_questions q WHERE " +
           "(:skillName IS NULL OR q.skill_id = (SELECT id FROM skills WHERE name = :skillName)) AND " +
           "(:topic IS NULL OR q.topic = :topic) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<GateQuestion> findRandomQuestions(@Param("skillName") String skillName,
                                           @Param("topic") String topic,
                                           @Param("difficulty") String difficulty,
                                           @Param("limit") int limit);
}
