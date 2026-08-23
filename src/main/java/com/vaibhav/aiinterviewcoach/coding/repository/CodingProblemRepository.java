package com.vaibhav.aiinterviewcoach.coding.repository;

import com.vaibhav.aiinterviewcoach.coding.entity.CodingProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long> {
    List<CodingProblem> findBySkillId(Long skillId);
}
