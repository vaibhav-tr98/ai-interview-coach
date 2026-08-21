package com.vaibhav.aiinterviewcoach.planner.repository;

import com.vaibhav.aiinterviewcoach.planner.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
}
