package com.vaibhav.aiinterviewcoach.intelligence.repository;

import com.vaibhav.aiinterviewcoach.intelligence.entity.JobDescriptionSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDescriptionSkillRepository extends JpaRepository<JobDescriptionSkill, Long> {
    List<JobDescriptionSkill> findByJobDescriptionId(Long jobDescriptionId);
    void deleteByJobDescriptionId(Long jobDescriptionId);
}

