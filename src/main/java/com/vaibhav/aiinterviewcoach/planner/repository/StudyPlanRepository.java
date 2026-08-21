package com.vaibhav.aiinterviewcoach.planner.repository;

import com.vaibhav.aiinterviewcoach.planner.entity.StudyPlan;
import com.vaibhav.aiinterviewcoach.planner.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    Optional<StudyPlan> findByUserEmailAndStatus(String email, PlanStatus status);
    List<StudyPlan> findAllByUserEmailAndStatus(String email, PlanStatus status);
}
